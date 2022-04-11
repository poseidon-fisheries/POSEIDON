package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.SetLocationValues;
import uk.ac.ox.oxfish.model.FishState;

/**
 * an abstract class that deals with planning modules that just repackage
 * the behaviour present in a DrawFromLocationValuePlannedActionGenerator but
 * need to take care into starting the location values with the right fisher
 */
public abstract class LocationValuePlanningModule implements PlanningModule {

    final private SetLocationValues<? extends AbstractSetAction> locationValues;

    final private DrawFromLocationValuePlannedActionGenerator<? extends PlannedAction> generator;


    public LocationValuePlanningModule(
            SetLocationValues<? extends AbstractSetAction> locationValues,
            DrawFromLocationValuePlannedActionGenerator<? extends PlannedAction> generator) {
        this.locationValues = locationValues;
        this.generator = generator;
    }

    @Override
    public PlannedAction chooseNextAction(Plan currentPlanSoFar) {
        return generator.drawNewPlannedAction();
    }

    @Override
    public boolean isStarted() {
        return generator.isReady();
    }

    @Override
    public void start(FishState model, Fisher fisher) {

        //start the location value if needed; else start the generator
        if(!locationValues.hasStarted())
            locationValues.start(model,fisher);
        generator.start();

    }

    /**
     * this is like the start(...) but gets called when we want the module to be aware that a new plan is starting
     *
     * @param state
     * @param fisher
     */
    @Override
    public void prepareForReplanning(FishState state, Fisher fisher) {
        Preconditions.checkArgument(locationValues.hasStarted());
        generator.start();
    }

    @Override
    public void turnOff(Fisher fisher) {
        locationValues.turnOff(fisher);

    }

    /**
     * returns the number of FADs in stock!
     * @param state
     * @param fisher
     * @return
     */
    @Override
    public abstract int maximumActionsInAPlan(FishState state, Fisher fisher) ;

}
