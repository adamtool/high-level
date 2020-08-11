package uniolunisaar.adam.pnml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.Parser;
import uniol.apt.io.parser.impl.AbstractParser;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColorTokens;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTupleElementType;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcType;
import uniolunisaar.adam.ds.highlevel.arcexpressions.SetMinusTerm;
import uniolunisaar.adam.ds.highlevel.predicate.BasicPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.BinaryPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.UnaryPredicate;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.PredecessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.AdamExtensions;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Parser for symmetric set based nets described by pnml.
 *
 * @author Lukas Panneke
 */
public class SymmetricPnmlParser extends AbstractParser<HLPetriGame> implements Parser<HLPetriGame> {

	private static final Logger log = Logger.getInstance();

	public static final String PNML_XMLNS = "http://www.pnml.org/version-2009/grammar/pnml";
	public static final String NET_TYPE = "http://www.pnml.org/version-2009/grammar/symmetricnet";
	public static final String EXTENSION_KEY_NAME = AdamExtensions.label.name();

	private static final Color DOT = new Color("dot");
	private static final ColorToken DOT_CONSTANT_TOKEN = new ColorToken(DOT);
	private static final Variable DOT_CONSTANT_VARIABLE = new Variable("dotconstant");

	private static class Parser {

		public boolean consistencyCheck = true;
		public boolean ignoreNumberofIfOmitted = false;

		private HLPetriGame game;
		private Map<String, String> safeIdMap;
		private int idCounter;

		/**
		 * Parses symmetric nets in PNML notation.
		 */
		public HLPetriGame parse(InputStream is) throws ParseException, IOException {
			Element net = getFirstNetElement(is);
			return parseIsoPNML(net);
		}

		/**
		 * Parses PNML that conforms to the standard ISO/IEC 15909.
		 *
		 * @param net pnml net element
		 * @return parsed Petri net
		 */
		private HLPetriGame parseIsoPNML(Element net) throws ParseException {
			init(net);
			parseDeclarations(net);
			parsePagesForNodes(net);
			parsePagesForEdges(net);
			return this.game;
		}

		/**
		 * Parses PNML output that has no page tags.
		 *
		 * @param net pnml net element
		 * @return parsed Petri net
		 */
		private HLPetriGame parseUnpagedPNML(Element net) throws ParseException {
			init(net);
			createNodes(net);
			createEdges(net);
			return this.game;
		}

		private void init(Element net) throws ParseException {
			String pnName = getAttribute(net, "id");
			this.game = new HLPetriGame(pnName);
			this.safeIdMap = new HashMap<>();
			this.idCounter = 0;
			checkNetType(net);
		}

		/**
		 * Returns the first net element found.
		 *
		 * @param is input stream to read PNML from
		 * @return first <net> element
		 */
		private Element getFirstNetElement(InputStream is) throws ParseException, IOException {
			Document doc = getDocument(is);
			Element root = getPnmlRoot(doc);

			// Only consider the first net that is encountered
			return getChildElement(root, "net");
		}

		/**
		 * Checks that the given net element has an attribute "type"
		 * whose value matches the expected type. If it does not
		 * match an exception is thrown.
		 */
		private void checkNetType(Element net) throws ParseException {
			String type = net.getAttribute("type");
			if (!NET_TYPE.equals(type)) {
				String msg = String.format("Expected net type '%s' but found '%s'", NET_TYPE, type);
				throw new ParseException(msg);
			}
		}

		/**
		 * Parses an xml file into a DOM model.
		 */
		private Document getDocument(InputStream is) throws ParseException, IOException {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new ParseException("Internal error while parsing the document", e);
			}
			builder.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException e) {
					// Silently ignore warnings
				}

				@Override
				public void error(SAXParseException e) throws SAXException {
					throw e;
				}

