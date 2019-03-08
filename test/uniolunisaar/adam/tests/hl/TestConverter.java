package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestConverter {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/converter/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void alarmSystem() throws IOException, InterruptedException {
        HLPetriGame hlgame = AlarmSystemHL.createSafetyVersionForHLRep(2);
        PetriGame pg = HL2PGConverter.convert(hlgame);
        PGTools.savePG2PDF(outputDir + pg.getName(), pg, false);
    }

}
