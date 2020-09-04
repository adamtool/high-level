package uniolunisaar.adam.logic.renderer.hl.pnml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.renderer.RenderException;
import uniol.apt.io.renderer.Renderer;
import uniol.apt.io.renderer.impl.AbstractRenderer;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColorDomain;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColorTokens;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.StaticColorClass;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcType;
import uniolunisaar.adam.ds.highlevel.arcexpressions.SetMinusTerm;
import uniolunisaar.adam.ds.highlevel.predicate.BasicPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.BinaryPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.Constants;
import uniolunisaar.adam.ds.highlevel.predicate.DomainTerm;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicateTerm;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicateType;
import uniolunisaar.adam.ds.highlevel.predicate.UnaryPredicate;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.PredecessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.petrinet.PetriNetExtensionHandler;
import uniolunisaar.adam.tools.CartesianProduct;
import uniolunisaar.adam.util.AdamExtensions;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Render {@link HLPetriGame} to pnml
 *
 * @author Lukas Panneke
 */
public class PnmlRenderer extends AbstractRenderer<HLPetriGame> implements Renderer<HLPetriGame> {

	public static boolean renderGameExtensions = true;

	public static final String PNML_XMLNS = "http://www.pnml.org/version-2009/grammar/pnml";
	public static final String NET_TYPE = "http://www.pnml.org/version-2009/grammar/symmetricnet";

	public static final String EXTENSION_KEY_NAME = AdamExtensions.label.name();

	public static class Configuration {

		/**
		 * If a condition can always fire
		 * it is usually represented as having no condition.
		 * But technically it's condition is TRUE.
		 * With this option enabled
		 * no condition will be rendered,
		 * if the transitions condition is just TRUE.
		 */
		public boolean omitTrueCondition = true;

		/**
		 * The condition of a transition can
		 * - in addition to the pnml structure -
		 * also be rendered as text.
		 * This is useful for a human
		 * to see the meaning of a transition at a glance,
		 * but rendering it can cause a stack overflow for huge conditions.
		 */
		public boolean renderConditionText = true;

		/**
		 * PNML has no representation for our DomainTerm (as far as we know).
		 * But in PNML you can compare a variable to a color.
		 * Thus
		 * Domain(variable) == ColorClass
		 * can be represented as
		 * variable == color1 OR variable == color2...
		 */
		public boolean allowDomainTerm = true;
	}

	private final Configuration config;

	public PnmlRenderer() {
		this(new Configuration());
	}

	public PnmlRenderer(Configuration config) {
		this.config = config;
	}

	private static final class Renderer {

		private final Configuration config;
		private final Document dom;
		private final HLPetriGame game;

		public Renderer(Configuration config, HLPetriGame game) throws ParserConfigurationException {
			this.config = config;
			this.game = game;
			this.dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = this.dom.createElement("pnml");
			root.setAttribute("xmlns", PNML_XMLNS);
			root.appendChild(renderNet());
			dom.appendChild(root);
		}

		private Element renderNet() {
			Element net = dom.createElement("net");
			net.setAttribute("id", game.getName());
			net.setAttribute("type", NET_TYPE);
			if (game.hasExtension(EXTENSION_KEY_NAME)) {
				String name = (String) game.getExtension(EXTENSION_KEY_NAME);
				net.appendChild(renderName(name));
			}

			net.appendChild(renderDeclaration());
			net.appendChild(renderPage());
			return net;
		}

		private Element renderDeclaration() {
			Element declarations = dom.createElement("declarations");

			for (BasicColorClass cc : game.getBasicColorClasses()) {
				/*
				 * <finiteintrage> may be applicable,
				 * but we don't detect that because it's expensive
				 */
				declarations.appendChild(renderEnumeration(cc));
				if (cc.hasStaticSubclasses()) {
					declarations.appendChild(renderPartition(cc));
				}
			}

			game.getPlaces().stream()
					.map(game::getColorDomain)
					.filter(colorDomain -> colorDomain.size() > 1)
					.distinct()
					.map(this::renderProduct)
					.forEach(declarations::appendChild);

			renderVariableDeclaration().forEach(declarations::appendChild);

			return wrapIn("declaration", wrapIn("structure", declarations));
		}

