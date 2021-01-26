package uniolunisaar.adam.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.synthesis.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.tools.processHandling.ExternalProcessHandler;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.tools.processHandling.ProcessPool;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.StateIdentifier;
import uniolunisaar.adam.ds.petrinet.PetriNetExtensionHandler;
import uniolunisaar.adam.tools.AdamProperties;

/**
 *
 * @author Manuel Gieseking
 */
public class HLTools {

    public static String hlpg2Dot(HLPetriGame game, boolean withLabels) {
        return HLTools.hlpg2Dot(game, withLabels, null);
    }

    public static String hlpg2Dot(HLPetriGame game, boolean withLabel, Integer tokencount) {
        final String placeShape = "circle";
        final String specialPlaceShape = "doublecircle";

        StringBuilder sb = new StringBuilder();
        sb.append("digraph PetriNet {\n");

        // Basic Color classes
        sb.append("#basic color classes\n");
        sb.append("node [shape=box, style=dashed];\n");
        sb.append("\"");
        for (BasicColorClass basicColorClasse : game.getBasicColorClasses()) {
            sb.append(basicColorClasse.toDot()).append("\n");
        }
        sb.append("\"\n");

        // Transitions
        sb.append("#transitions\n");
        sb.append("node [shape=box, style=solid, height=0.5, width=0.5, fixedsize=true];\n");
        for (Transition t : game.getTransitions()) {
            String c = null;
            if (game.isStrongFair(t)) {
                c = "blue";
            }
            if (game.isWeakFair(t)) {
                c = "lightblue";
            }
            String color = (c != null) ? "style=filled, fillcolor=" + c : "";

            sb.append("\"").append(t.getId()).append("\"").append("[").append(color);
            sb.append("xlabel=\"").append(game.getPredicate(t).toSymbol()).append("\"");
            sb.append("];\n");
        }
        sb.append("\n\n");

        // Places
        sb.append("#places\n");
        for (Place place : game.getPlaces()) {
            // special?
            String shape = (game.isBad(place) || game.isReach(place) || game.isBuchi(place)) ? specialPlaceShape : placeShape;
            // Initialtoken number
            String tokenString = game.hasColorTokens(place) ? game.getColorTokens(place).toDotString() : "";
            // Drawing
            String label = place.getId();
            if (withLabel && PetriNetExtensionHandler.hasLabel(place)) {
                label += "(" + PetriNetExtensionHandler.getLabel(place) + ")";
            }
            sb.append("\"").append(place.getId()).append("\"").append("[shape=").append(shape);
            sb.append(", height=0.5, width=0.5, fixedsize=false, margin=0");
            sb.append(", xlabel=").append("\"").append(label).append("\\n(").append(game.getColorDomain(place)).append(")").append("\"");
            sb.append(", label=").append("\"").append(tokenString).append("\"");

//            if (game.hasPartition(place)) {
//                int t = game.getPartition(place);
//                if (t != 0) {  // should it be colored?
//                    sb.append(", style=\"filled");
//                    if (game.isInitialTransit(place)) {
//                        sb.append(", dashed");
//                    }
//                    sb.append("\", fillcolor=");
//                    if (tokencount == null) {
//                        sb.append("gray");
//                    } else {
//                        sb.append("\"");
//                        float val = ((t + 1) * 1.f) / (tokencount * 1.f);
//                        sb.append(val).append(" ").append(val).append(" ").append(val);
//                        sb.append("\"");
//                    }
//                } else if (game.isInitialTransit(place)) {
//                    sb.append(", style=dashed");
//                }
//            }
            if (game.isSystem(place)) {
                sb.append(", style=\"filled");
                sb.append("\", fillcolor=");
                sb.append("gray");
            }

            sb.append("];\n");
        }

        // Flows
//        Map<Flow, String> map = getTransitRelationFromTransitions(game);
        sb.append("\n#flows\n");
        for (Flow f : game.getEdges()) {
            sb.append("\"").append(f.getSource().getId()).append("\"").append("->").append("\"").append(f.getTarget().getId()).append("\"");
            Integer w = f.getWeight();
            String weight = "\"" + ((w != 1) ? w.toString() + " : " : "");
//            if (map.containsKey(f)) {
//                weight += map.get(f);
//            }
            if (game.hasArcExpression(f)) {
                weight += game.getArcExpression(f).toString();
            }
            weight += "\"";
            sb.append("[label=").append(weight);
//            if (map.containsKey(f)) {
//                String tfl = map.get(f);
//                if (!tfl.contains(",")) {
//                    sb.append(", color=\"");
//                    Transit init = game.getInitialTransit(f.getTransition());
//                    int max = game.getTransits(f.getTransition()).size() + ((init == null) ? 0 : init.getPostset().size() - 1);
//                    int id = Tools.calcStringIDSmallPrecedenceReverse(tfl);
//                    float val = ((id + 1) * 1.f) / (max * 1.f);
//                    sb.append(val).append(" ").append(val).append(" ").append(val);
//                    sb.append("\"");
//                }
//            }
//            if (game.isInhibitor(f)) {
//                sb.append(", dir=\"both\", arrowtail=\"odot\"");
//            }
            sb.append("]\n");
        }
        sb.append("overlap=false\n");
        sb.append("label=\"").append(game.getName()).append("\"\n");
        sb.append("fontsize=12\n");
        sb.append("}");
        return sb.toString();
    }

//    public static void savePnwt2Dot(String input, String output, boolean withLabel) throws IOException, ParseException {
//        PetriNetWithTransits net = new PetriNetWithTransits(Tools.getPetriNet(input));
//        HLTools.savePnwt2Dot(output, net, withLabel);
//    }
    public static void saveHLPG2Dot(String path, HLPetriGame game, boolean withLabels) throws FileNotFoundException {
        saveHLPG2Dot(path, game, withLabels, -1);
    }

