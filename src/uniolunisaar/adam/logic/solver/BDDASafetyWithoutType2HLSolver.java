package uniolunisaar.adam.logic.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.highlevel.symmetries.SymmetryIterator;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;
import uniolunisaar.adam.util.benchmarks.Benchmarks;
import uniolunisaar.adam.tools.Logger;

/**
 * Solves Petri games with a safety objective with BDDs. This solver can be used
 * when there is no possibillity for system player to play infinitely long
 * without any further interaction with the environment.
 *
 * @author Manuel Gieseking
 */
public class BDDASafetyWithoutType2HLSolver extends BDDSolver<Safety> {

    private final Symmetries syms;

    /**
     * Creates a new Safety solver for a given game.
     *
     * @param game - the Petri game to solve.
     * @param skipTests - should the tests for safe and bounded and other
     * preconditions be skipped?
     * @param opts - the options for the solver.
     * @throws NotSupportedGameException - Thrown if the given net is not
     * bounded.
     * @throws NetNotSafeException - Thrown if the given net is not safe.
     * @throws NoSuitableDistributionFoundException - Thrown if the given net is
     * not annotated to which token each place belongs and the algorithm was not
     * able to detect it on its own.
     */
    public BDDASafetyWithoutType2HLSolver(PetriGame game, Symmetries syms, boolean skipTests, Safety win, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(game, skipTests, win, opts);
        this.syms = syms;
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%% START WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%
    /**
     * Calculates a BDD with all possible situations containing a bad place.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @return BDD representing situations with bad places
     */
    private BDD baddcs(int pos) {
        BDD bad = getZero();
        for (Place place : getSolvingObject().getWinCon().getBadPlaces()) {
            bad.orWith(codePlace(place, pos, getSolvingObject().getGame().getPartition(place)));
        }
        return bad;
    }

    /**
     * Calculates a BDD representing all decision sets where the system decided
     * not to choose any enabled transition, but there exists at least one.
     *
     * @param pos - 0 for the predecessor variables and 1 for the successor.
     * @return BDD representing the deadlocks of the Petri game.
     */
    private BDD deadSysDCS(int pos) {
        BDD dead = getOne();
        BDD buf = getZero();
        for (Transition t : getGame().getTransitions()) {
//            dead = dead.and((firable(t, true).or(firable(t, false))).not());
//            buf = buf.or(enabled(t, true).or(enabled(t, false)));
            dead.andWith(firable(t, pos).not());
            buf.orWith(enabled(t, pos));
        }
        dead.andWith(buf);
        return dead.andWith(getTop().not());//.andWith(wellformed());
    }

    /**
     * Calculates a BDD with all possible bad situations:
     *
     * 1) bad place reached 2) non determinism encountered 3) deadlock created
     *
     * @return BDD representing all bad situations
     */
    public BDD badSysDCS() {
//        System.out.println("bad");
//        BDDTools.printDecisionSets(baddcs(0).not(),  true);
//        System.out.println("end");
        return baddcs(0).orWith(getBufferedNDet().or(deadSysDCS(0)));
    }

    public BDD badStates() {
        return badSysDCS();
    }
// %%%%%%%%%%%%%%%%%%%%%%%%%%% END WINNING CONDITION %%%%%%%%%%%%%%%%%%%%%%%%%%% 

    @Override
    protected BDD envTransitionCP(Transition t) {
        return super.envTransitionCP(t).andWith(badStates().not()); // bad states don't have any successor
    }

    @Override
    protected BDD envTransitionNotCP(Transition t) {
        return super.envTransitionNotCP(t).andWith(badStates().not()); // bad states don't have any successors
    }

    @Override
    protected BDD sysTransitionCP(Transition t) {
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut        
        BDD sys1 = super.sysTransitionCP(t);
        // bad states don't have succesors
        sys1.andWith(badStates().not());
//        sys1.andWith(oldType2());//.andWith(wellformed(1));//.andWith(wellformedTransition()));
        return sys1;//.andWith(wellformed(1));//.andWith(wellformedTransition()));
    }

    @Override
    protected BDD sysTransitionNotCP(Transition t) {
        // todo: cheaper?
        // could be outside of the transition (move to envTransitionCP), since it fits for all transitions
        // but then calling this method e.g. for hasFired won't work as expected.
        // Only useable if it's not an mcut        
        BDD sys1 = super.sysTransitionNotCP(t);
        // bad states don't have succesors
        sys1.andWith(badStates().not());
//        sys1.andWith(oldType2());//.andWith(wellformed(1));//.andWith(wellformedTransition()));
        return sys1;//.andWith(wellformed(1));//.andWith(wellformedTransition()));
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    @Override
    protected BDD calcDCSs() throws CalculationInterruptedException {
        BDD trans = getBufferedEnvTransitions().or(getBufferedSystemTransitions());

        BDD Q = getZero();
        BDD Q_ = getInitialDCSs().andWith(getWellformed()); // seems to be the fastes only to add the getRepr completely at the end
//        BDD Q_ = getRepresentatives(getInitialDCSs().andWith(getWellformed())); // seems to be faster than without wellformed
//        BDD Q_ = getRepresentatives(getInitialDCSs());
//        try {
//            BDDTools.saveStates2Pdf("states", Q_.andWith(getWellformed()), this);
//        } catch (Exception e) {
//        }
        while (!Q_.equals(Q)) {
            if (Thread.currentThread().isInterrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            Q = Q_;
            // if it is an mcut or not is already coded in the transitions itself
            BDD succs = getSuccs(trans.and(Q));// seems to be faster than with representatives
//            succs = getRepresentatives(getSuccs(succs.and(Q))); // this seems to be very expensive
//            BDD symQ = getSuccs(getSymmetries().and(Q)); // symmetries saves the symmetric states in the successor           
//            succs.andWith(symQ.not());
            Q_ = Q.or(succs);
        }

        return getRepresentatives(Q.and(getWellformed()));
    }

    private BDD getRepresentatives(BDD states) {
        System.out.println("IN REPRI");
        BDD reps = getZero();
        BDD state = states.satOne(getFirstBDDVariables(), false);
//            BDDTools.printDecodedDecisionSets(state, this, true);
        while (!state.isZero()) {
            reps = reps.or(state);
            BDD syms = getSuccs(getSymmetries().and(state)); // with andWith uses very less memory, but takes significant longer (test it with DW3) 1:36 against over 3 minutes
//            BDDTools.printDecodedDecisionSets(syms, this, true);
            states.andWith(syms.not());
            state = states.satOne(getFirstBDDVariables(), false);
        }
        System.out.println("finish REPRI");
        return reps;
    }

    private BDD symmetries = null;

    private BDD getSymmetries() {
        if (symmetries == null) {
            symmetries = symmetries(syms);
        }
        return symmetries;
    }

    private BDD symmetries(Symmetries syms) {
        BDD symsBDD = getZero();
        SymmetryIterator symit = syms.iterator();
//        if (symit.hasNext()) {
//            symit.next(); // jump over identity
//        }
        for (SymmetryIterator iti = symit; iti.hasNext();) {
            Symmetry sym = iti.next();
//            System.out.println(sym.toString());
            BDD symm = getOne();
            // the symmetries for all places
            for (Place place : getGame().getPlaces()) {
                int partition = getSolvingObject().getGame().getPartition(place);
                // Calculate the symmetric place
                String id = HL2PGConverter.getOrigID(place);
                List<Color> col = HL2PGConverter.getColors(place);
                List<Color> colors = new ArrayList<>();
                for (int i = 0; i < col.size(); i++) {
                    colors.add(sym.get(col.get(i)));
                }
                Place newPlace = getGame().getPlace(HL2PGConverter.getPlaceID(id, colors));
                int newPartition = getSolvingObject().getGame().getPartition(newPlace);
                symm.andWith(codePlace(place, 0, partition).biimpWith(codePlace(newPlace, 1, newPartition)));
                if (partition != 0) { // no env place
                    // the symmetries for all transitions            
                    for (Transition t : place.getPostset()) {
                        int transId = getSolvingObject().getDevidedTransitions()[partition - 1].indexOf(t);
                        // Calculate the symmetric transition
                        String hlID = HL2PGConverter.getOrigID(t);
                        Valuation val = HL2PGConverter.getValuation(t);
                        Valuation newVal = new Valuation();
                        for (Map.Entry<Variable, Color> entry : val.entrySet()) {
                            Variable var = entry.getKey();
                            Color c = entry.getValue();
                            newVal.put(var, sym.get(c));
                        }
                        Transition tNew = getGame().getTransition(HL2PGConverter.getTransitionID(hlID, newVal));
                        int transIdNew = getSolvingObject().getDevidedTransitions()[newPartition - 1].indexOf(tNew);
                        symm.andWith(getFactory().ithVar(getTransitionDomain(0, partition - 1).vars()[transId]).biimpWith(getFactory().ithVar(getTransitionDomain(1, newPartition - 1).vars()[transIdNew])));
                    }
                    // symmetries for the top
                    symm.andWith(getTopDomain(0, partition - 1).buildEquals(getTopDomain(1, newPartition - 1)));
                }
            }
            symsBDD.orWith(symm);
        }
        return symsBDD;
    }

    /**
     * ATTENTION: This method can only be used if the places are devided into
     * the partitions according their color classes. It is cheaper than the
     * symmetries function because the places and transition loops are not
     * nested
     *
     * @param syms
     * @return
     */
    private BDD symmetriesProperlyPartitioned(Symmetries syms) {
        BDD symsBDD = getZero();
        for (SymmetryIterator iti = syms.iterator(); iti.hasNext();) {
            Symmetry sym = iti.next();
            BDD symm = getOne();
            // the symmetries for all places
            for (Place place : getGame().getPlaces()) {
                int partition = getSolvingObject().getGame().getPartition(place);
                // Calculate the symmetric place
                String id = HL2PGConverter.getOrigID(place);
                List<Color> col = HL2PGConverter.getColors(place);
                List<Color> colors = new ArrayList<>();
                for (int i = 0; i < col.size(); i++) {
                    colors.add(sym.get(col.get(i)));
                }
                Place newPlace = getGame().getPlace(HL2PGConverter.getPlaceID(id, colors));
                int newPartition = getSolvingObject().getGame().getPartition(newPlace);
                symm.andWith(codePlace(place, 0, partition).impWith(codePlace(newPlace, 1, newPartition)));
            }
            // the symmetries for all transitions            
            // todo: this approach only works when the symmetric transitions are in the same divided classes
            for (int i = 1; i < getSolvingObject().getMaxTokenCount(); ++i) { // todo: may this can be optimized by directly doing it int the loop for the places                
                for (Transition t : getSolvingObject().getDevidedTransitions()[i]) {
                    int id = getSolvingObject().getDevidedTransitions()[i - 1].indexOf(t);
                    // Calculate the symmetric place
                    String hlID = HL2PGConverter.getOrigID(t);
                    Valuation val = HL2PGConverter.getValuation(t);
                    Valuation newVal = new Valuation();
                    for (Map.Entry<Variable, Color> entry : val.entrySet()) {
                        Variable var = entry.getKey();
                        Color c = entry.getValue();
                        newVal.put(var, sym.get(c));
                    }
                    Transition tNew = getGame().getTransition(HL2PGConverter.getTransitionID(hlID, newVal));
                    int idNew = getSolvingObject().getDevidedTransitions()[i - 1].indexOf(tNew);
                    symm.andWith(getFactory().ithVar(getTransitionDomain(0, i - 1).vars()[id]).impWith(getFactory().ithVar(getTransitionDomain(1, i - 1).vars()[idNew])));
                }
            }
            symsBDD.orWith(symm);
        }
        return symsBDD;
    }

    /**
     * Returns the winning decisionsets for the system players
     *
     * @return
     * @throws uniolunisaar.adam.exceptions.pg.CalculationInterruptedException
     */
    @Override
    protected BDD calcWinningDCSs(Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        BDD fixedPoint = attractor(badStates(), true, distance).not().and(getBufferedDCSs());//fixpointOuter();
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()).andWith(getNotTop()), this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
    }

    /**
     * Overriden for marking the bad states.
     *
     * @return
     * @throws uniolunisaar.adam.exceptions.pg.CalculationInterruptedException
     */
    @Override
    public BDDGraph getGraphGame() throws CalculationInterruptedException {
        BDDGraph graph = super.getGraphGame();
        for (BDDState state : graph.getStates()) { // mark all special states
            if (!graph.getInitial().equals(state) && !badStates().and(state.getState()).isZero()) {
                state.setBad(true);
            }
        }
        return graph;
    }
// %%%%%%%%%%%%%%%%%%%%%%%%% END The relevant ability of the solver %%%%%%%%%%%%
}
