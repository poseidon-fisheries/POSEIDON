package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbundanceFadInitializerFactory
    extends FadInitializerFactory<AbundanceLocalBiology, AbundanceAggregatingFad> {

    private AbundanceFiltersFactory abundanceFiltersFactory;

    public AbundanceFadInitializerFactory(
        final CarryingCapacityInitializerFactory carryingCapacityInitializerFactory,
        final Map<String, DoubleParameter> catchabilities,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter fishReleaseProbabilityInPercent,
        final AbundanceFiltersFactory abundanceFiltersFactory
    ) {
        super(
            carryingCapacityInitializerFactory,
            catchabilities,
            daysInWaterBeforeAttraction,
            fishReleaseProbabilityInPercent
        );
        this.abundanceFiltersFactory = checkNotNull(abundanceFiltersFactory);
    }

    protected AbundanceFadInitializerFactory(
        final CarryingCapacityInitializerFactory<?> carryingCapacitiesFactory,
        final Map<String, DoubleParameter> catchabilities,
        final AbundanceFiltersFactory abundanceFiltersFactory
    ) {
        super(carryingCapacitiesFactory, catchabilities);
        this.abundanceFiltersFactory = checkNotNull(abundanceFiltersFactory);
    }

    public AbundanceFadInitializerFactory() {

    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        invalidateCache();
        this.abundanceFiltersFactory = checkNotNull(abundanceFiltersFactory);
    }

}