    public static void saveHLPG2Dot(String path, HLPetriGame game, boolean withLabels, Integer tokencount) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(path + ".dot")) {
            if (tokencount == -1) {
                out.println(hlpg2Dot(game, withLabels));
            } else {
                out.println(hlpg2Dot(game, withLabels, tokencount));
            }
        }
        Logger.getInstance().addMessage("Saved to: " + path + ".dot", true);
    }

//    public static Thread saveHLPG2DotAndPDF(String input, String output) throws FileNotFoundException, ParseException, IOException {
//        PetriNetWithTransits net = getPetriNetWithTransitsFromParsedPetriNet(new AptPNParser().parseFile(input), false);
//        return savePnwt2DotAndPDF(output, net, withLabel);
//    }
    public static Thread saveHLPG2DotAndPDF(String path, HLPetriGame game, boolean withLabels) throws FileNotFoundException {
        return saveHLPG2DotAndPDF(path, game, withLabels, -1);
    }

    public static Thread saveHLPG2DotAndPDF(String path, HLPetriGame game, boolean withLabels, Integer tokencount) throws FileNotFoundException {
        if (tokencount == -1) {
            saveHLPG2Dot(path, game, withLabels);
        } else {
            saveHLPG2Dot(path, game, withLabels, tokencount);
        }
        String dot = AdamProperties.getInstance().getProperty(AdamProperties.DOT);
        String[] command = {dot, "-Tpdf", path + ".dot", "-o", path + ".pdf"};
        // Mac:
        //String[] command = {"/usr/local/bin/dot", "-Tpdf", path + ".dot", "-o", path + ".pdf"};
        ExternalProcessHandler procH = new ExternalProcessHandler(true, command);
        ProcessPool.getInstance().putProcess(game.getProcessFamilyID() + "#dot", procH);
        // start it in an extra thread
        Thread thread = new Thread(() -> {
            try {
                procH.startAndWaitFor();
                Logger.getInstance().addMessage("Saved to: " + path + ".pdf", true);
//                    if (deleteDot) {
//                        // Delete dot file
//                        new File(path + ".dot").delete();
//                        Logger.getInstance().addMessage("Deleted: " + path + ".dot", true);
//                    }
            } catch (IOException | InterruptedException ex) {
                String errors = "";
                try {
                    errors = procH.getErrors();
                } catch (ProcessNotStartedException e) {
                }
                Logger.getInstance().addError("Saving pdf from dot failed.\n" + errors, ex);
            }
        });
        thread.start();
        return thread;
    }

    public static Thread saveHLPG2PDF(String path, HLPetriGame game, boolean withLabels) throws FileNotFoundException {
        return saveHLPG2PDF(path, game, withLabels, -1);
    }

    public static Thread saveHLPG2PDF(String path, HLPetriGame game, boolean withLabels, Integer tokencount) throws FileNotFoundException {
        String bufferpath = path + "_" + System.currentTimeMillis();
        Thread dot;
        if (tokencount == -1) {
            dot = saveHLPG2DotAndPDF(bufferpath, game, withLabels);
        } else {
            dot = saveHLPG2DotAndPDF(bufferpath, game, withLabels, tokencount);
        }
        Thread mvPdf = new Thread(() -> {
            try {
                dot.join();
                // Delete dot file
                new File(bufferpath + ".dot").delete();
                Logger.getInstance().addMessage("Deleted: " + bufferpath + ".dot", true);
                // move to original name
                Files.move(new File(bufferpath + ".pdf").toPath(), new File(path + ".pdf").toPath(), REPLACE_EXISTING);
                Logger.getInstance().addMessage("Moved: " + bufferpath + ".pdf --> " + path + ".pdf", true);
            } catch (IOException | InterruptedException ex) {
                Logger.getInstance().addError("Deleting the buffer files and moving the pdf failed", ex);
            }
        });
        mvPdf.start();
        return mvPdf;
    }

    public static <P, T, DC extends IDecision<P, T>, DCS extends IDecisionSet<P, T, DC, DCS>, ID extends StateIdentifier, F extends GameGraphFlow<T, ID>> String hlGraph2Dot(AbstractGameGraph<P, T, DC, DCS, ID, F> graph) {
        final String mcutColor = "white";
        final String sysColor = "gray";

        StringBuilder sb = new StringBuilder();
        sb.append("digraph hlGraphGame {\n");

        // States
        sb.append("#states\n");
        for (DCS state : graph.getStatesView()) {
            if (state.getId() == -1) {
                state.setId(state.hashCode());
            }
            // mcut?
//            String shape = (state.isMcut()) ? mcutShape : sysShape;
            String color = (state.isMcut()) ? mcutColor : sysColor;
            int penwidth = (state.isBad()) ? 8 : 1;
            //            String shape = (state.isGood()) ? "doubleoctagon" : "box";
            String shape = "box";
            // Drawing
            sb.append(state.getId()).append("[shape=").append(shape).append(", style=filled, fillcolor=").append(color);
            sb.append(", height=0.5, width=0.5, fixedsize=false,  penwidth=").append(penwidth);

            String value = state.toDot();
            sb.append(", label=\"").append(value).append("\"");
            sb.append(", xlabel=\"").append(state.getId()).append("\"");
            sb.append("];\n");
        }

        // Flows
        sb.append("\n#flows\n");
        for (F f : graph.getFlowsView()) {
//            sb.append(f.getSource().toString()).append("->").append(f.getTarget().toString());
if(f.getSource().getId()==-1) {
    System.out.println(f.getSource().toString());
}
            sb.append(f.getSource().getId()).append("->").append(f.getTarget().getId());
            T t = f.getTransition();
            String label = (t == null) ? "T" : t.toString();
            sb.append("[label=\"").append(label).append("\"]");
            sb.append("\n");
        }
        sb.append("overlap=false\n");
        sb.append("label=\"").append(graph.getName()).append("\"\n");
        sb.append("fontsize=12\n\n");
        sb.append("}");
        return sb.toString();
    }

    public static <P, T, DC extends IDecision<P, T>, DCS extends IDecisionSet<P, T, DC, DCS>, ID extends StateIdentifier, F extends GameGraphFlow<T, ID>> void saveGraph2Dot(String path, AbstractGameGraph<P, T, DC, DCS, ID, F> graph) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(path + ".dot")) {
            out.println(hlGraph2Dot(graph));
        }
        Logger.getInstance().addMessage("Saved to: " + path + ".dot", true);
    }

    public static <P, T, DC extends IDecision<P, T>, DCS extends IDecisionSet<P, T, DC, DCS>, ID extends StateIdentifier, F extends GameGraphFlow<T, ID>> void saveGraph2DotAndPDF(String path, AbstractGameGraph<P, T, DC, DCS, ID, F> graph) throws IOException, InterruptedException {
        saveGraph2Dot(path, graph);
        Runtime rt = Runtime.getRuntime();
        String dot = AdamProperties.getInstance().getProperty(AdamProperties.DOT);
        String exString = dot + " -Tpdf " + path + ".dot -o " + path + ".pdf";
        Process p = rt.exec(exString);
        p.waitFor();
        Logger.getInstance().addMessage("Saved to: " + path + ".pdf", true);
    }

    public static <P, T, DC extends IDecision<P, T>, DCS extends IDecisionSet<P, T, DC, DCS>, ID extends StateIdentifier, F extends GameGraphFlow<T, ID>> void saveGraph2PDF(String path, AbstractGameGraph<P, T, DC, DCS, ID, F> graph) throws IOException, InterruptedException {
        String bufferpath = path + System.currentTimeMillis();
        saveGraph2DotAndPDF(bufferpath, graph);
        // Delete dot file
        new File(bufferpath + ".dot").delete();
        Logger.getInstance().addMessage("Deleted: " + bufferpath + ".dot", true);
        // move to original name
        Files.move(new File(bufferpath + ".pdf").toPath(), new File(path + ".pdf").toPath(), REPLACE_EXISTING);
        Logger.getInstance().addMessage("Moved: " + bufferpath + ".pdf --> " + path + ".pdf", true);
    }

    public static void saveHLPG2PDF(String path, HLPetriGame hlgame) throws FileNotFoundException {
        saveHLPG2PDF(path, hlgame, false);
    }

}
