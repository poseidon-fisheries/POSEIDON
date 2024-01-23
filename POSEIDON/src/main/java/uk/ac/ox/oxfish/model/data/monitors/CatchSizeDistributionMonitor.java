package uk.ac.ox.oxfish.model.data.monitors;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.KsTestAccumulator;

import javax.measure.quantity.Dimensionless;
import java.util.Map;

import static tech.units.indriya.AbstractUnit.ONE;
import static uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy.EVERY_YEAR;

public class CatchSizeDistributionMonitor<A extends AbstractSetAction>
    extends AbstractMonitor<AbstractSetAction, Double, Dimensionless> {

    private static final long serialVersionUID = 2860152553029319065L;
    private final Class<A> actionClass;
    private final Species species;

    public CatchSizeDistributionMonitor(
        final Class<A> actionClass,
        final Species species,
        final Map<Integer, double[]> empiricalDistributionPerYear
    ) {
        super(
            species.getName() + " catches from " + ActionClass.classMap.get(actionClass).name() + " sets",
            EVERY_YEAR,
            () -> new KsTestAccumulator(empiricalDistributionPerYear),
            ONE,
            "Kolmogorov-Smirnov test statistic"
        );
        this.actionClass = actionClass;
        this.species = species;
    }

    @Override
    public Iterable<Double> extractValues(final AbstractSetAction observable) {
        if (actionClass.isInstance(observable)) {
            return ImmutableList.of(
                observable
                    .getCatchesKept()
                    .map(catchesKept -> catchesKept.getWeightCaught(species))
                    .orElse(0.0)
            );
        } else {
            return ImmutableList.of();
        }
    }
}
