package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.List;

public abstract class DiscretizedOwnFadPlanningModule implements PlanningModule {


    private static final int MAX_OWN_FAD_SETS = 1000;
    final protected OwnFadSetDiscretizedActionGenerator optionsGenerator;

    private NauticalMap map;

    protected double speedInKmPerHours;

    private Fisher fisher;

    private FishState fishState;


    public DiscretizedOwnFadPlanningModule(
            MapDiscretization discretization,
            double minimumValueOfFadBeforeBeingPickedUp) {
        this( new OwnFadSetDiscretizedActionGenerator(discretization,
                        minimumValueOfFadBeforeBeingPickedUp));
    }

    public DiscretizedOwnFadPlanningModule(
            OwnFadSetDiscretizedActionGenerator optionsGenerator) {
        this.optionsGenerator =
                optionsGenerator;
    }

    @Override
    public PlannedAction chooseNextAction(Plan currentPlanSoFar) {


        return chooseFadSet(currentPlanSoFar,
                fisher,
                fishState,
                map,
                optionsGenerator);

    }

    protected abstract PlannedAction chooseFadSet(Plan currentPlanSoFar,
                                                  Fisher fisher,
                                                  FishState model,
                                                  NauticalMap map,
                                                  OwnFadSetDiscretizedActionGenerator optionsGenerator
                                                  );


    @Override
    public void start(FishState model, Fisher fisher) {
        optionsGenerator.startOrReset(
                FadManager.getFadManager(fisher),
                model.getRandom(),
                model.getMap());
        map = model.getMap();
        speedInKmPerHours = fisher.getBoat().getSpeedInKph();
        this.fisher=fisher;
        this.fishState = model;

    }

    @Override
    public void turnOff(Fisher fisher) {
        map=null;
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
    public void prepareForReplanning(FishState state, Fisher fisher) {
        start(state,fisher);
        speedInKmPerHours = fisher.getBoat().getSpeedInKph();
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
    public int maximumActionsInAPlan(FishState state, Fisher fisher) {

        return
                Math.min(
                        FadManager.getFadManager(fisher).getNumberOfRemainingYearlyActions(FadSetAction.class),
                        MAX_OWN_FAD_SETS);


    }


}
