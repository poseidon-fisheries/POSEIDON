package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.SetLocationValues;
import uk.ac.ox.oxfish.model.FishState;

/**
 * an abstract class that deals with planning modules that just repackage the behaviour present in a
 * DrawFromLocationValuePlannedActionGenerator but need to take care into starting the location values with the right
 * fisher
 */
public abstract class LocationValuePlanningModule<B extends LocalBiology>
    implements PlanningModule {

    final private SetLocationValues<?> locationValues;

    final private DrawFromLocationValuePlannedActionGenerator<? extends PlannedAction> generator;

    public LocationValuePlanningModule(
        final SetLocationValues<?> locationValues,
        final DrawFromLocationValuePlannedActionGenerator<? extends PlannedAction> generator
    ) {
        this.locationValues = locationValues;
        this.generator = generator;
    }

    @Override
    public PlannedAction chooseNextAction(final Plan currentPlanSoFar) {
        return generator.drawNewPlannedAction();
    }

    @Override
    public boolean isStarted() {
        return generator.isReady();
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {

        // start the location value if needed; else start the generator
        if (!locationValues.hasStarted())
            locationValues.start(model, fisher);
        generator.start();

    }

    /**
     * this is like the start(...) but gets called when we want the module to be aware that a new plan is starting
     */
    @Override
    public void prepareForReplanning(
        final FishState state,
        final Fisher fisher
    ) {
        Preconditions.checkArgument(locationValues.hasStarted());
        generator.start();
    }

    @Override
    public void turnOff(final Fisher fisher) {
        locationValues.turnOff(fisher);

    }

    /**
     * returns the number of FADs in stock!
     */
    @Override
    public abstract int maximumActionsInAPlan(
        FishState state,
        Fisher fisher
    );

}
