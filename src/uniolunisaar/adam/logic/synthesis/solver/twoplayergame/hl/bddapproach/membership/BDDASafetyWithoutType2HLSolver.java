package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.membership;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.BDDSGGBuilder;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.util.benchmarks.synthesis.Benchmarks;
import uniolunisaar.adam.tools.Logger;

/**
 * Solves Petri games with a safety objective with BDDs. This solver can be used
 * when there is no possibillity for system player to play infinitely long
 * without any further interaction with the environment.
 *
 * @author Manuel Gieseking
 */
public class BDDASafetyWithoutType2HLSolver extends DistrSysBDDSolver<Safety> {

    private final Iterable<Symmetry> syms;

    /**
     * Creates a new Safety solver for a given game.
     *
     * @param obj
     * @param syms
     * @param opts - the options for the solver.
     * @throws NotSupportedGameException - Thrown if the given net is not
     * bounded.
     * @throws NetNotSafeException - Thrown if the given net is not safe.
     * @throws NoSuitableDistributionFoundException - Thrown if the given net is
     * not annotated to which token each place belongs and the algorithm was not
     * able to detect it on its own.
     * @throws
     * uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException
     */
    public BDDASafetyWithoutType2HLSolver(DistrSysBDDSolvingObject<Safety> obj, Iterable<Symmetry> syms, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(obj, opts);
        this.syms = syms;
//        for (Symmetry sym : syms) {
//            System.out.println(sym.toString());
//        }
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
        // if it is an mcut or not is already coded in the transitions itself            
        BDD trans = getBufferedEnvTransitions().or(getBufferedSystemTransitions());

        BDD Q = getZero();
        BDD Q_ = getInitialDCSs().andWith(getWellformed(0)); // seems to be the fastes only to add the getRepr completely at the end
//        BDD Q_ = getRepresentatives(getInitialDCSs().andWith(getWellformed())); // seems to be faster than without wellformed
//        BDD Q_ = getRepresentatives(getInitialDCSs());
//        try {
//            BDDTools.saveStates2Pdf("states", Q_.andWith(getWellformed()), this);
//        } catch (Exception e) {
//        }
        while (!Q_.equals(Q)) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            Q = Q_;

            BDD succs = getSuccs(trans.and(Q));// seems to be faster than with representatives
//            succs = getRepresentatives(getSuccs(succs.and(Q))); // this seems to be very expensive
//            BDD symQ = getSuccs(getSymmetries().and(Q)); // symmetries saves the symmetric states in the successor           
//            succs.andWith(symQ.not());
            Q_ = Q.or(succs);
        }

