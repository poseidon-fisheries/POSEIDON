package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observer;

import java.util.function.Predicate;

/**
 * counts for certain actions (and their total catch) when they pass a predicate. Useful for studying only actions that happen
 * in a specific area.
 * <p>
 * Following convention here assuming that we start ourselves to register gatherers but somebody else has already registered
 * us as observers in the fad manager!
 */
public class LocalizedActionCounter implements Observer<AbstractFadSetAction>, AdditionalStartable {

    private final Counter validActions;

    private final Predicate<AbstractFadSetAction> passThisIfYouWantToBeCounted;


    private final String counterName;

    public LocalizedActionCounter(
        Predicate<AbstractFadSetAction> passThisIfYouWantToBeCounted,
        String counterName
    ) {
        this.passThisIfYouWantToBeCounted = passThisIfYouWantToBeCounted;
        this.counterName = counterName;
        validActions = new Counter(IntervalPolicy.EVERY_YEAR);
        validActions.addColumn("Number of Actions");
        validActions.addColumn("Total Catch");
    }


    @Override
    public void observe(AbstractFadSetAction observable) {
        if (passThisIfYouWantToBeCounted.test(observable)) {
            validActions.count("Number of Actions", 1.0);
            if (observable.getCatchesKept().isPresent())
                validActions.count(
                    "Total Catch",
                    ((Catch) observable.getCatchesKept().get()).getTotalWeight()
                );

        }
    }

    @Override
    public void start(FishState model) {

        validActions.start(model);
        model.getYearlyDataSet().registerGatherer(counterName + ": Number of Actions",
            new Gatherer<FishState>() {
                @Override
                public Double apply(FishState fishState) {
                    return getNumberOfActionsThisYearSoFar();
                }
            }, Double.NaN
        );
        model.getYearlyDataSet().registerGatherer(counterName + ": Total Catch",
            new Gatherer<FishState>() {
                @Override
                public Double apply(FishState fishState) {
                    return getTotalCatchThisYearSoFar();
                }
            }, Double.NaN
        );

    }

    public double getNumberOfActionsThisYearSoFar() {
        return validActions.getColumn("Number of Actions");
    }

    public double getTotalCatchThisYearSoFar() {
        return validActions.getColumn("Total Catch");
    }

    @Override
    public void turnOff() {
        validActions.turnOff();
    }
}
