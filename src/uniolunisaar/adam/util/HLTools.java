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
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.tools.ExternalProcessHandler;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.tools.ProcessPool;

/**
 *
 * @author Manuel Gieseking
 */
public class HLTools {

    public static String hlpg2Dot(HLPetriGame game) {
        return HLTools.hlpg2Dot(game, null);
    }

    public static String hlpg2Dot(HLPetriGame game, Integer tokencount) {
        final String placeShape = "circle";
        final String specialPlaceShape = "doublecircle";

        StringBuilder sb = new StringBuilder();
        sb.append("digraph PetriNet {\n");

        // Transitions
        sb.append("#transitions\n");
        sb.append("node [shape=box, height=0.5, width=0.5, fixedsize=true];\n");
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
            String tokenString = game.hasColorToken(place) ? game.getColorToken(place).toString() : "";
            // Drawing
            sb.append("\"").append(place.getId()).append("\"").append("[shape=").append(shape);
            sb.append(", height=0.5, width=0.5, fixedsize=true");
            sb.append(", xlabel=").append("\"").append(place.getId()).append("\\n(").append(game.getColorDomain(place)).append(")").append("\"");
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
    public static void saveHLPG2Dot(String path, HLPetriGame game) throws FileNotFoundException {
        saveHLPG2Dot(path, game, -1);
    }

    public static void saveHLPG2Dot(String path, HLPetriGame game, Integer tokencount) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(path + ".dot")) {
            if (tokencount == -1) {
                out.println(hlpg2Dot(game));
            } else {
                out.println(hlpg2Dot(game, tokencount));
            }
        }
        Logger.getInstance().addMessage("Saved to: " + path + ".dot", true);
    }

//    public static Thread saveHLPG2DotAndPDF(String input, String output) throws FileNotFoundException, ParseException, IOException {
//        PetriNetWithTransits net = getPetriNetWithTransitsFromParsedPetriNet(new AptPNParser().parseFile(input), false);
//        return savePnwt2DotAndPDF(output, net, withLabel);
//    }
    public static Thread saveHLPG2DotAndPDF(String path, HLPetriGame game) throws FileNotFoundException {
        return saveHLPG2DotAndPDF(path, game, -1);
    }

    public static Thread saveHLPG2DotAndPDF(String path, HLPetriGame game, Integer tokencount) throws FileNotFoundException {
        if (tokencount == -1) {
            saveHLPG2Dot(path, game);
        } else {
            saveHLPG2Dot(path, game, tokencount);
        }
        String[] command = {"dot", "-Tpdf", path + ".dot", "-o", path + ".pdf"};
        ExternalProcessHandler procH = new ExternalProcessHandler(true, command);
        ProcessPool.getInstance().putProcess(game.getName() + "#dot", procH);
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
        // older version
//        ProcessBuilder procBuilder = new ProcessBuilder("dot", "-Tpdf", path + ".dot", "-o", path + ".pdf");
//        Process proc = procBuilder.start();
//        String error = IOUtils.toString(proc.getErrorStream());
//        Logger.getInstance().addMessage(error, true); // todo: print it as error an a proper exception
//        String output = IOUtils.toString(proc.getInputStream());
//        Logger.getInstance().addMessage(output, true);
//        proc.waitFor();
//        Logger.getInstance().addMessage("Saved to: " + path + ".pdf", true);

        // oldest version
//        Runtime rt = Runtime.getRuntime();
////        String exString = "dot -Tpdf " + path + ".dot > " + path + ".pdf";
//        String exString = "dot -Tpdf " + path + ".dot -o " + path + ".pdf";
//        Process p = rt.exec(exString);
//        p.waitFor();
//            rt.exec("evince " + path + ".pdf");
//        Logger.getInstance().addMessage("Saved to: " + path + ".pdf", true);
    }

    public static Thread saveHLPG2PDF(String path, HLPetriGame game) throws FileNotFoundException {
        return saveHLPG2PDF(path, game, -1);
    }

    public static Thread saveHLPG2PDF(String path, HLPetriGame game, Integer tokencount) throws FileNotFoundException {
        String bufferpath = path + "_" + System.currentTimeMillis();
        Thread dot;
        if (tokencount == -1) {
            dot = saveHLPG2DotAndPDF(bufferpath, game);
        } else {
            dot = saveHLPG2DotAndPDF(bufferpath, game, tokencount);
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

}