		private Element renderPartition(BasicColorClass cc) {
			Element partition = dom.createElement("partition");
			partition.setAttribute("id", cc.getId() + "Partition");
			partition.appendChild(renderColorClassReference(cc));

			cc.getStaticSubclasses().forEach((id, subclass) -> {
				Element partitionelement = dom.createElement("partitionelement");
				partitionelement.setAttribute("id", id);

				for (Color color : subclass.getColors()) {
					partitionelement.appendChild(renderColorReference(color));
				}

				partition.appendChild(partitionelement);
			});

			return partition;
		}

		private Element renderEnumeration(BasicColorClass cc) {
			Element namedsort = dom.createElement("namedsort");
			namedsort.setAttribute("id", cc.getId());
			// TODO besser einen typen ohne "Named", weil wir dafür keine Namen haben

			Element enumeration = dom.createElement(cc.isOrdered() ? "cyclicenumeration" : "finiteenumeration");

			for (Color color : cc.getColors()) {
				Element feconstant = dom.createElement("feconstant");
				feconstant.setAttribute("id", color.getId());

				enumeration.appendChild(feconstant);
			}

			namedsort.appendChild(enumeration);
			return namedsort;
		}

		private Element renderProduct(ColorDomain domain) {
			Element namedsort = dom.createElement("namedsort");
			namedsort.setAttribute("id", tupleName(domain));

			Element productsort = dom.createElement("productsort");
			for (String factor : domain) {
				productsort.appendChild(renderColorClassReference(factor));
			}

			namedsort.appendChild(productsort);
			return namedsort;
		}

		private Set<Pair<String, String>> getVariableNamesWithColorClass() {
			Set<Pair<String, String>> ret = new HashSet<>(); /* variable name and variables color class */
			for (Flow edge : game.getEdges()) {
				ArcExpression arc = game.getArcExpression(edge);
				for (var expression : arc.getExpresssions()) {
					ColorDomain domain = game.getColorDomain(edge.getPlace());
					switch (expression.getFirst()) {
						case VARIABLE:
						case PREDECESSOR:
						case SUCCESSOR: {
							if (domain.size() != 1) {
								throw new IllegalStateException("a variable must have an atomic color class");
							}
							String name = expression.getSecond().getVariables().iterator().next().getName();
							ret.add(new Pair<>(name, tupleName(domain)));
							break;
						}
						case COLORCLASS:
							/* has no variables, thus ignored */
							break;
						case SETMINUS: {
							if (domain.size() != 1) {
								throw new IllegalStateException("a SetMinus term must only have variables of one color class");
							}
							String domainName = tupleName(domain);
							ret.addAll(expression.getSecond().getVariables().stream()
									.map(Variable::getName)
									.map(name -> new Pair<>(name, domainName))
									.collect(Collectors.toSet()));
							break;
						}
						case TUPLE: {
							var tuples = ((ArcTuple) expression.getSecond()).getValues();
							if (tuples.size() != domain.size()) {
								throw new IllegalStateException();
							}
							var tupleIterator = tuples.iterator();
							var domainIterator = domain.iterator();

							while (tupleIterator.hasNext()) {
								var currentExpression = tupleIterator.next();
								String currentColorClassId = domainIterator.next();
								switch (currentExpression.getFirst()) {
									case VARIABLE:
									case PREDECESSOR:
									case SUCCESSOR: {
										String name = currentExpression.getSecond().getVariables().iterator().next().getName();
										ret.add(new Pair<>(name, currentColorClassId));
										break;
									}
									case COLORCLASS:
										/* has no variables, thus ignored */
										break;
									case SETMINUS: {
										ret.addAll(expression.getSecond().getVariables().stream()
												.map(Variable::getName)
												.map(name -> new Pair<>(name, currentColorClassId))
												.collect(Collectors.toSet()));
										break;
									}
									default:
										throw new UnsupportedOperationException(currentExpression.getFirst() + " cannot be in a tuple");
								}
							}
							break;
						}
						default:
							throw new UnsupportedOperationException(expression.getFirst() + " is not supported");
					}
				}
			}
			return ret;
		}

