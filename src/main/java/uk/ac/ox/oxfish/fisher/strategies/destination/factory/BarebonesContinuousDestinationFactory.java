package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PeriodHabitContinuousExtractor;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;

/**
 * Created by carrknight on 7/17/17.
 */
public class BarebonesContinuousDestinationFactory extends BarebonesLogitDestinationFactory {

    @NotNull
    @Override
    public ObservationExtractor buildHabitExtractor(
            MapDiscretization discretization, int period) {
        return new PeriodHabitContinuousExtractor(discretization,period);

    }
}