        return getRepresentatives(Q.and(getWellformed(0))).andWith(getWellformed(0));
//        return Q.andWith(getWellformed(0));
    }

    private BDD getRepresentatives(BDD states) throws CalculationInterruptedException {
        BDD reps = getZero();
        BDD state = states.satOne(getFirstBDDVariables(), false);
//            BDDTools.printDecodedDecisionSets(state, this, true);
        while (!state.isZero()) {
            reps = reps.or(state);
            BDD syms = getSymmetricStates(state);
//            BDDTools.printDecodedDecisionSets(syms, this, true);
            states.andWith(syms.not());
            state = states.satOne(getFirstBDDVariables(), false);
        }
        return reps;
    }

    private BDD symmetries = null;

    public BDD getSymmetries() throws CalculationInterruptedException {
        if (symmetries == null) {
            symmetries = symmetries(syms);
        }
        return symmetries;
    }

    public BDD getSymmetricStates(BDD state) throws CalculationInterruptedException {
        return getSuccs(getSymmetries().and(state));
    }

    /**
     * Calculates a BDD where
     *
     * V_sym Wedge_p (p <-> sym(p)' wedge post(p) <-> sym(post(p)')
     *
     * @param syms
     * @return
     */
    private BDD symmetries(Iterable<Symmetry> syms) throws CalculationInterruptedException {
        Logger.getInstance().addMessage("Calculation of symmetry BDD ...", "INTERMEDIATE_TIMING");
        long time = System.currentTimeMillis();

//        BDD start = getWellformed(0).andWith(getWellformed(1)); // this seems to be faster then just getOne()
        BDD symsBDD = getZero();
        Iterator<Symmetry> symit = syms.iterator();
//        if (symit.hasNext()) {
//            symit.next(); // jump over identity
//        }
        for (Iterator<Symmetry> iti = symit; iti.hasNext();) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            Symmetry sym = iti.next();

//            System.out.println(sym.toString());
//            BDD symm = getOne();
            BDD symm = getWellformed(0).andWith(getWellformed(1)); // this seems to be faster then just getOne()
//            BDD symm = start.id();
            // the symmetries for all places
            for (Place llplace : getGame().getPlaces()) {
                int partition = getSolvingObject().getGame().getPartition(llplace);
                // Calculate the symmetric place
                String id = HL2PGConverter.getOrigID(llplace);
                List<Color> oldcolors = HL2PGConverter.getColors(llplace);
                // cal the new colors
                List<Color> newcolors = new ArrayList<>();
                for (int i = 0; i < oldcolors.size(); i++) {
                    newcolors.add(sym.get(oldcolors.get(i)));
                }
                Place newPlace = getGame().getPlace(HL2PGConverter.getPlaceID(id, newcolors));
                int newPartition = getSolvingObject().getGame().getPartition(newPlace);
                symm.andWith(codePlace(llplace, 0, partition).biimpWith(codePlace(newPlace, 1, newPartition)));
                if (partition != 0) { // no env place
                    // the symmetries for all transitions            
                    for (Transition t : llplace.getPostset()) {
                        int transId = getSolvingObject().getDevidedTransitions()[partition - 1].indexOf(t);
                        // Calculate the symmetric transition
                        String hlID = HL2PGConverter.getOrigID(t);
                        Valuation oldval = HL2PGConverter.getValuation(t);
                        Valuation newVal = new Valuation();
                        for (Map.Entry<Variable, Color> entry : oldval.entrySet()) {
                            Variable var = entry.getKey();
                            Color c = entry.getValue();
                            newVal.put(var, sym.get(c));
                        }
                        Transition tNew = HL2PGConverter.getTransition(getGame(), hlID, newVal);
                        int transIdNew = getSolvingObject().getDevidedTransitions()[newPartition - 1].indexOf(tNew);
                        symm.andWith(getFactory().ithVar(getTransitionDomain(0, partition - 1).vars()[transId]).biimpWith(getFactory().ithVar(getTransitionDomain(1, newPartition - 1).vars()[transIdNew])));
                    }
                    // symmetries for the top
                    symm.andWith(getTopDomain(0, partition - 1).buildEquals(getTopDomain(1, newPartition - 1)));
                }
            }
            symsBDD.orWith(symm);
        }
        Logger.getInstance().addMessage(".... finished calculation of symmetry BDD (" + (System.currentTimeMillis() - time) / 1000.0f + ")", "INTERMEDIATE_TIMING");

        return symsBDD;
    }

    /**
     * ATTENTION: This method can only be used if the places are divided into
     * the partitions according their color classes. It is cheaper than the
     * symmetries function because the places and transition loops are not
     * nested
     *
     * @param syms
     * @return
     */
    private BDD symmetriesProperlyPartitioned(Symmetries syms) {
        BDD symsBDD = getZero();
        for (Iterator<Symmetry> iti = syms.iterator(); iti.hasNext();) {
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
                    Transition tNew = HL2PGConverter.getTransition(getGame(), hlID, newVal);
                    int idNew = getSolvingObject().getDevidedTransitions()[i - 1].indexOf(tNew);
                    symm.andWith(getFactory().ithVar(getTransitionDomain(0, i - 1).vars()[id]).impWith(getFactory().ithVar(getTransitionDomain(1, i - 1).vars()[idNew])));
                }
            }
            symsBDD.orWith(symm);
        }
        return symsBDD;
    }

    /**
     * With abortion = true, the return value is null iff some abortionState is
     * contained in the attractor.
     *
     * @param F
     * @param p1
     * @param gameGraph
     * @param distance
     * @param withAbortion
     * @param abortionStates
     * @return
     * @throws CalculationInterruptedException
     */
    @Override
    protected BDD attractor(BDD F, boolean p1, BDD gameGraph, Map<Integer, BDD> distance, boolean withAbortion, BDD abortionStates) throws CalculationInterruptedException {
        // also all symmetric are bad
        gameGraph = getSuccs(gameGraph.and(getSymmetries()));
        // Calculate the possibly restricted transitions to the given game graph
        BDD graphSuccs = super.shiftFirst2Second(gameGraph);
        BDD envTrans = getBufferedEnvTransitions().and(gameGraph).and(graphSuccs);
        BDD sysTrans = getBufferedSystemTransitions().and(gameGraph).and(graphSuccs);

        BDD Q = getZero();
        BDD Q_ = F;
        int i = 0;
        while (!Q_.equals(Q)) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            if (distance != null) {
                distance.put(i++, Q_);
            }
            Q = Q_;
            BDD pre = p1 ? pre(Q, sysTrans, envTrans) : pre(Q, envTrans, sysTrans);
            pre = getSuccs(pre.and(getSymmetries())).and(getBufferedDCSs());
            if (withAbortion && !pre.and(abortionStates).isZero()) {
                return null;
            }
            Q_ = pre.or(Q);
        }
        return Q_.andWith(getWellformed(0));
    }

    /**
     * Returns the winning decisionsets for the system players
     *
     * @return
     * @throws uniolunisaar.adam.exceptions.CalculationInterruptedException
     */
    @Override
    protected BDD calcWinningDCSs(Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        // todo: it should be expensive to calculate the buffered dcss!? Why did I chose to use it? BECAUSE THIS SEEMS REALLY TO BE FASTER?
        BDD fixedPoint = attractor(badStates(), true, getBufferedDCSs(), distance, false, null).not().and(getBufferedDCSs());//fixpointOuter();
//        BDD fixedPoint = attractor(badStates(), true, getFactory().one(), distance).not();
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()).andWith(getNotTop()), this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
    }

    @Override
    public BDDGraph calculateGraphGame() throws CalculationInterruptedException {
        return BDDSGGBuilder.getInstance().builtGraph(this);
    }

    @Override
    public BDDGraph calculateGraphStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
        BDDGraph g = BDDSGGBuilder.getInstance().builtGraphStrategy(this);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.GRAPH_STRAT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
        return g;
    }

    @Override
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return super.calculateStrategy();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% END The relevant ability of the solver %%%%%%%%%%%%
    @Override
    protected BDD calcBadDCSs() {
        return badStates();
    }

    @Override
    protected BDD calcSpecialDCSs() {
        return getFactory().zero();
    }

    /**
     * Safety game graphs don't have a special state
     *
     * @param state
     * @return
     */
    @Override
    public boolean isSpecialState(BDD state) {
        return false;
    }
}