		private List<Element> renderVariableDeclaration() {
			Set<Pair<String, String>> variableNamesWithColorClass = getVariableNamesWithColorClass();
			Function<Pair<String, String>, String> idChooser;
			if (isSecondUniquelyIdentifiedByFirst(variableNamesWithColorClass)) {
				idChooser = Pair::getFirst;
			} else {
				idChooser = nameTypePair -> nameTypePair.getFirst() + ":" + nameTypePair.getSecond();
			}
			return variableNamesWithColorClass.stream()
					.map(nameTypePair -> {
						Element variabledecl = dom.createElement("variabledecl");
						variabledecl.setAttribute("id", idChooser.apply(nameTypePair));
						variabledecl.appendChild(renderColorClassReference(nameTypePair.getSecond()));
						return variabledecl;
					})
					.collect(Collectors.toList());
		}

		private <F, S> boolean isSecondUniquelyIdentifiedByFirst(Set<Pair<F, S>> pairs) {
			Map<F, S> encounteredPairs = new HashMap<>();
			for (Pair<F, S> pair : pairs) {
				if (!encounteredPairs.containsKey(pair.getFirst())) {
					encounteredPairs.put(pair.getFirst(), pair.getSecond());
				} else if (!encounteredPairs.get(pair.getFirst()).equals(pair.getSecond())) {
					return false;
				}
			}
			return true;
		}

		private Element renderPage() {
			Element page = dom.createElement("page");
			page.setAttribute("id", "top-level");

			for (Place place : game.getPlaces()) {
				page.appendChild(renderPlace(place));
			}
			for (Transition transition : game.getTransitions()) {
				page.appendChild(renderTransition(transition));
			}
			for (Flow arc : game.getEdges()) {
				page.appendChild(renderArc(arc));
			}

			return page;
		}

		private Element renderPlace(Place place) {
			Element placeElement = dom.createElement("place");
			placeElement.setAttribute("id", place.getId());
			if (renderGameExtensions) {
				Element gameElement = dom.createElement("game");

				if (game.isEnvironment(place)) {
					gameElement.appendChild(dom.createElement("env"));
				}
				if (game.isBad(place)) {
					gameElement.appendChild(dom.createElement("bad"));
				}
				if (game.isBuchi(place)) {
					gameElement.appendChild(dom.createElement("buchi"));
				}
				if (game.isReach(place)) {
					gameElement.appendChild(dom.createElement("reach"));
				}

				placeElement.appendChild(gameElement);
			}

			if (place.hasExtension(EXTENSION_KEY_NAME)) {
				String name = (String) place.getExtension(EXTENSION_KEY_NAME);
				placeElement.appendChild(renderName(name));
			}

			renderGraphics(place).ifPresent(placeElement::appendChild);

			placeElement.appendChild(renderTypeReference(game.getColorDomain(place)));

			ColorTokens colorTokens = game.getColorTokens(place);
			if (colorTokens != null && colorTokens.size() > 0) {
				placeElement.appendChild(renderInitialMarking(colorTokens));
			}
			return placeElement;
		}

		private Element renderTypeReference(ColorDomain domain) {
			return wrapIn("type", wrapIn("structure", renderColorClassReference(tupleName(domain))));
		}

		private Element renderInitialMarking(ColorTokens tokens) {
			Element structure = dom.createElement("structure");

			if (tokens.size() == 1) {
				ColorToken onlyColorToken = tokens.iterator().next();
				if (onlyColorToken.size() == 0) {
					throw new IllegalStateException();
				}
				structure.appendChild(renderColorToken(onlyColorToken));
			} else {
				/*
				 * <all> may be applicable,
				 * but we don't detect that because it's expensive
				 */
				Element add = dom.createElement("add");
				for (ColorToken token : tokens) {
					add.appendChild(wrapInSubterm(renderColorToken(token)));
				}
				structure.appendChild(add);
			}

			return wrapIn("hlinitialMarking", structure);
		}

		private Element renderColorToken(ColorToken token) {
			if (token.size() == 1) {
				return wrapInNumberof(renderColorReference(token.get(0)));
			} else {
				return wrapInNumberof(renderTuple(token));
			}
		}

