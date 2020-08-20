package uniolunisaar.adam.pnml;

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

	private static final class Renderer {
		private boolean omitTrueCondition = true;
		private boolean allowDomainTerm = true;

		private final Document dom;
		private final HLPetriGame game;

		public Renderer(HLPetriGame game) throws ParserConfigurationException {
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
			Element declaration = dom.createElement("declaration");
			Element structure = dom.createElement("structure");
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

			structure.appendChild(declarations);
			declaration.appendChild(structure);
			return declaration;
		}

		private Element renderPartition(BasicColorClass cc) {
			Element partition = dom.createElement("partition");
			partition.setAttribute("id", cc.getId() + "Partition");
			partition.appendChild(renderUsersortReference(cc.getId()));

			cc.getStaticSubclasses().forEach((id, subclass) -> {
				Element partitionelement = dom.createElement("partitionelement");
				partitionelement.setAttribute("id", id);

				for (Color color : subclass.getColors()) {
					partitionelement.appendChild(renderUseroperatorReference(color.getId()));
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
				productsort.appendChild(renderUsersortReference(factor));
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
						variabledecl.appendChild(renderUsersortReference(nameTypePair.getSecond()));
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
			Element type = dom.createElement("type");
			Element structure = dom.createElement("structure");
			structure.appendChild(renderUsersortReference(tupleName(domain)));

			type.appendChild(structure);
			return type;
		}

		private Element renderInitialMarking(ColorTokens tokens) {
			Element hlinitialMarking = dom.createElement("hlinitialMarking");
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
					Element subterm = dom.createElement("subterm");
					subterm.appendChild(renderColorToken(token));
					add.appendChild(subterm);
				}
				structure.appendChild(add);
			}

			hlinitialMarking.appendChild(structure);
			return hlinitialMarking;
		}

		private Element renderColorToken(ColorToken token) {
			Element numberof = dom.createElement("numberof");
			Element multiplicitySubterm = dom.createElement("subterm");
			multiplicitySubterm.appendChild(renderNumberconstant(1));

			Element typeSubterm = dom.createElement("subterm");
			if (token.size() == 1) {
				typeSubterm.appendChild(renderUseroperatorReference(token.get(0).getId()));
			} else {
				typeSubterm.appendChild(renderTuple(token));
			}

			numberof.appendChild(multiplicitySubterm);
			numberof.appendChild(typeSubterm);
			return numberof;
		}

		private Element renderTuple(ColorToken token) {
			Element tuple = dom.createElement("tuple");
			for (int i = 0; i < token.size(); i++) {
				Element subterm = dom.createElement("subterm");
				subterm.appendChild(renderUseroperatorReference(token.get(i).getId()));

				tuple.appendChild(subterm);
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
			if (!(omitTrueCondition && condition instanceof Constants && condition.equals(Constants.TRUE))) {
				transitionElement.appendChild(renderCondition(condition));
			}

			return transitionElement;
		}

		private Element renderCondition(IPredicate predicate) {
			Element condition = dom.createElement("condition");
			Element text = dom.createElement("text");
			text.setTextContent(predicate.toString());
			Element structure = dom.createElement("structure");
			structure.appendChild(renderPredicate(predicate));

			condition.appendChild(text);
			condition.appendChild(structure);
			return condition;
		}

		private Element renderPredicate(IPredicate predicate) {
			if (predicate instanceof Constants) {
				Constants constant = (Constants) predicate;
				Element booleanconstant = dom.createElement("booleanconstant");
				switch (constant) {
					case FALSE:
						booleanconstant.setAttribute("value", "false");
						break;
					case TRUE:
						booleanconstant.setAttribute("value", "true");
						break;
					default:
						throw new UnsupportedOperationException("Unknown constant: " + predicate);
				}
				return booleanconstant;
			} else if (predicate instanceof UnaryPredicate) {
				UnaryPredicate unary = (UnaryPredicate) predicate;
				switch (unary.getOperator()) {
					case NEG:
						Element not = dom.createElement("not");
						Element subterm = dom.createElement("subterm");
						subterm.appendChild(renderPredicate(unary.getOperand()));

						not.appendChild(subterm);
						return not;
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
				Element operator = dom.createElement(elementName);
				Element left = dom.createElement("subterm");
				Element right = dom.createElement("subterm");
				left.appendChild(renderPredicate(binary.getLeftOperand()));
				right.appendChild(renderPredicate(binary.getRightOperand()));

				operator.appendChild(left);
				operator.appendChild(right);
				return operator;
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
			Element operator = dom.createElement(equality.getOperator() == BasicPredicate.Operator.EQ ? "equality" : "inequality");
			Element left = dom.createElement("subterm");
			Element right = dom.createElement("subterm");
			left.appendChild(renderPredicateTerm(equality.getLeftOperand()));
			right.appendChild(renderPredicateTerm(equality.getRightOperand()));

			operator.appendChild(left);
			operator.appendChild(right);
			return operator;
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
			if (!allowDomainTerm) {
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
					throw new IllegalArgumentException("ON places DomainTerm and ColorClassTerm must come together");
				}
			}

			Variable variable = domainTerm.getVariables().iterator().next();
			String classId = colorClassTerm.getClassId();
			List<Color> colors;
			if (game.hasStaticSubclass(classId)) {
				colors = game.getBasicColorClassOfStaticSubclass(classId)
						.getStaticSubclasses()
						.get(classId)
						.getColors();
			} else {
				colors = game.getBasicColorClass(classId).getColors();
			}

			List<Element> equalities = new LinkedList<>();
			for (Color color : colors) {
				Element operator = dom.createElement(equality.getOperator() == BasicPredicate.Operator.EQ ? "equality" : "inequality");
				Element left = dom.createElement("subterm");
				Element right = dom.createElement("subterm");
				left.appendChild(renderVariableReference(variable));
				right.appendChild(renderUseroperatorReference(color.getId()));

				operator.appendChild(left);
				operator.appendChild(right);
				equalities.add(operator);
			}
			Iterator<Element> subterms = equalities.iterator();
			return renderNaryPredicate(equality.getOperator() == BasicPredicate.Operator.EQ ? "or" : "and", subterms.next(), subterms);
		}

		private Element renderNaryPredicate(String operator, Element current, Iterator<Element> rest) {
			if (!rest.hasNext()) {
				return current;
			}
			Element operatorElement = dom.createElement(operator);
			Element left = dom.createElement("subterm");
			Element right = dom.createElement("subterm");
			left.appendChild(current);
			right.appendChild(renderNaryPredicate(operator, rest.next(), rest));
			operatorElement.appendChild(left);
			operatorElement.appendChild(right);
			return operatorElement;
		}

		private Element renderArc(Flow flow) {
			Element arc = dom.createElement("arc");
			String source = flow.getSource().getId();
			String target = flow.getTarget().getId();
			arc.setAttribute("id", source + "-" + target);
			arc.setAttribute("source", source);
			arc.setAttribute("target", target);

			Element hlinscription = dom.createElement("hlinscription");
			Element structure = dom.createElement("structure");

			structure.appendChild(renderArcExpression(game.getArcExpression(flow)));

			hlinscription.appendChild(structure);
			arc.appendChild(hlinscription);
			return arc;
		}

		private Element renderArcExpression(ArcExpression arc) {
			var expressions = arc.getExpresssions();
			if (expressions.size() == 1) {
				return renderSingleArcExpressionSubterm(expressions.iterator().next());
			} else if (expressions.size() > 1) {
				Element add = dom.createElement("add");
				for (var expression : expressions) {
					Element subterm = dom.createElement("subterm");
					subterm.appendChild(renderSingleArcExpressionSubterm(expression));

					add.appendChild(subterm);
				}
				return add;
			} else /* expressions.size() == 0 */ {
				// no expression sometimes means dot variable.
				throw new UnsupportedOperationException("every arc must have at least one arc expression");
			}
		}

		private Element wrapInNumberof(Element element) {
			Element numberof = dom.createElement("numberof");
			Element multiplicitySubterm = dom.createElement("subterm");
			multiplicitySubterm.appendChild(renderNumberconstant(1));

			Element contentSubterm = dom.createElement("subterm");
			contentSubterm.appendChild(element);

			numberof.appendChild(multiplicitySubterm);
			numberof.appendChild(contentSubterm);
			return numberof;
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
					return wrapInNumberof(renderArcTuple((ArcTuple) expression.getSecond()));
				default:
					throw new UnsupportedOperationException("unknown arc expression type");
			}
		}

		private Element renderArcTuple(ArcTuple tuple) {
			Element tupleDomElement = dom.createElement("tuple");
			var tupleElements = tuple.getValues();
			for (var tupleElement : tupleElements) {
				Element subterm = dom.createElement("subterm");
				switch (tupleElement.getFirst()) {
					case VARIABLE:
						subterm.appendChild(renderVariableReference((Variable) tupleElement.getSecond()));
						break;
					case PREDECESSOR:
						subterm.appendChild(renderNeighbourTerm(((PredecessorTerm) tupleElement.getSecond()).getVariable(), true));
						break;
					case SUCCESSOR:
						subterm.appendChild(renderNeighbourTerm(((SuccessorTerm) tupleElement.getSecond()).getVariable(), false));
						break;
					case COLORCLASS:
						// TODO explicitly enumerate all colors of that colorclass
					case SETMINUS:
						// TODO also enumerate all colors that are not subtracted
						subterm.appendChild(dom.createElement("WIP"));
						break;
					default:
						throw new UnsupportedOperationException("unknown arc tuple expression type");
				}

				tupleDomElement.appendChild(subterm);
			}
			return tupleDomElement;
		}

		private Element renderNeighbourTerm(Variable var, boolean predecessor) {
			Element neighbour = dom.createElement(predecessor ? "predecessor" : "successor");
			Element subterm = dom.createElement("subterm");
			subterm.appendChild(renderVariableReference(var));

			neighbour.appendChild(subterm);
			return neighbour;
		}

		private Element renderColorClassTerm(ColorClassTerm term) {
			Element all = dom.createElement("all");
			all.appendChild(renderUsersortReference(term.getClassId()));
			return all;
		}

		private Element renderSetMinusTerm(SetMinusTerm term) {
			Element subtract = dom.createElement("subtract");
			Element setSubterm = dom.createElement("subterm");
			setSubterm.appendChild(wrapInNumberof(renderColorClassTerm(term.getClazz())));
			for (Variable variable : term.getVariables()) {
				Element minusSubterm = dom.createElement("subterm");
				minusSubterm.appendChild(wrapInNumberof(renderVariableReference(variable)));

				subtract.appendChild(minusSubterm);
			}

			subtract.appendChild(setSubterm);
			return subtract;
		}

		private Element renderUseroperatorReference(String id) {
			Element useroperator = dom.createElement("useroperator");
			useroperator.setAttribute("declaration", id);
			return useroperator;
		}

		private Element renderUsersortReference(String id) {
			Element useroperator = dom.createElement("usersort");
			useroperator.setAttribute("declaration", id);
			return useroperator;
		}

		private Element renderVariableReference(Variable variable) {
			Element variableElement = dom.createElement("variable");
			variableElement.setAttribute("refvariable", variable.getName());
			return variableElement;
		}

		private Element renderNumberconstant(long value) {
			Element numberconstant = dom.createElement("numberconstant");
			numberconstant.setAttribute("value", Long.toString(value));
			Element natural = dom.createElement("natural");

			numberconstant.appendChild(natural);
			return numberconstant;
		}

		private Element renderName(String value) {
			Element name = dom.createElement("name");
			Element text = dom.createElement("text");
			text.setTextContent(value);

			name.appendChild(text);
			return name;
		}

		private Optional<Element> renderGraphics(Node node) {
			if (!PetriNetExtensionHandler.hasXCoord(node) || !PetriNetExtensionHandler.hasYCoord(node)) {
				return Optional.empty();
			}
			double x = PetriNetExtensionHandler.getXCoord(node);
			double y = PetriNetExtensionHandler.getYCoord(node);
			Element graphics = dom.createElement("graphics");
			Element position = dom.createElement("position");
			position.setAttribute("x", Double.toString(x));
			position.setAttribute("y", Double.toString(y));

			graphics.appendChild(position);
			return Optional.of(graphics);
		}

		/* a one-tuple's name is just the only color class name */
		private String tupleName(ColorDomain domain) {
			return String.join("*", domain);
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
			Document dom = new Renderer(game).dom;
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
