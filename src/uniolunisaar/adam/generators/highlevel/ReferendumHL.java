package uniolunisaar.adam.generators.highlevel;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReferendumHL {

	public static final String VOTERS_CLASS_NAME = "Voters";
	public static final String[] JUST_VOTERS = { VOTERS_CLASS_NAME };

	/**
	 * <a href="https://mcc.lip6.fr/pdf/Referendum-form.pdf">Referendum</a> model
	 * from the <a href="https://mcc.lip6.fr/models.php">Model Checking Contest</a>.
	 *
	 * @param voters the maximum number of voters (in the colordomain).
	 */
	public static HLPetriGame create(int voters) {
		HLPetriGame net = new HLPetriGame("Referendum");

		List<Color> votersClass = IntStream.rangeClosed(1, voters)
				.mapToObj(i -> new Color(String.valueOf(i)))
				.collect(Collectors.toList());
		Variable v = new Variable("v");
		net.createBasicColorClass(VOTERS_CLASS_NAME, true, votersClass);

		Place ready = net.createEnvPlace("ready", new String[0]);
		Place voting = net.createEnvPlace("voting", JUST_VOTERS);
		Place votedYes = net.createEnvPlace("voted_yes", JUST_VOTERS);
		Place votedNo = net.createEnvPlace("voted_no", JUST_VOTERS);

		Transition start = net.createTransition("start");
		Transition yes = net.createTransition("yes");
		Transition no = net.createTransition("no");

		net.createFlow(ready, start);
		net.createFlow(start, voting, new ArcExpression(new ColorClassTerm(VOTERS_CLASS_NAME)));
		net.createFlow(voting, yes, new ArcExpression(v));
		net.createFlow(yes, votedYes, new ArcExpression(v));
		net.createFlow(voting, no, new ArcExpression(v));
		net.createFlow(no, votedNo, new ArcExpression(v));
		//net.initializeWinningCondition();
		return net;
	}
}