		private Element renderTuple(ColorToken token) {
			Element tuple = dom.createElement("tuple");
			for (int i = 0; i < token.size(); i++) {
				tuple.appendChild(wrapInSubterm(renderColorReference(token.get(i))));
			}
			return tuple;
		}

		private Element renderTransition(Transition transition) {
			Element transitionElement = dom.createElement("transition");
			transitionElement.setAttribute("id", transition.getId());

			if (transition.hasExtension(EXTENSION_KEY_NAME)) {
				String name = (String) transition.getExtension(EXTENSION_KEY_NAME);
				transitionElement.appendChild(renderName(name));
			}

			renderGraphics(transition).ifPresent(transitionElement::appendChild);

			IPredicate condition = game.getPredicate(transition);
			if (!(this.config.omitTrueCondition && condition instanceof Constants && condition.equals(Constants.TRUE))) {
				transitionElement.appendChild(renderCondition(condition));
			}

			return transitionElement;
		}

		private Element renderCondition(IPredicate predicate) {
			Element condition = dom.createElement("condition");
			if (this.config.renderConditionText) {
				Element text = dom.createElement("text");
				text.setTextContent(predicate.toString());
				condition.appendChild(text);
			}
			condition.appendChild(wrapIn("structure", renderPredicate(predicate)));
			return condition;
		}

		private Element renderPredicate(IPredicate predicate) {
			if (predicate instanceof Constants) {
				Constants constant = (Constants) predicate;
				String value;
				switch (constant) {
					case FALSE:
						value = "false";
						break;
					case TRUE:
						value = "true";
						break;
					default:
						throw new UnsupportedOperationException("Unknown constant: " + predicate);
				}
				return renderElementWithAttribute("booleanconstant", "value", value);
			} else if (predicate instanceof UnaryPredicate) {
				UnaryPredicate unary = (UnaryPredicate) predicate;
				switch (unary.getOperator()) {
					case NEG:
						return wrapInElementWithSubterms("not", renderPredicate(unary.getOperand()));
					default:
						throw new UnsupportedOperationException("Unknown unary operator " + unary.getOperator() + " in " + predicate);
				}
			} else if (predicate instanceof BinaryPredicate) {
				BinaryPredicate binary = (BinaryPredicate) predicate;
				String elementName;
				switch (binary.getOperator()) {
					case AND:
						elementName = "and";
						break;
					case OR:
						elementName = "or";
						break;
					case IMP:
						elementName = "imply";
						break;
					case BIMP:
						elementName = "equality";
						break;
					default:
						throw new UnsupportedOperationException("Unknown binary operator " + binary.getOperator() + " in " + predicate);
				}
				return wrapInElementWithSubterms(elementName,
						renderPredicate(binary.getLeftOperand()),
						renderPredicate(binary.getRightOperand())
				);
			} else if (predicate instanceof BasicPredicate) {
				BasicPredicate<?> equality = (BasicPredicate<?>) predicate;
				if (equality.getLeftOperand() instanceof DomainTerm) {
					return renderEqualityDomainTermEqColorClassTerm(equality);
				} else {
					return renderEquality(equality);
				}
			} else {
				throw new UnsupportedOperationException("This predicate (" + predicate + ") cannot yet be rendered to pnml");
			}
		}

		private Element renderEquality(BasicPredicate<?> equality) {
			return wrapInElementWithSubterms(equality.getOperator() == BasicPredicate.Operator.EQ ? "equality" : "inequality",
					renderPredicateTerm(equality.getLeftOperand()),
					renderPredicateTerm(equality.getRightOperand())
			);
		}

		private <T extends IPredicateType> Element renderPredicateTerm(IPredicateTerm<T> term) {
			if (term instanceof Variable) {
				return renderVariableReference((Variable) term);
			} else if (term instanceof PredecessorTerm) {
				return renderNeighbourTerm(((PredecessorTerm) term).getVariable(), true);
			} else if (term instanceof SuccessorTerm) {
				return renderNeighbourTerm(((SuccessorTerm) term).getVariable(), false);
			} else if (term instanceof ColorClassTerm || term instanceof DomainTerm) {
				/*
				 * The ColorClassTerm only makes sense in combination with the DomainTerm.
				 * Apparently there is no equivalent for the DomainTerm in PNML.
				 * renderEqualityDomainTermEqColorClassTerm compensates for that.
				 * This line thus cannot be reached.
				 */
				throw new AssertionError("Unreachable statement");
			} else {
				throw new UnsupportedOperationException("This predicate term (" + term + ") cannot yet be rendered to pnml");
			}
		}