				@Override
				public void fatalError(SAXParseException e) throws SAXException {
					throw e;
				}
			});
			try {
				return builder.parse(is);
			} catch (SAXException e) {
				throw new ParseException("Could not parse PNML XML file", e);
			}
		}

		/**
		 * Returns the root pnml element.
		 */
		private Element getPnmlRoot(Document doc) throws ParseException {
			Element root = doc.getDocumentElement();
			if (!root.getNodeName().equals("pnml")) {
				throw new ParseException("Root element isn't <pnml>");
			}
			return root;
		}

		private void parseDeclarations(Element net) throws ParseException {
			log.addMessage("Begin <declaration>");
			Element declaration = getChildElement(net, "declaration");
			Element structure = getChildElement(declaration, "structure");
			Element declarations = getChildElement(structure, "declarations");
			List<Element> namedsorts = getChildElements(declarations, "namedsort");
			for (Element namedsort : namedsorts) {
				String id = toSafeIdentifier(getAttribute(namedsort, "id"));
				log.addMessage("Begin <namedsort id=\"" + id + "\">");
				NodeList childNodes = namedsort.getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++) {
					if (childNodes.getLength() == 0) {
						throw new ParseException("<namedsort> has no children");
					}
					switch (childNodes.item(i).getNodeName()) {
						case "finiteenumeration":
							parseEnumeration(namedsort, false);
							break;
						case "cyclicenumeration":
							parseEnumeration(namedsort, true);
							break;
						case "finiteintrange":
							parseFiniteintrange(namedsort);
							break;
						case "productsort":
							parseProductsort(namedsort);
							break;
						case "dot":
							/*
							 * Dot is a built-in sort.
							 * http://www.pnml.org/version-2009/grammar/dots.rng
							 */
							game.createBasicColorClass("dot", false, "dot");
						default:
							break;
					}
				}
				log.addMessage(" End  <namedsort id=\"" + id + "\">");
			}
			List<Element> partitions = getChildElements(declarations, "partition");
			for (Element partition : partitions) {
				parsePartition(partition);
			}
			this.colorClassPrototypes.forEach((id, prototype) -> {
				if (game.getBasicColorClass(id) == null) {
					prototype.create(game);
				}
			});

			// variabledecl is ignored

			log.addMessage(" End  <declaration>");
		}

		/**
		 * Stores the information required for creating basic color classes later.
		 * <p>
		 * We first parse all color classes,
		 * and then the partitions.
		 * But for partitions we want to use the id of the color class they are partitioning.
		 * Since we cannot overwrite an already created color class
		 * the actual creation of them in the game is postponed
		 * until all partitions have been created.
		 */
		private final Map<String, ColorClassPrototype> colorClassPrototypes = new HashMap<>();

		private void addColorClassPrototype(String id, boolean ordered, List<String> colors) throws ParseException {
			if (this.colorClassPrototypes.containsKey(id) || game.getBasicColorClass(id) != null) {
				throw new ParseException("Color class " + id + " already exists");
			}
			this.colorClassPrototypes.put(id, new ColorClassPrototype(id, ordered, colors));
		}

		private static final class ColorClassPrototype {
			public String id;
			public boolean ordered;
			public List<String> colors;

			public ColorClassPrototype(String id, boolean ordered, List<String> colors) {
				this.id = id;
				this.ordered = ordered;
				this.colors = colors;
			}

			public void create(HLPetriGame game) {
				List<Color> actualColors = this.colors.stream()
						.map(Color::new)
						.collect(Collectors.toUnmodifiableList());
				game.createBasicColorClass(this.id, this.ordered, actualColors);
			}
		}

		/**
		 * Partition an already prepared color class.
		 * <p>
		 * The ID of the partition element is ignored.
		 * Instead the ID of the partitioned color class is used.
		 */
		private void parsePartition(Element partition) throws ParseException {
			String id = toSafeIdentifier(getAttribute(partition, "id"));
			log.addMessage("Begin <partition id=\"" + id + "\">");
			Element usersort = getChildElement(partition, "usersort");
			String partitionedColorClassId = toSafeIdentifier(getAttribute(usersort, "declaration"));
			ColorClassPrototype partitionedColorClass = colorClassPrototypes.get(partitionedColorClassId);
			if (partitionedColorClass == null) {
				throw new ParseException("<partition id=\"" + id + "\"> reefers to color class "
						+ partitionedColorClassId + ", which does not exist");
			}
			List<Element> partitionelements = getChildElements(partition, "partitionelement");
			List<Pair<String, String[]>> partitionedClasses = new ArrayList<>();
			for (Element partitionelement : partitionelements) {
				String classId = toSafeIdentifier(getAttribute(partitionelement, "id"));
				List<Element> useroperators = getChildElements(partitionelement, "useroperator");
				List<String> colors = new ArrayList<>();
				for (Element useroperator : useroperators) {
					colors.add(toSafeIdentifier(getAttribute(useroperator, "declaration")));
				}
				partitionedClasses.add(new Pair<>(classId, colors.toArray(String[]::new)));
			}

			if (consistencyCheck) {
				Set<Map.Entry<String, Long>> duplicates = partitionedClasses.stream()
						.flatMap(pair -> Arrays.stream(pair.getSecond()))
						.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
						.entrySet().stream()
						.filter(entry -> entry.getValue() > 1)
						.collect(Collectors.toSet());
				if (!duplicates.isEmpty()) {
					throw new ParseException(id + " is not a partition because it contains duplicates " + duplicates);
				}

				List<String> partitionedClassIds = partitionedClasses.stream()
						.map(Pair::getSecond)
						.flatMap(Arrays::stream)
						.sorted()
						.collect(Collectors.toList());
				List<String> classIdsToPartition = partitionedColorClass.colors.stream()
						.sorted()
						.collect(Collectors.toList());
				if (!partitionedClassIds.equals(classIdsToPartition)) {
					throw new ParseException(id + " is not a partition because it is not complete");
				}
			}
			game.createBasicColorClassByStaticSubClass(partitionedColorClassId, false, partitionedClasses);
			log.addMessage(" End  <partition id=\"" + id + "\">");
		}

		private void parseFiniteintrange(Element namedsort) throws ParseException {
			String id = toSafeIdentifier(getAttribute(namedsort, "id"));
			Element finiteintrange = getChildElement(namedsort, "finiteintrange");
			long start = parseLong(getAttribute(finiteintrange, "start"));
			long end = parseLong(getAttribute(finiteintrange, "end"));
			List<String> range = LongStream.rangeClosed(start, end)
					.mapToObj(Long::toString)
					.collect(Collectors.toList());
			this.addColorClassPrototype(id, true, range);
		}

		private void parseEnumeration(Element namedsort, boolean ordered) throws ParseException {
			String id = toSafeIdentifier(getAttribute(namedsort, "id"));
			Element enumeration = getChildElement(namedsort, ordered ? "cyclicenumeration" : "finiteenumeration");
			List<Element> feconstants = getChildElements(enumeration, "feconstant");

			String[] constantIds = new String[feconstants.size()];
			for (int i = 0; i < feconstants.size(); i++) {
				constantIds[i] = toSafeIdentifier(getAttribute(feconstants.get(i), "id"));
			}
			this.addColorClassPrototype(id, ordered, Arrays.asList(constantIds));
		}

		private final Map<String, String[]> products = new HashMap<>();

		/* cross product */
		private void parseProductsort(Element namedsort) throws ParseException {
			String id = toSafeIdentifier(getAttribute(namedsort, "id"));
			Element productsort = getChildElement(namedsort, "productsort");
			List<Element> usersorts = getChildElements(productsort, "usersort");
			if (usersorts.size() < 2) {
				throw new ParseException("Product must have at least 2 Factors");
			}
			List<String> referenced = new ArrayList<>();
			for (Element usersort : usersorts) {
				referenced.add(toSafeIdentifier(getAttribute(usersort, "declaration")));
			}
			products.put(id, referenced.toArray(String[]::new));
		}

		/**
		 * Recursively parses all pages contained in the given element
		 * and creates nodes for each place or transition encountered.
		 *
		 * @param parent net or (sub-)page element
		 */
		private void parsePagesForNodes(Element parent) throws ParseException {
			List<Element> childPages = getChildElements(parent, "page");
			for (Element page : childPages) {
				createNodes(page);
				parsePagesForNodes(page);
			}
		}

		/**
		 * Recursively parses all pages contained in the given element
		 * and creates edges for each arc encountered.
		 *
		 * @param parent net or (sub-)page element
		 */
		private void parsePagesForEdges(Element parent) throws ParseException {
			List<Element> childPages = getChildElements(parent, "page");
			for (Element page : childPages) {
				createEdges(page);
				parsePagesForEdges(page);
			}
		}

		/**
		 * Returns a list of all child elements with the given tag name.
		 *
		 * @param parent parent element
		 * @param tagName child tag name
		 * @return list of child elements
		 */
		private List<Element> getChildElements(Element parent, String tagName) {
			List<Element> elements = new ArrayList<>();
			NodeList children = parent.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tagName)) {
					elements.add((Element) child);
				}
			}
			return elements;
		}

		/**
		 * Returns a single child element with the given name.
		 *
		 * @param parent parent element
		 * @param tagName child tag name
		 * @return the child element
		 * @throws ParseException thrown when not exactly one child with the
		 * given name is found
		 */
		private Element getChildElement(Element parent, String tagName) throws ParseException {
			List<Element> elements = getChildElements(parent, tagName);
			if (elements.size() == 1) {
				return elements.get(0);
			} else {
				throw new ParseException(
						String.format("Expected single child <%s> of parent <%s> but found %d",
								tagName, parent.getTagName(), elements.size()));
			}
		}

		/**
		 * Returns a single child element with the given name or null if
		 * it does not exist.
		 *
		 * @param parent parent element
		 * @param tagName child tag name
		 * @return the child element
		 */
		private Element getOptionalChildElement(Element parent, String tagName) {
			List<Element> elements = getChildElements(parent, tagName);
			if (elements.size() == 1) {
				return elements.get(0);
			} else {
				return null;
			}
		}

		/**
		 * Returns an attribute's value.
		 *
		 * @param elem element of which the attribute is to be
		 * returned
		 * @param attrName attribute name
		 * @return attribute value
		 * @throws ParseException thrown when the attribute does not exist
		 */
		private String getAttribute(Element elem, String attrName) throws ParseException {
			if (!elem.hasAttribute(attrName)) {
				throw new ParseException("Element <" + elem.getTagName() + "> does not have attribute "
						+ attrName);
			}
			return elem.getAttribute(attrName);
		}

		/**
		 * Returns the text contents of an element.
		 *
		 * @param element parent element
		 * @return text enclosed by parent element
		 * @throws ParseException thrown when there are other elements inside
		 * the parent element
		 */
		private String getText(Element element) throws ParseException {
			Node child = element.getFirstChild();
			if (child == null || child.getNextSibling() != null)
				throw new ParseException("Trying to get text inside of <" + element.getTagName()
						+ ">, but this element has multiple children");
			if (!(child instanceof Text))
				throw new ParseException("Trying to get text inside of <" + element.getTagName()
						+ ">, but child isn't text");
			return child.getNodeValue();
		}

		/**
		 * Creates places and transitions for all elements found in the
		 * given page.
		 *
		 * @param page page element
		 */
		private void createNodes(Element page) throws ParseException {
			log.addMessage("Begin places");
			List<Element> places = getChildElements(page, "place");
			for (Element place : places) {
				createPlace(place);
			}
			log.addMessage(" End  places");

			log.addMessage("Begin transitions");
			List<Element> transitions = getChildElements(page, "transition");
			for (Element transition : transitions) {
				createTransition(transition);
			}
			log.addMessage(" End  transitions");
		}

		/**
		 * Creates a place corresponding to the given <place> element.
		 *
		 * @param place place element
		 */
		private void createPlace(Element place) throws ParseException {
			String id = toSafeIdentifier(getAttribute(place, "id"));
			log.addMessage("Begin <place id=\"" + id + "\">");
			String name = parseName(place);
			String typeDeclaration = toSafeIdentifier(getAttribute(getChildElement(getChildElement(getChildElement(place,
					"type"), "structure"), "usersort"), "declaration"));
			String[] type = products.getOrDefault(typeDeclaration, new String[] { typeDeclaration });
			Place pnPlace = game.createSysPlace(id, type);
			parseInitialMarking(place);
			if (name != null) {
				pnPlace.putExtension(EXTENSION_KEY_NAME, name, ExtensionProperty.WRITE_TO_FILE);
			}
			log.addMessage(" End  <place id=\"" + id + "\">");
		}

		/**
		 * Parses and applies the initial marking of a place, if present.
		 */
		private void parseInitialMarking(Element placeElem) throws ParseException {
			String placeId = toSafeIdentifier(getAttribute(placeElem, "id"));
			Place place = game.getPlace(placeId);
			Element initMarkElem = getOptionalChildElement(placeElem, "hlinitialMarking");
			if (initMarkElem == null) {
				return;
			}
			Element structureElem = getChildElement(initMarkElem, "structure");

			Element elem;
			if ((elem = getOptionalChildElement(structureElem, "numberof")) != null) {
				game.setColorTokens(place, parseNumberofPlace(elem));
			} else if ((elem = getOptionalChildElement(structureElem, "add")) != null) {
				List<Element> subterms = getChildElements(elem, "subterm");
				if (subterms.isEmpty()) {
					throw new ParseException("<add> structure of Place " + placeId + " is empty");
				}
				ColorTokens tokens = new ColorTokens();
				for (Element subterm : subterms) {
					tokens.addAll(parseNumberofPlace(getChildElement(subterm, "numberof")));
				}
				game.setColorTokens(place, tokens);
			} else {
				if (ignoreNumberofIfOmitted) {
					game.setColorTokens(place, parseInitialMarkingTerm(structureElem));
				} else {
					throw new ParseException(new UnsupportedOperationException("Place " + placeId + " has an unsupported initial marking structure"));
				}
			}
		}

		private ColorTokens parseNumberofPlace(Element numberofElem) throws ParseException {
			List<Element> subterms = getChildElements(numberofElem, "subterm");
			if (subterms.size() != 2) {
				throw new ParseException("<numberof> must have 2 subterms");
			}
			long amount = parseLong(getAttribute(getChildElement(subterms.get(0), "numberconstant"), "value"));
			if (amount != 1) {
				throw new ParseException("Only set based nets allowed");
			}

			return parseInitialMarkingTerm(subterms.get(1));
		}

		private ColorTokens parseInitialMarkingTerm(Element containingElement) throws ParseException {
			Element elem;
			if ((elem = getOptionalChildElement(containingElement, "useroperator")) != null) {
				String referencedColor = toSafeIdentifier(getAttribute(elem, "declaration"));
				return new ColorTokens(Collections.singleton(new ColorToken(new Color(referencedColor))));
			} else if ((elem = getOptionalChildElement(containingElement, "tuple")) != null) {
				// TODO <all>
				ColorToken token = new ColorToken();
				for (Element subterm : getChildElements(elem, "subterm")) {
					String referenced = toSafeIdentifier(getAttribute(getChildElement(subterm, "useroperator"), "declaration"));
					token.add(new Color(referenced));
				}
				return new ColorTokens(Collections.singleton(token));
			} else if ((elem = getOptionalChildElement(containingElement, "all")) != null) {
				String referencedColorClassId = toSafeIdentifier(getAttribute(getChildElement(elem, "usersort"), "declaration"));
				Set<ColorToken> tokens = game.getBasicColorClass(referencedColorClassId).getColors().stream()
						.map(ColorToken::new)
						.collect(Collectors.toSet());
				return new ColorTokens(tokens);
			} else if (getOptionalChildElement(containingElement, "finiteintrangeconstant") != null) {
				throw new ParseException(new UnsupportedOperationException("<finiteintrangeconstant> is not supported yet."));
			} else if (getOptionalChildElement(containingElement, "dotconstant") != null) {
				/*
				 * DotConstant is a built-in constant for Dot.
				 * http://www.pnml.org/version-2009/grammar/dots.rng
				 */
				return new ColorTokens(Collections.singleton(DOT_CONSTANT_TOKEN));
			} else {
				throw new ParseException(new UnsupportedOperationException("Unknown term in initial marking"));
			}
		}

		/**
		 * Creates a transition corresponding to the given
		 * <transition> element
		 *
		 * @param transition transition element
		 */
		private void createTransition(Element transition) throws ParseException {
			String id = toSafeIdentifier(getAttribute(transition, "id"));
			log.addMessage("Begin <transition id=\"" + id + "\">");
			String name = parseName(transition);
			Transition trans = game.createTransition(id);
			parseCondition(transition);
			if (name != null) {
				trans.putExtension(EXTENSION_KEY_NAME, name, ExtensionProperty.WRITE_TO_FILE);
			}
			log.addMessage(" End  <transition id=\"" + id + "\">");
		}

		private void parseCondition(Element transitionElem) throws ParseException {
			String placeId = toSafeIdentifier(getAttribute(transitionElem, "id"));
			Transition transition = game.getTransition(placeId);
			Element conditionElem = getOptionalChildElement(transitionElem, "condition");
			if (conditionElem == null) {
				return;
			}
			game.setPredicate(transition, parsePredicate(getChildElement(conditionElem, "structure")));
		}

		private IPredicate parsePredicate(Element termElem) throws ParseException {
			Element elem;
			if ((elem = getOptionalChildElement(termElem, "and")) != null) {
				return parseBinaryPredicate(elem, BinaryPredicate.Operator.AND);
			} else if ((elem = getOptionalChildElement(termElem, "or")) != null) {
				return parseBinaryPredicate(elem, BinaryPredicate.Operator.OR);
			} else if ((elem = getOptionalChildElement(termElem, "imply")) != null) {
				return parseBinaryPredicate(elem, BinaryPredicate.Operator.IMP);
			} else if ((elem = getOptionalChildElement(termElem, "not")) != null) {
				return parseUnaryPredicate(elem, UnaryPredicate.Operator.NEG);
			} else if ((elem = getOptionalChildElement(termElem, "equality")) != null) {
				return parseEqualityPredicate(elem, BasicPredicate.Operator.EQ);
			} else if ((elem = getOptionalChildElement(termElem, "inequality")) != null) {
				return parseEqualityPredicate(elem, BasicPredicate.Operator.NEQ);
			} else {
				throw new ParseException(new UnsupportedOperationException("Unknown condition operator"));
			}
		}

		private IPredicate parseUnaryPredicate(Element unaryOperatorElement, UnaryPredicate.Operator operator) throws ParseException {
			List<Element> subterms = getChildElements(unaryOperatorElement, "subterm");
			if (subterms.size() != 1) {
				throw new ParseException("<" + unaryOperatorElement.getNodeName() + "> is a unary operator");
			}
			return new UnaryPredicate(operator, parsePredicate(subterms.get(0)));
		}

		private IPredicate parseBinaryPredicate(Element binaryOperatorElement, BinaryPredicate.Operator operator) throws ParseException {
			List<Element> subterms = getChildElements(binaryOperatorElement, "subterm");
			if (subterms.size() != 2) {
				throw new ParseException("<" + binaryOperatorElement.getNodeName() + "> is a binary operator");
			}
			return new BinaryPredicate(parsePredicate(subterms.get(0)), operator, parsePredicate(subterms.get(1)));
		}

		private IPredicate parseEqualityPredicate(Element equalityOperatorElement, BasicPredicate.Operator operator) throws ParseException {
			List<Element> subterms = getChildElements(equalityOperatorElement, "subterm");
			if (subterms.size() != 2) {
				throw new ParseException("<" + equalityOperatorElement.getNodeName() + "> is a binary operator");
			}
			/* first check if its equality on variables */
			Element v1Elem = getOptionalChildElement(subterms.get(0), "variable");
			if (v1Elem != null) {
				String v1 = toSafeIdentifier(getAttribute(v1Elem, "refvariable"));
				Element v2Elem = getOptionalChildElement(subterms.get(1), "variable");
				if (v2Elem != null) {
					String v2 = toSafeIdentifier(getAttribute(v2Elem, "refvariable"));
					return new BasicPredicate<>(new Variable(v1), operator, new Variable(v2));
				}
				Element c2Elem = getOptionalChildElement(subterms.get(1), "useroperator");
				if (c2Elem != null) {
					/*
					 * variable == color is not supported,
					 * because this would destroy the symmetric behavior.
					 * To allow this
					 * one has to put each of the colors
					 * appearing as constants in a separate subclass
					 * and this can then be checked
					 * with the variable\in\domain term.
					 */
					throw new ParseException(new UnsupportedOperationException("Equality between variable and color breaks symmetries"));
				}

			}
			throw new ParseException(new UnsupportedOperationException("Unknown type of equality"));
		}

		/**
		 * Creates arcs for all elements found in the given page.
		 *
		 * @param page page element
		 */
		private void createEdges(Element page) throws ParseException {
			log.addMessage("Begin arcs");
			List<Element> arcs = getChildElements(page, "arc");
			for (Element arc : arcs) {
				createArc(arc);
			}
			log.addMessage(" End  arcs");
		}

		/**
		 * Creates an arc for the given arc element.
		 */
		private void createArc(Element arc) throws ParseException {
			String id = toSafeIdentifier(getAttribute(arc, "id"));
			String sourceId = toSafeIdentifier(getAttribute(arc, "source"));
			String targetId = toSafeIdentifier(getAttribute(arc, "target"));
			log.addMessage("Begin <arc id=\"" + id + "\">");

			Flow flow = game.createFlow(game.getNode(sourceId), game.getNode(targetId),
					parseArcInscription(getChildElement(getChildElement(arc,
							"hlinscription"), "structure")));

			String name = parseName(arc);
			if (name != null) {
				flow.putExtension(EXTENSION_KEY_NAME, name, ExtensionProperty.WRITE_TO_FILE);
			}
			log.addMessage(" End  <arc id=\"" + id + "\">");
		}

		private ArcExpression parseArcInscription(Element structure) throws ParseException {
			Element elem;
			if ((elem = getOptionalChildElement(structure, "numberof")) != null) {
				return addToArcExpression(new ArcExpression(), parseNumberofArc(elem));
			} else if ((elem = getOptionalChildElement(structure, "add")) != null) {
				ArcExpression arcExpression = new ArcExpression();
				for (Element subterm : getChildElements(elem, "subterm")) {
					addToArcExpression(arcExpression, parseNumberofArc(getChildElement(subterm, "numberof")));
				}
				return arcExpression;
			} else {
				if (ignoreNumberofIfOmitted) {
					return addToArcExpression(new ArcExpression(), parseArcTerm(structure));
				} else {
					throw new ParseException(new UnsupportedOperationException("Unknown root structure of arc inscription"));
				}
			}
		}

		/**
		 * Parse the numberof tag of an arc's hlinscription structure.
		 */
		private Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> parseNumberofArc(Element numberofElem) throws ParseException {
			List<Element> subterms = getChildElements(numberofElem, "subterm");
			if (subterms.size() != 2) {
				throw new ParseException("<numberof> must have 2 subterms, found " + subterms.size());
			}
			if (consistencyCheck) {
				long amount = parseLong(getAttribute(getChildElement(subterms.get(0), "numberconstant"), "value"));
				if (amount != 1) {
					throw new ParseException("Expected 1, but got " + amount + " as multiplicity for arc expression.",
							new UnsupportedOperationException("We only support set based nets."));
				}
			}
			return parseArcTerm(subterms.get(1));
		}

		private Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> parseArcTerm(Element containingElement) throws ParseException {
			Element elem;
			if ((elem = getOptionalChildElement(containingElement, "variable")) != null) {
				return new Pair<>(IArcTerm.Sort.VARIABLE, parseVariableArcTerm(elem));
			} else if ((elem = getOptionalChildElement(containingElement, "predecessor")) != null) {
				return new Pair<>(IArcTerm.Sort.PREDECESSOR, parsePredecessorArcTerm(elem));
			} else if ((elem = getOptionalChildElement(containingElement, "successor")) != null) {
				return new Pair<>(IArcTerm.Sort.SUCCESSOR, parseSuccessorArcTerm(elem));
			} else if ((elem = getOptionalChildElement(containingElement, "all")) != null) {
				return new Pair<>(IArcTerm.Sort.COLORCLASS, parseColorClassArcTerm(elem));
			} else if ((elem = getOptionalChildElement(containingElement, "tuple")) != null) {
				return new Pair<>(IArcTerm.Sort.TUPLE, parseArcTuple(elem));
			} else if (getOptionalChildElement(containingElement, "dotconstant") != null) {
				return new Pair<>(IArcTerm.Sort.VARIABLE, DOT_CONSTANT_VARIABLE);
			} else {
				// TODO setminus
				throw new ParseException(new UnsupportedOperationException("Unknown arc expression type"));
			}
		}

		private ArcExpression addToArcExpression(ArcExpression expression, Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> term) throws ParseException {
			switch (term.getFirst()) {
				case VARIABLE:
					expression.add((Variable) term.getSecond());
					break;
				case SUCCESSOR:
					expression.add((SuccessorTerm) term.getSecond());
					break;
				case PREDECESSOR:
					expression.add((PredecessorTerm) term.getSecond());
					break;
				case COLORCLASS:
					expression.add((ColorClassTerm) term.getSecond());
					break;
				case SETMINUS:
					expression.add((SetMinusTerm) term.getSecond());
					break;
				case TUPLE:
					expression.add((ArcTuple) term.getSecond());
					break;
				default:
					throw new ParseException(new UnsupportedOperationException("An arc term of type " + term.getFirst() + " was parsed, but is not supported yet."));
			}
			return expression;
		}

		private Variable parseVariableArcTerm(Element variableElement) throws ParseException {
			String referenced = toSafeIdentifier(getAttribute(variableElement, "refvariable"));
			return new Variable(referenced);
		}

		private PredecessorTerm parsePredecessorArcTerm(Element predecessorElement) throws ParseException {
			String referenced = toSafeIdentifier(getAttribute(getChildElement(getChildElement(predecessorElement,
					"subterm"), "variable"), "refvariable"));
			return new PredecessorTerm(new Variable(referenced), game);
		}

		private SuccessorTerm parseSuccessorArcTerm(Element successorElement) throws ParseException {
			String referenced = toSafeIdentifier(getAttribute(getChildElement(getChildElement(successorElement,
					"subterm"), "variable"), "refvariable"));
			return new SuccessorTerm(new Variable(referenced), game);
		}

		private ColorClassTerm parseColorClassArcTerm(Element allElement) throws ParseException {
			String referenced = toSafeIdentifier(getAttribute(getChildElement(allElement,
					"usersort"), "declaration"));
			return new ColorClassTerm(referenced);
		}

		private ArcTuple parseArcTuple(Element tupleElement) throws ParseException {
			List<Element> subterms = getChildElements(tupleElement, "subterm");
			ArcTuple arcTuple = new ArcTuple();
			for (Element subterm : subterms) {
				addToArcTuple(arcTuple, parseArcTupleTerm(subterm));
			}
			return arcTuple;
		}

		private ArcTuple addToArcTuple(ArcTuple arcTuple, Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>> term) throws ParseException {
			switch (term.getFirst()) {
				case VARIABLE:
					arcTuple.add((Variable) term.getSecond());
					break;
				case SUCCESSOR:
					arcTuple.add((SuccessorTerm) term.getSecond());
					break;
				case PREDECESSOR:
					arcTuple.add((PredecessorTerm) term.getSecond());
					break;
				case COLORCLASS:
					arcTuple.add((ColorClassTerm) term.getSecond());
					break;
				case SETMINUS:
					arcTuple.add((SetMinusTerm) term.getSecond());
					break;
				default:
					throw new ParseException(new UnsupportedOperationException("An arc tuple of type " + term.getFirst() + " was parsed, but is not supported yet."));
			}
			return arcTuple;
		}

		private Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>> parseArcTupleTerm(Element containingElement) throws ParseException {
			Element elem;
			if ((elem = getOptionalChildElement(containingElement, "variable")) != null) {
				return new Pair<>(IArcTupleElement.Sort.VARIABLE, parseVariableArcTerm(elem));
			} else if ((elem = getOptionalChildElement(containingElement, "predecessor")) != null) {
				return new Pair<>(IArcTupleElement.Sort.PREDECESSOR, parsePredecessorArcTerm(elem));
			} else if ((elem = getOptionalChildElement(containingElement, "successor")) != null) {
				return new Pair<>(IArcTupleElement.Sort.SUCCESSOR, parseSuccessorArcTerm(elem));
			} else {
				// TODO colorclass and setminus
				throw new ParseException(new UnsupportedOperationException("Unknown arc tuple expression type"));
			}
		}

		/**
		 * Parses the name of a "basicobject" element like place,
		 * transition or arc.
		 *
		 * @param elem "basicobject" element
		 * @return the name or null if none exists
		 */
		private String parseName(Element elem) throws ParseException {
			Element nameElem = getOptionalChildElement(elem, "name");
			if (nameElem == null) {
				return null;
			} else {
				Element textElem = getChildElement(nameElem, "text");
				return getText(textElem);
			}
		}

		/**
		 * Parses a long.
		 *
		 * @param str string to parse
		 * @return long value
		 */
		private long parseLong(String str) throws ParseException {
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
				throw new ParseException("Cannot parse number " + str, e);
			}
		}

		/**
		 * Transforms the given input string to a string that is safe to
		 * use as an identifier in APT.
		 *
		 * @param input input string
		 * @return input string with all illegal characters replaced by
		 * "_"
		 */
		private String toSafeIdentifier(String input) {
			if (safeIdMap.containsKey(input)) {
				return safeIdMap.get(input);
			}

			String key = input;
			// Replace all generally illegal characters
			input = input.replaceAll("[^a-zA-Z0-9_]", "_");
			// Make sure first character is non-numeric
			input = input.replaceAll("^[0-9]", "_");
			// Guarantee uniqueness
			String unique = input;
			while (safeIdMap.containsKey(unique)) {
				unique = input + "_" + idCounter;
				idCounter += 1;
			}

			safeIdMap.put(key, unique);
			return input;
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
	public HLPetriGame parse(InputStream input) throws IOException, ParseException {
		return new Parser().parse(input);
	}
}
