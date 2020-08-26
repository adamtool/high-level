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
import uniolunisaar.adam.ds.highlevel.predicate.Constants;
import uniolunisaar.adam.ds.highlevel.predicate.DomainTerm;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicateTerm;
import uniolunisaar.adam.ds.highlevel.predicate.UnaryPredicate;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassType;
import uniolunisaar.adam.ds.highlevel.terms.PredecessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.tools.CartesianProduct;
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
import java.util.stream.StreamSupport;

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

	public static class Configuration {

		/**
		 * Validate for every partition,
		 * that the color class is a disjoint union of all its subclasses.
		 */
		public boolean checkPartitions = true;

		/**
		 * Since we only support set based nets
		 * we can ignore multiplicities.
		 * With this option enabled all multiplicities must be 1.
		 */
		public boolean checkSetBased = true;

		/**
		 * Sometimes the numberof tag is omitted.
		 * Since we only support set based nets
		 * we can infer, that the multiplicity must always be 1.
		 * Thus a omitted numberof tag can be ignored.
		 */
		public boolean ignoreNumberofIfOmitted = true;

		/**
		 * Referencing one color specifically breaks symmetries.
		 * We don't like working with those nets,
		 * because they are expensive to analyse.
		 * <p>
		 * We can simulate explicit color references in terms of the form
		 * variable == color
		 * by creating a subclass for just that color
		 * and then checking if the variables domain is that of the singleton subclass.
		 */
		public boolean allowExplicitlyReferencedColors = true;

		/**
		 * Color classes are ordered,
		 * but the predecessor of the "first" color is the "last" color.
		 * Without a minimum and a maximum you cannot have an ordering relation.
		 * <p>
		 * This option *defines* the color that was created first to be the minimum
		 * and the last created color as the maximum.
		 */
		public boolean allowOrderingRelations = true;
	}

	private final Configuration config;

	public SymmetricPnmlParser() {
		this(new Configuration());
	}

	public SymmetricPnmlParser(Configuration config) {
		this.config = config;
	}

	private static class Parser {

		private final Configuration config;
		private HLPetriGame game;
		private Map<String, String> safeIdMap;
		private int idCounter;

		private Parser(Configuration config) {
			this.config = config;
		}

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
			this.colorClassPrototypes.forEach((id, prototype) -> prototype.createSubclasses(game));
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
			String netId = getAttribute(net, "id");
			this.game = new HLPetriGame(netId);
			String name = parseName(net);
			if (name != null) {
				game.putExtension(EXTENSION_KEY_NAME, name, ExtensionProperty.WRITE_TO_FILE);
			}
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
			/*
			 * at this point all color classes are read,
			 * but the may still be partitioned to allow for color references.
			 */
			this.colorClassPrototypes.forEach((id, prototype) -> prototype.createColorClass(game));

			List<Element> variables = getChildElements(declarations, "variabledecl");
			for (Element variableElement : variables) {
				String variableId = toSafeIdentifier(getAttribute(variableElement, "id"));
				String colorClassId = toSafeIdentifier(getAttribute(getChildElement(variableElement, "usersort"), "declaration"));
				this.variableIdToColorClassId.put(variableId, colorClassId);
			}

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
		 * <p>
		 * BasicColorClasses are created
		 * after all declarations have been parsed.
		 * They are needed for parsing places.
		 * Transitions and arcs are created after places,
		 * but they may produce StaticColorClasses,
		 * to allow for references to a color
		 * via a variable that can only hold that specific color.
		 * Thus creation of StaticColorClasses is postponed
		 * until everything is parsed.
		 */
		private final Map<String, ColorClassPrototype> colorClassPrototypes = new HashMap<>();

		private final Map<String, String> variableIdToColorClassId = new HashMap<>();

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
			public List<Pair<String, Set<String>>> subclasses = new ArrayList<>();
			boolean createdColorClass = false;
			boolean createdSubclasses = false;

			public ColorClassPrototype(String id, boolean ordered, List<String> colors) {
				this.id = id;
				this.ordered = ordered;
				this.colors = colors;
			}

			public void createColorClass(HLPetriGame game) {
				if (this.createdColorClass) {
					throw new IllegalStateException("Already created color class from prototype");
				}
				List<Color> actualColors = this.colors.stream()
						.map(Color::new)
						.collect(Collectors.toUnmodifiableList());
				game.createBasicColorClass(this.id, this.ordered, actualColors);
				this.createdColorClass = true;
			}

			public void createSubclasses(HLPetriGame game) {
				if (this.createdSubclasses) {
					throw new IllegalStateException("Already created subclasses from prototype");
				}
				if (!this.subclasses.isEmpty()) {
					this.finalizeSubclasses();
					game.addStaticSubClasses(this.id, this.subclasses.stream()
							.map(pair -> new Pair<>(pair.getFirst(), pair.getSecond().toArray(String[]::new)))
							.collect(Collectors.toUnmodifiableList()));
				}
				this.createdSubclasses = true;
			}

			private void finalizeSubclasses() {
				Set<String> alreadyPartitioned = this.subclasses.stream()
						.flatMap(subclass -> subclass.getSecond().stream())
						.collect(Collectors.toUnmodifiableSet());
				Set<String> rest = new HashSet<>(this.colors);
				rest.removeAll(alreadyPartitioned);

				if (rest.isEmpty()) {
					return;
				}
				boolean partitionForRestAlreadyExists = this.subclasses.stream()
						.anyMatch(subclass -> rest.equals(subclass.getSecond()));
				if (partitionForRestAlreadyExists) {
					return;
				}
				this.subclasses.add(new Pair<>(this.id + "_rest", rest));
			}

			public void setSubclasses(List<Pair<String, Set<String>>> subclasses) {
				if (!this.subclasses.isEmpty()) {
					throw new IllegalStateException("Color class '" + this.id + "' is already partitioned");
				}
				this.subclasses = subclasses;
			}

			public void addSingletonSubclass(String colorId) {
				Optional<Pair<String, Set<String>>> subclass = getSubclass(colorId);
				if (subclass.isEmpty()) {
					this.subclasses.add(new Pair<>(colorId + "_singleton", Collections.singleton(colorId)));
				} else if (subclass.get().getSecond().size() != 1) {
					throw new IllegalStateException("Color '" + colorId + "' already has a subclass, that is not a singleton");
				}
			}

			private Optional<Pair<String, Set<String>>> getSubclass(String colorId) {
				return this.subclasses.stream()
						.filter(pair -> pair.getSecond().contains(colorId))
						.findFirst();
			}

			@Override
			public String toString() {
				return this.id + ": " + this.colors.stream().collect(Collectors.joining(", ", "{", "}")) + "\n\t" + this.subclasses.stream()
						.map(pair -> pair.getFirst() + " = " + pair.getSecond().stream().collect(Collectors.joining(", ", "{", "}")))
						.collect(Collectors.joining("\n\t"));
			}
		}

		/* not null */
		private ColorClassPrototype getColorClassById(String colorClassId) throws ParseException {
			ColorClassPrototype ret = this.colorClassPrototypes.get(colorClassId);
			if (ret == null) {
				throw new ParseException("ColorClass '" + colorClassId + "' does not exists");
			}
			return ret;
		}

		private ColorClassPrototype getColorClassByVariableId(String variableId) throws ParseException {
			String colorClassId = this.variableIdToColorClassId.get(variableId);
			if (colorClassId == null) {
				throw new ParseException(new NoSuchElementException("Variable '" + variableId + "' is not declared"));
			}
			return this.getColorClassById(colorClassId);
		}

		/* not null */
		private ColorClassPrototype getColorClassByColorId(String colorId, boolean createPartition) throws ParseException {
			if (!this.config.allowExplicitlyReferencedColors && createPartition) {
				throw new ParseException(new UnsupportedOperationException("Explicitly referencing a color breaks symmetries and the workaround is disabled"));
			}
			ColorClassPrototype ret = this.colorClassPrototypes.values().stream()
					.filter(colorClass -> colorClass.colors.contains(colorId))
					.findFirst()
					.orElseThrow(() -> new ParseException("Color '" + colorId + "' has no color class"));
			if (createPartition) {
				ret.addSingletonSubclass(colorId);
			}
			return ret;
		}

		private String getColorSubClassByColorId(String colorId) throws ParseException {
			return getColorClassByColorId(colorId, true)
					.getSubclass(colorId)
					.orElseThrow(() -> new ParseException("Color '" + colorId + "' has no color class"))
					.getFirst();
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
			List<Pair<String, Set<String>>> partitionedClasses = new ArrayList<>();
			for (Element partitionelement : partitionelements) {
				String classId = toSafeIdentifier(getAttribute(partitionelement, "id"));
				List<Element> useroperators = getChildElements(partitionelement, "useroperator");
				Set<String> colors = new HashSet<>();
				for (Element useroperator : useroperators) {
					colors.add(toSafeIdentifier(getAttribute(useroperator, "declaration")));
				}
				partitionedClasses.add(new Pair<>(classId, colors));
			}

			if (this.config.checkPartitions) {
				Set<Map.Entry<String, Long>> duplicates = partitionedClasses.stream()
						.flatMap(pair -> pair.getSecond().stream())
						.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
						.entrySet().stream()
						.filter(entry -> entry.getValue() > 1)
						.collect(Collectors.toSet());
				if (!duplicates.isEmpty()) {
					throw new ParseException(id + " is not a partition because it contains duplicates " + duplicates);
				}

				List<String> partitionedClassIds = partitionedClasses.stream()
						.map(Pair::getSecond)
						.flatMap(Collection::stream)
						.sorted()
						.collect(Collectors.toList());
				List<String> classIdsToPartition = partitionedColorClass.colors.stream()
						.sorted()
						.collect(Collectors.toList());
				if (!partitionedClassIds.equals(classIdsToPartition)) {
					throw new ParseException(id + " is not a partition because it is not complete");
				}
			}
			partitionedColorClass.setSubclasses(partitionedClasses);
			log.addMessage(" End  <partition id=\"" + id + "\">");
		}

		private void parseFiniteintrange(Element namedsort) throws ParseException {
			String id = toSafeIdentifier(getAttribute(namedsort, "id"));
			Element finiteintrange = getChildElement(namedsort, "finiteintrange");
			long start = parseLong(getAttribute(finiteintrange, "start"));
			long end = parseLong(getAttribute(finiteintrange, "end"));
			List<String> range = LongStream.rangeClosed(start, end)
					.mapToObj(Long::toString)
					.map(this::toSafeIdentifier)
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
				if (this.config.ignoreNumberofIfOmitted) {
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
			if (this.config.checkSetBased) {
				long amount = parseLong(getAttribute(getChildElement(subterms.get(0), "numberconstant"), "value"));
				if (amount != 1) {
					throw new ParseException("Only set based nets allowed");
				}
			}

			return parseInitialMarkingTerm(subterms.get(1));
		}

		private ColorTokens parseInitialMarkingTerm(Element containingElement) throws ParseException {
			Element elem;
			if ((elem = getOptionalChildElement(containingElement, "useroperator")) != null) {
				String referencedColor = toSafeIdentifier(getAttribute(elem, "declaration"));
				return new ColorTokens(Collections.singleton(new ColorToken(new Color(referencedColor))));
			} else if ((elem = getOptionalChildElement(containingElement, "tuple")) != null) {
				return parseInitialMarkingTupleTerm(elem);
			} else if ((elem = getOptionalChildElement(containingElement, "all")) != null) {
				String referencedColorClassId = toSafeIdentifier(getAttribute(getChildElement(elem, "usersort"), "declaration"));
				Set<ColorToken> tokens = this.getColorClassById(referencedColorClassId).colors.stream()
						.map(Color::new)
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
				// TODO setminus
				throw new ParseException(new UnsupportedOperationException("Unknown term in initial marking"));
			}
		}

		private ColorTokens parseInitialMarkingTupleTerm(Element tupleElement) throws ParseException {
			List<List<String>> cartesianFactors = new ArrayList<>();
			for (Element subterm : getChildElements(tupleElement, "subterm")) {
				Element innerElem;
				if ((innerElem = getOptionalChildElement(subterm, "useroperator")) != null) {
					String referenced = toSafeIdentifier(getAttribute(innerElem, "declaration"));
					cartesianFactors.add(List.of(referenced));
				} else if ((innerElem = getOptionalChildElement(subterm, "all")) != null) {
					String referencedColorClassId = toSafeIdentifier(getAttribute(getChildElement(innerElem, "usersort"), "declaration"));
					cartesianFactors.add(this.getColorClassById(referencedColorClassId).colors);
				}
			}

			Set<ColorToken> tuples = StreamSupport.stream(new CartesianProduct<>(cartesianFactors).spliterator(), false)
					.map(colorIds -> colorIds.stream()
							.map(Color::new)
							.collect(Collectors.toList()))
					.map(ColorToken::new)
					.collect(Collectors.toSet());
			return new ColorTokens(tuples);
		}

		Set<IPredicate> additionalTermsForCurrentTransition = new HashSet<>();

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
			this.additionalTermsForCurrentTransition.clear();
			String placeId = toSafeIdentifier(getAttribute(transitionElem, "id"));
			Transition transition = game.getTransition(placeId);
			Element conditionElem = getOptionalChildElement(transitionElem, "condition");
			if (conditionElem == null) {
				return;
			}
			IPredicate predicate = parsePredicate(getChildElement(conditionElem, "structure"));
			for (IPredicate additionalTerm : this.additionalTermsForCurrentTransition) {
				predicate = new BinaryPredicate(predicate, BinaryPredicate.Operator.AND, additionalTerm);
			}
			game.setPredicate(transition, predicate);
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
			} else if ((elem = getOptionalChildElement(termElem, "booleanconstant")) != null) {
				return parseBooleanConstant(elem);
			} else if ((elem = getOptionalChildElement(termElem, "lessthan")) != null) {
				return parseOrderingPredicate(elem, OrderingOperator.LESS);
			} else if ((elem = getOptionalChildElement(termElem, "lessthanorequal")) != null) {
				return parseOrderingPredicate(elem, OrderingOperator.LESS_EQUAL);
			} else if ((elem = getOptionalChildElement(termElem, "greaterthan")) != null) {
				return parseOrderingPredicate(elem, OrderingOperator.GREATER);
			} else if ((elem = getOptionalChildElement(termElem, "greaterthanorequal")) != null) {
				return parseOrderingPredicate(elem, OrderingOperator.GREATER_EQUAL);
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
			return new BasicPredicate<>(parseEqualityPredicateTerm(subterms.get(0)), operator, parseEqualityPredicateTerm(subterms.get(1)));
		}

		public IPredicateTerm<Color> parseEqualityPredicateTerm(Element containingElement) throws ParseException {
			Element elem;
			if ((elem = getOptionalChildElement(containingElement, "variable")) != null) {
				return new Variable(toSafeIdentifier(getAttribute(elem, "refvariable")));
			} else if ((elem = getOptionalChildElement(containingElement, "predecessor")) != null) {
				return new PredecessorTerm(new Variable(toSafeIdentifier(getAttribute(getChildElement(getChildElement(elem,
						"subterm"), "variable"), "refvariable"))), game);
			} else if ((elem = getOptionalChildElement(containingElement, "successor")) != null) {
				return new SuccessorTerm(new Variable(toSafeIdentifier(getAttribute(getChildElement(getChildElement(elem,
						"subterm"), "variable"), "refvariable"))), game);
			} else if ((elem = getOptionalChildElement(containingElement, "useroperator")) != null) {
				log.addWarning("Equality between variable and color breaks symmetries");
				String referencedColorId = toSafeIdentifier(getAttribute(elem, "declaration"));
				Pair<Variable, BasicPredicate<ColorClassType>> antisymmetryTerms = createAntisymmetryTerms(referencedColorId);
				this.additionalTermsForCurrentTransition.add(antisymmetryTerms.getSecond());
				return antisymmetryTerms.getFirst();
			} else {
				throw new ParseException(new UnsupportedOperationException("Unknown term in equality"));
			}
		}

		private Pair<Variable, BasicPredicate<ColorClassType>> createAntisymmetryTerms(String referencedColorId) throws ParseException {
			String subclass = this.getColorSubClassByColorId(referencedColorId);
			Variable variable = new Variable(subclass + "_variable");
			BasicPredicate<ColorClassType> variableRestrictionTerm = new BasicPredicate<>(
					new DomainTerm(variable, game), BasicPredicate.Operator.EQ, new ColorClassTerm(subclass));
			return new Pair<>(variable, variableRestrictionTerm);
		}

		private IPredicate parseBooleanConstant(Element booleanvalue) throws ParseException {
			String value = getAttribute(booleanvalue, "value");
			if (value.equalsIgnoreCase("true")) {
				return Constants.TRUE;
			} else if (value.equalsIgnoreCase("false")) {
				return Constants.FALSE;
			} else {
				throw new ParseException("a booleanconstants value must be true or false");
			}
		}

		private enum OrderingOperator {
			LESS("<", true, true),
			LESS_EQUAL("<=", true, false),
			GREATER(">", false, true),
			GREATER_EQUAL(">=", false, false);

			private final String symbol;
			private final boolean falling;
			private final boolean skipBound;

			OrderingOperator(String symbol, boolean falling, boolean skipBound) {
				this.symbol = symbol;
				this.falling = falling;
				this.skipBound = skipBound;
			}

			@Override
			public String toString() {
				return this.symbol;
			}
		}

		private IPredicate parseOrderingPredicate(Element containingElement, OrderingOperator operator) throws ParseException {
			if (!this.config.allowOrderingRelations) {
				throw new ParseException(new UnsupportedOperationException("ColorClasses cannot be partially ordered sets, because they are cyclic. The workaround is disabled."));
			}
			List<Element> subterms = getChildElements(containingElement, "subterm");
			if (subterms.size() != 2) {
				throw new ParseException("<" + containingElement.getNodeName() + "> is a binary operator");
			}

			String variableId = toSafeIdentifier(getAttribute(getChildElement(subterms.get(0), "variable"), "refvariable"));
			Element elem;
			if ((elem = getOptionalChildElement(subterms.get(1), "useroperator")) != null) {
				String colorId = toSafeIdentifier(getAttribute(elem, "declaration"));
				return createOrderPredicateForVariableAndColor(variableId, operator, colorId);
			} else if ((elem = getOptionalChildElement(subterms.get(1), "variable")) != null) {
				String otherVariableId = toSafeIdentifier(getAttribute(elem, "refvariable"));
				return createOrderPredicateForVariableAndVariable(variableId, operator, otherVariableId);
			} else {
				throw new ParseException("Unknown term in ordering predicate");
			}
		}

		private IPredicate createOrderPredicateForVariableAndColor(String variableId, OrderingOperator operator, String colorId) throws ParseException {
			List<String> allowedColorIds = getRange(colorId, operator);
			if (allowedColorIds.isEmpty()) {
				return Constants.FALSE;
			} else {
				List<ColorClassTerm> allowedColorClasses = new LinkedList<>();
				for (String currentColorId : allowedColorIds) {
					allowedColorClasses.add(new ColorClassTerm(getColorSubClassByColorId(currentColorId)));
				}
				Variable variable = new Variable(variableId);
				return BinaryPredicate.createPredicate(allowedColorClasses.stream()
						.map(colorClassTerm -> new BasicPredicate<>(new DomainTerm(variable, game), BasicPredicate.Operator.EQ, colorClassTerm))
						.collect(Collectors.toList()), BinaryPredicate.Operator.OR);
			}
		}

		private IPredicate createOrderPredicateForVariableAndVariable(String leftId, OrderingOperator operator, String rightId) throws ParseException {
			ColorClassPrototype colorClass = getColorClassByVariableId(rightId);
			Variable variable = new Variable(leftId);

			List<IPredicate> cases = new LinkedList<>();
			for (String colorId : colorClass.colors) {
				cases.add(new BinaryPredicate(
						new BasicPredicate<>(new DomainTerm(variable, game), BasicPredicate.Operator.EQ, new ColorClassTerm(getColorSubClassByColorId(colorId))),
						BinaryPredicate.Operator.AND,
						createOrderPredicateForVariableAndColor(leftId, operator, colorId)
				));
			}
			return BinaryPredicate.createPredicate(cases, BinaryPredicate.Operator.OR);
		}

		/**
		 * Get all colors, that satisfy the given ordering relation.
		 * <p>
		 * Assume that the color that was given first is the minimum and the color given last is the maximum.
		 * <p>
		 * For x in [1, 2, 3, 4] and the expression x < 3 return [1, 2]
		 */
		private List<String> getRange(String referencedColorId, OrderingOperator operator) throws ParseException {
			List<String> ret = new LinkedList<>();
			ColorClassPrototype colorClass = getColorClassByColorId(referencedColorId, false);
			String end = operator.falling ? colorClass.colors.get(0) : colorClass.colors.get(colorClass.colors.size() - 1);
			if (operator.skipBound && referencedColorId.equals(end)) {
				return Collections.emptyList();
			}
			String current = operator.skipBound ? getNeighbour(referencedColorId, operator.falling) : referencedColorId;
			while (!current.equals(end)) {
				ret.add(current);
				current = getNeighbour(current, operator.falling);
			}
			ret.add(current);
			return ret;
		}

		private String getNeighbour(String colorId, boolean wantPredecessor) throws ParseException {
			List<String> colors = this.getColorClassByColorId(colorId, false).colors;
			int colorIndex = colors.indexOf(colorId);
			/* 2 * because java's modulo can return negative numbers */
			if (colorIndex == 0 && wantPredecessor) {
				return colors.get(colors.size() - 1);
			} else if (colorIndex == colors.size() - 1 && !wantPredecessor) {
				return colors.get(0);
			} else {
				return colors.get(colorIndex + (wantPredecessor ? -1 : +1));
			}
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
			this.additionalTermsForCurrentTransition.clear();
			log.addMessage("Begin <arc id=\"" + id + "\">");

			uniol.apt.adt.pn.Node source = game.getNode(sourceId);
			uniol.apt.adt.pn.Node target = game.getNode(targetId);
			Transition transition;
			if (source instanceof Transition) {
				transition = (Transition) source;
			} else if (target instanceof Transition) {
				transition = (Transition) target;
			} else {
				throw new ParseException("An arc must be between a place and a transition");
			}
			Flow flow = game.createFlow(source, target,
					parseArcInscription(getChildElement(getChildElement(arc,
							"hlinscription"), "structure")));

			IPredicate predicate = game.getPredicate(transition);
			for (IPredicate additionalTerm : this.additionalTermsForCurrentTransition) {
				predicate = new BinaryPredicate(predicate, BinaryPredicate.Operator.AND, additionalTerm);
			}
			game.setPredicate(transition, predicate);

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
			} else if ((elem = getOptionalChildElement(structure, "subtract")) != null) {
				return parseArcSetMinusTerm(elem);
			} else {
				if (this.config.ignoreNumberofIfOmitted) {
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
			return parseArcTerm(unwrapNumberofArc(numberofElem));
		}

		private ArcExpression parseArcSetMinusTerm(Element containingElement) throws ParseException {
			List<Element> subterms = getChildElements(containingElement, "subterm");
			if (subterms.size() < 2) {
				throw new ParseException("A <subtract> term must have at least 2 subterms");
			}
			Iterator<Element> subtermIterator = subterms.iterator();
			Element setSubterm = subtermIterator.next();
			Element elem;
			if ((elem = getOptionalChildElement(setSubterm, "numberof")) != null) {
				String referencedColorClassId = toSafeIdentifier(getAttribute(getChildElement(getChildElement(unwrapNumberofArc(elem),
						"all"), "usersort"), "declaration"));
				return new ArcExpression(new SetMinusTerm(new ColorClassTerm(referencedColorClassId), parseArcSetMinusSubtractedPartSingle(subtermIterator).toArray(Variable[]::new)));
			} else if ((elem = getOptionalChildElement(setSubterm, "add")) != null) {
				List<Object> parseArcSetMinusSetPartWithAdd = parseArcSetMinusSetPartWithAdd(elem);
				List<List<Variable>> variableTuples = parseArcSetMinusSubtractedPartTuple(subtermIterator);

				ArcTuple arcTuple = new ArcTuple();
				for (int i = 0, size = parseArcSetMinusSetPartWithAdd.size(); i < size; i++) {
					Object tupleMemberInSet = parseArcSetMinusSetPartWithAdd.get(i);
					if (tupleMemberInSet instanceof ColorClassTerm) {
						List<Variable> list = new ArrayList<>();
						for (List<Variable> tupleMembers : variableTuples) {
							list.add(tupleMembers.get(i));
						}
						Variable[] variables = list.toArray(Variable[]::new);
						arcTuple.add(new SetMinusTerm((ColorClassTerm) tupleMemberInSet, variables));
					} else {
						assert tupleMemberInSet instanceof Variable;
						Variable variable = (Variable) tupleMemberInSet;
						for (List<Variable> tupleMembers : variableTuples) {
							if (!variable.equals(tupleMembers.get(i))) {
								throw new ParseException("Cannot subtract variable from another variable");
							}
						}
						arcTuple.add(variable);
					}
				}
				return new ArcExpression(arcTuple);
			} else {
				throw new ParseException(new UnsupportedOperationException("Unknown description of the set for <subtract>"));
			}
		}

		private List<Variable> parseArcSetMinusSubtractedPartSingle(Iterator<Element> subtractedIterator) throws ParseException {
			List<Variable> subtractedVariables = new LinkedList<>();
			while (subtractedIterator.hasNext()) {
				Element subtractedSubterm = subtractedIterator.next();
				Element numberofSubtracted = getChildElement(subtractedSubterm, "numberof");
				Element numberofSubtermContainingVariableReference = unwrapNumberofArc(numberofSubtracted);
				subtractedVariables.add(parseVariableArcTerm(getChildElement(numberofSubtermContainingVariableReference, "variable")));
			}
			return subtractedVariables;
		}

		/**
		 * Outer list: the tuple to remove, inner list: tuple members
		 */
		private List<List<Variable>> parseArcSetMinusSubtractedPartTuple(Iterator<Element> subtractedIterator) throws ParseException {
			List<List<Variable>> subtractedTuples = new LinkedList<>();
			while (subtractedIterator.hasNext()) {
				Element subtractedSubterm = subtractedIterator.next();
				Element numberofSubtracted = getChildElement(subtractedSubterm, "numberof");
				Element numberofSubtermContainingTupleOfVariables = unwrapNumberofArc(numberofSubtracted);
				List<Element> tupleMemberElements = getChildElements(getChildElement(numberofSubtermContainingTupleOfVariables, "tuple"), "subterm");
				List<Variable> tupleMembers = new LinkedList<>();
				for (Element tupleMemberElement : tupleMemberElements) {
					tupleMembers.add(parseVariableArcTerm(getChildElement(tupleMemberElement, "variable")));
				}
				subtractedTuples.add(tupleMembers);
			}
			return subtractedTuples;
		}

		/**
		 * Return a list of variables and color classes
		 */
		private List<Object> parseArcSetMinusSetPartWithAdd(Element add) throws ParseException {
			List<Element> addSubterms = getChildElements(add, "subterm");
			if (addSubterms.size() < 2) {
				throw new ParseException("Must add at least 2 elements");
			}
			Element secondSubtermInFirstNumberof = unwrapNumberofArc(getChildElement(addSubterms.get(0), "numberof"));
			List<Element> tupleMemberElements = getChildElements(getChildElement(secondSubtermInFirstNumberof, "tuple"), "subterm");
			String[] variables = new String[addSubterms.size()];
			String[] colors = new String[addSubterms.size()];
			for (int i = 0, size = tupleMemberElements.size(); i < size; i++) {
				Element tupleMemberElement = tupleMemberElements.get(i);
				Element elem;
				if ((elem = getOptionalChildElement(tupleMemberElement, "variable")) != null) {
					String referencedVariableId = getAttribute(elem, "refvariable");
					variables[i] = referencedVariableId;
				} else if ((elem = getOptionalChildElement(tupleMemberElement, "useroperator")) != null) {
					String referencedColorId = getAttribute(elem, "declaration");
					colors[i] = referencedColorId;
				} else {
					throw new ParseException(new UnsupportedOperationException("Unknown reference in <add> inside <subtract> for arcs"));
				}
			}
			/* now for every index i variables[i] != null xor colors[i] != null */

			List<Object> ret = new ArrayList<>();
			for (int i = 0; i < variables.length; i++) {
				if (variables[i] != null) {
					ret.add(new Variable(variables[i]));
				} else {
					assert colors[i] != null;
					ColorClassPrototype colorClass = this.getColorClassByColorId(colors[i], false);
					ret.add(new ColorClassTerm(colorClass.id));
				}
			}
			// TODO validate that we guessed the color classes correct
			return ret;
		}

		/**
		 * Check if the numberof tag of an arc's hlinscription structure
		 * is tolerable in a set based net,
		 * then return the second subterm.
		 */
		private Element unwrapNumberofArc(Element numberofElem) throws ParseException {
			List<Element> subterms = getChildElements(numberofElem, "subterm");
			if (subterms.size() != 2) {
				throw new ParseException("<numberof> must have 2 subterms, found " + subterms.size());
			}
			if (this.config.checkSetBased) {
				long amount = parseLong(getAttribute(getChildElement(subterms.get(0), "numberconstant"), "value"));
				if (amount != 1) {
					throw new ParseException("Expected 1, but got " + amount + " as multiplicity for arc expression.",
							new UnsupportedOperationException("Only set based nets allowed"));
				}
			}
			return subterms.get(1);
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
			} else if ((elem = getOptionalChildElement(containingElement, "useroperator")) != null) {
				return new Pair<>(IArcTerm.Sort.VARIABLE, parseColorReferenceArc(elem));
			} else if (getOptionalChildElement(containingElement, "dotconstant") != null) {
				return new Pair<>(IArcTerm.Sort.VARIABLE, DOT_CONSTANT_VARIABLE);
			} else {
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

		private Variable parseColorReferenceArc(Element useroperator) throws ParseException {
			String referencedColorId = toSafeIdentifier(getAttribute(useroperator, "declaration"));
			Pair<Variable, BasicPredicate<ColorClassType>> antisymmetryTerms = createAntisymmetryTerms(referencedColorId);
			this.additionalTermsForCurrentTransition.add(antisymmetryTerms.getSecond());
			return antisymmetryTerms.getFirst();
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
			} else if ((elem = getOptionalChildElement(containingElement, "all")) != null) {
				/* there is no example using this, maybe this is not allowed. */
				return new Pair<>(IArcTupleElement.Sort.COLORCLASS, parseColorClassArcTerm(elem));
			} else if ((elem = getOptionalChildElement(containingElement, "useroperator")) != null) {
				return new Pair<>(IArcTupleElement.Sort.VARIABLE, parseColorReferenceArc(elem));
			} else {
				// TODO setminus
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
			while (safeIdMap.containsValue(unique)) {
				unique = unique + "_" + idCounter;
				idCounter += 1;
			}

			safeIdMap.put(key, unique);
			return unique;
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
		return new Parser(this.config).parse(input);
	}
}