		private Element renderEqualityDomainTermEqColorClassTerm(BasicPredicate<?> equality) {
			if (!this.config.allowDomainTerm) {
				throw new UnsupportedOperationException("Rendering DomainTerms is disabled.");
			}
			DomainTerm domainTerm;
			ColorClassTerm colorClassTerm;
			{
				IPredicateTerm<?> left = equality.getLeftOperand();
				IPredicateTerm<?> right = equality.getRightOperand();
				if (left instanceof DomainTerm && right instanceof ColorClassTerm) {
					domainTerm = (DomainTerm) left;
					colorClassTerm = (ColorClassTerm) right;
				} else if (left instanceof ColorClassTerm && right instanceof DomainTerm) {
					domainTerm = (DomainTerm) right;
					colorClassTerm = (ColorClassTerm) left;
				} else {
					throw new IllegalArgumentException("On places DomainTerm and ColorClassTerm must come together");
				}
			}

			Variable variable = domainTerm.getVariables().iterator().next();
			List<Element> equalities = new LinkedList<>();
			for (Color color : getColorsOfClass(colorClassTerm.getClassId())) {
				equalities.add(wrapInElementWithSubterms(equality.getOperator() == BasicPredicate.Operator.EQ ? "equality" : "inequality",
						renderVariableReference(variable),
						renderColorReference(color)
				));
			}
			Iterator<Element> subterms = equalities.iterator();
			return renderNaryPredicate(equality.getOperator() == BasicPredicate.Operator.EQ ? "or" : "and", subterms.next(), subterms);
		}

		private List<Color> getColorsOfClass(String classOrSubClassId) {
			if (game.hasStaticSubclass(classOrSubClassId)) {
				return game.getBasicColorClassOfStaticSubclass(classOrSubClassId)
						.getStaticSubclasses()
						.get(classOrSubClassId)
						.getColors();
			} else {
				return game.getBasicColorClass(classOrSubClassId).getColors();
			}
		}

		private Element renderNaryPredicate(String operator, Element current, Iterator<Element> rest) {
			if (!rest.hasNext()) {
				return current;
			}
			return wrapInElementWithSubterms(operator,
					current,
					renderNaryPredicate(operator, rest.next(), rest)
			);
		}

		private Element renderArc(Flow flow) {
			Element arc = dom.createElement("arc");
			String source = flow.getSource().getId();
			String target = flow.getTarget().getId();
			arc.setAttribute("id", source + "-" + target);
			arc.setAttribute("source", source);
			arc.setAttribute("target", target);
			arc.appendChild(wrapIn("hlinscription", wrapIn("structure", renderArcExpression(game.getArcExpression(flow)))));
			return arc;
		}

		private Element renderArcExpression(ArcExpression arc) {
			var expressions = arc.getExpresssions();
			if (expressions.size() == 1) {
				return renderSingleArcExpressionSubterm(expressions.iterator().next());
			} else if (expressions.size() > 1) {
				Element add = dom.createElement("add");
				for (var expression : expressions) {
					add.appendChild(wrapInSubterm(renderSingleArcExpressionSubterm(expression)));
				}
				return add;
			} else /* expressions.size() == 0 */ {
				// no expression sometimes means dot variable.
				throw new UnsupportedOperationException("every arc must have at least one arc expression");
			}
		}

		private Element renderSingleArcExpressionSubterm(Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> expression) {
			switch (expression.getFirst()) {
				case VARIABLE:
					return wrapInNumberof(renderVariableReference((Variable) expression.getSecond()));
				case PREDECESSOR:
					return wrapInNumberof(renderNeighbourTerm(((PredecessorTerm) expression.getSecond()).getVariable(), true));
				case SUCCESSOR:
					return wrapInNumberof(renderNeighbourTerm(((SuccessorTerm) expression.getSecond()).getVariable(), false));
				case COLORCLASS:
					return wrapInNumberof(renderColorClassTerm(((ColorClassTerm) expression.getSecond())));
				case SETMINUS:
					/* this is not wrapped in a numberof term */
					return renderSetMinusTerm(((SetMinusTerm) expression.getSecond()));
				case TUPLE:
					/* the potential numberof term is is rendered by the called method */
					return renderArcTuple((ArcTuple) expression.getSecond());
				default:
					throw new UnsupportedOperationException("unknown arc expression type");
			}
		}

