package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.FAD;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public abstract class DiscretizedOwnFadPlanningModule implements PlanningModule {
    
    private static final int MAX_OWN_FAD_SETS = 100;
    final protected OwnFadSetDiscretizedActionGenerator optionsGenerator;
    protected double speedInKmPerHours;
    private NauticalMap map;
    private Fisher fisher;

    private FishState fishState;


    public DiscretizedOwnFadPlanningModule(
        final MapDiscretization discretization,
        final double minimumValueOfFadBeforeBeingPickedUp
    ) {
        this(
            new OwnFadSetDiscretizedActionGenerator(
                discretization,
                minimumValueOfFadBeforeBeingPickedUp
            )
        );
    }

    public DiscretizedOwnFadPlanningModule(
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        this.optionsGenerator = optionsGenerator;
    }

    @Override
    public PlannedAction chooseNextAction(final Plan currentPlanSoFar) {


        return chooseFadSet(
            currentPlanSoFar,
            fisher,
            fishState,
            map,
            optionsGenerator
        );

    }

    protected abstract PlannedAction chooseFadSet(
        Plan currentPlanSoFar,
        Fisher fisher,
        FishState model,
        NauticalMap map,
        OwnFadSetDiscretizedActionGenerator optionsGenerator
    );

    @Override
    public void turnOff(final Fisher fisher) {
        map = null;
        this.fisher = null;
        this.fishState = null;

    }

    @Override
    public boolean isStarted() {
        return this.map != null;
    }

    /**
     * this is like the start(...) but gets called when we want the module to be aware that a new plan is starting
     *
     * @param state
     * @param fisher
     */
    @Override
    public void prepareForReplanning(final FishState state, final Fisher fisher) {
        start(state, fisher);
        speedInKmPerHours = fisher.getBoat().getSpeedInKph();
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        optionsGenerator.startOrReset(
            getFadManager(fisher),
            model.getRandom(),
            model.getMap()
        );
        map = model.getMap();
        speedInKmPerHours = fisher.getBoat().getSpeedInKph();
        this.fisher = fisher;
        this.fishState = model;

    }

    /**
     * if a plan is about to start, how many times are we allowed to call this planning module (it may fail before
     * then, the
     * point of this function is to deal with regulations or other constraints)
     *
     * @param state
     * @param fisher
     * @return
     */
    @Override
    public int maximumActionsInAPlan(final FishState state, final Fisher fisher) {
        return getFadManager(fisher).numberOfPermissibleActions(
            FAD, MAX_OWN_FAD_SETS
        );
    }

}