		private Element renderArcTuple(ArcTuple tuple) {
			var tupleElements = tuple.getValues();

			/*
			 * first: the things that are added up to make the set
			 * second: the things that are subtracted
			 */
			List<Pair<List<Element>, List<Element>>> plusMinus = new LinkedList<>();

			boolean subtracting = false;
			for (var tupleElement : tupleElements) {
				switch (tupleElement.getFirst()) {
					case VARIABLE:
						Element variableReference = renderVariableReference((Variable) tupleElement.getSecond());
						plusMinus.add(new Pair<>(
								List.of(variableReference),
								List.of(variableReference)
						));
						break;
					case PREDECESSOR:
						Element predecessorReference = renderNeighbourTerm(((PredecessorTerm) tupleElement.getSecond()).getVariable(), true);
						plusMinus.add(new Pair<>(
								List.of(predecessorReference),
								List.of(predecessorReference)
						));
						break;
					case SUCCESSOR:
						Element successorReference = renderNeighbourTerm(((SuccessorTerm) tupleElement.getSecond()).getVariable(), false);
						plusMinus.add(new Pair<>(
								List.of(successorReference),
								List.of(successorReference)
						));
						break;
					case COLORCLASS: {
						List<Element> colors = getColorsOfClass(((ColorClassTerm) tupleElement.getSecond()).getClassId()).stream()
								.map(this::renderColorReference)
								.collect(Collectors.toUnmodifiableList());
						plusMinus.add(new Pair<>(
								colors,
								colors
						));
						break;
					}
					case SETMINUS: {
						SetMinusTerm setMinus = (SetMinusTerm) tupleElement.getSecond();
						plusMinus.add(new Pair<>(
								getColorsOfClass(setMinus.getClazz().getClassId()).stream()
										.map(this::renderColorReference)
										.collect(Collectors.toUnmodifiableList()),
								setMinus.getVariables().stream()
										.map(this::renderVariableReference)
										.collect(Collectors.toUnmodifiableList()))
						);
						subtracting = true;
						break;
					}
					default:
						throw new UnsupportedOperationException("unknown arc tuple expression type");
				}
			}


			boolean adding = plusMinus.stream()
					.anyMatch(pm -> pm.getFirst().size() > 1);

			if (!adding && !subtracting) {
				Element tupleDomElement = dom.createElement("tuple");
				for (Pair<List<Element>, List<Element>> element : plusMinus) {
					tupleDomElement.appendChild(wrapInSubterm(element.getFirst().get(0)));
				}
				return wrapInNumberof(tupleDomElement);
			} else {
				CartesianProduct<Element> toAdd = new CartesianProduct<>(
						plusMinus.stream()
								.map(Pair::getFirst)
								.collect(Collectors.toUnmodifiableList())
				);
				Element add = dom.createElement("add");
				for (List<Element> elements : toAdd) {
					Element tupleDomElement = dom.createElement("tuple");
					for (Element element : elements) {
						tupleDomElement.appendChild(wrapInSubterm((Element) element.cloneNode(true)));
					}
					add.appendChild(wrapInSubterm(wrapInNumberof(tupleDomElement)));
				}
				if (!subtracting) {
					return add;
				}

				Element subtract = dom.createElement("subtract");
				subtract.appendChild(wrapInSubterm(add));

				CartesianProduct<Element> toSubtract = new CartesianProduct<>(
						plusMinus.stream()
								.map(Pair::getSecond)
								.collect(Collectors.toUnmodifiableList())
				);
				for (List<Element> elements : toSubtract) {
					Element tupleDomElement = dom.createElement("tuple");
					for (Element element : elements) {
						tupleDomElement.appendChild(wrapInSubterm((Element) element.cloneNode(true)));
					}
					subtract.appendChild(wrapInSubterm(wrapInNumberof(tupleDomElement)));
				}

				return subtract;
			}
		}

		private Element renderNeighbourTerm(Variable var, boolean predecessor) {
			return wrapInElementWithSubterms(predecessor ? "predecessor" : "successor", renderVariableReference(var));
		}

		private Element renderColorClassTerm(ColorClassTerm term) {
			return wrapIn("all", renderColorClassReference(term.getClassId()));
		}

		private Element renderSetMinusTerm(SetMinusTerm term) {
			Element subtract = dom.createElement("subtract");
			subtract.appendChild(wrapInSubterm(wrapInNumberof(renderColorClassTerm(term.getClazz()))));
			for (Variable variable : term.getVariables()) {
				subtract.appendChild(wrapInSubterm(wrapInNumberof(renderVariableReference(variable))));
			}

			return subtract;
		}

		private Element renderVariableReference(String variableId) {
			return renderElementWithAttribute("variable", "refvariable", variableId);
		}

		private Element renderVariableReference(Variable variable) {
			return renderVariableReference(variable.getName());
		}

		private Element renderColorReference(String colorId) {
			return renderElementWithAttribute("useroperator", "declaration", colorId);
		}

		private Element renderColorReference(Color color) {
			return renderColorReference(color.getId());
		}

		private Element renderColorClassReference(String colorClassId) {
			return renderElementWithAttribute("usersort", "declaration", colorClassId);
		}

		private Element renderColorClassReference(BasicColorClass colorClass) {
			return renderColorClassReference(colorClass.getId());
		}

		private Element renderColorClassReference(StaticColorClass colorClass) {
			return renderColorClassReference(colorClass.getId());
		}

		private Element renderElementWithAttribute(String tagName, String attributeName, String attributeValue) {
			Element Element = dom.createElement(tagName);
			Element.setAttribute(attributeName, attributeValue);
			return Element;
		}

		private Element wrapInNumberof(Element element) {
			return wrapInElementWithSubterms("numberof",
					renderNumberconstant(1),
					element
			);
		}

		private Element renderNumberconstant(long value) {
			Element numberconstant = dom.createElement("numberconstant");
			numberconstant.setAttribute("value", Long.toString(value));
			Element natural = dom.createElement("natural");

			numberconstant.appendChild(natural);
			return numberconstant;
		}

		private Element renderName(String value) {
			Element text = dom.createElement("text");
			text.setTextContent(value);
			return wrapIn("name", text);
		}

		private Optional<Element> renderGraphics(Node node) {
			if (!PetriNetExtensionHandler.hasXCoord(node) || !PetriNetExtensionHandler.hasYCoord(node)) {
				return Optional.empty();
			}
			Element position = dom.createElement("position");
			position.setAttribute("x", Double.toString(PetriNetExtensionHandler.getXCoord(node)));
			position.setAttribute("y", Double.toString(PetriNetExtensionHandler.getYCoord(node)));
			return Optional.of(wrapIn("graphics", position));
		}

		/* a one-tuple's name is just the only color class name */
		private String tupleName(ColorDomain domain) {
			return String.join("*", domain);
		}

		private Element wrapIn(String tagName, Element element) {
			Element subterm = dom.createElement(tagName);
			subterm.appendChild(element);
			return subterm;
		}

		private Element wrapInSubterm(Element element) {
			return wrapIn("subterm", element);
		}

		private Element wrapInElementWithSubterms(String tagName, Element... subtermContent) {
			Element element = dom.createElement(tagName);
			for (Element content : subtermContent) {
				element.appendChild(wrapInSubterm(content));
			}
			return element;
		}

	}

	@Override
	public String getFormat() {
		return "pnml";
	}

	@Override
	public List<String> getFileExtensions() {
		return List.of("pnml", "xml");
	}

	@Override
	public void render(HLPetriGame game, Writer writer) throws RenderException {
		try {
			Document dom = new Renderer(this.config, game).dom;
			Transformer tr = TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD, "xml");
			tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

			// send DOM to file
			tr.transform(new DOMSource(dom), new StreamResult(writer));
		} catch (TransformerException | ParserConfigurationException e) {
			throw new RenderException(e);
		}
	}
}
