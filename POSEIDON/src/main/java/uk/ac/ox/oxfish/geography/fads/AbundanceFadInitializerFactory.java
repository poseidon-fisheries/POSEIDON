package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbundanceFadInitializerFactory
    extends FadInitializerFactory<AbundanceLocalBiology, AbundanceAggregatingFad> {

    private AbundanceFiltersFactory abundanceFilters;

    public AbundanceFadInitializerFactory(
        final CarryingCapacityInitializerFactory<?> carryingCapacityInitializerFactory,
        final Map<String, DoubleParameter> catchabilities,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter fishReleaseProbabilityInPercent,
        final AbundanceFiltersFactory abundanceFilters
    ) {
        super(
            carryingCapacityInitializerFactory,
            catchabilities,
            daysInWaterBeforeAttraction,
            fishReleaseProbabilityInPercent
        );
        this.abundanceFilters = checkNotNull(abundanceFilters);
    }

    protected AbundanceFadInitializerFactory(
        final CarryingCapacityInitializerFactory<?> carryingCapacitiesFactory,
        final Map<String, DoubleParameter> catchabilities,
        final AbundanceFiltersFactory abundanceFilters
    ) {
        super(carryingCapacitiesFactory, catchabilities);
        this.abundanceFilters = checkNotNull(abundanceFilters);
    }

    public AbundanceFadInitializerFactory() {

    }

    public AbundanceFiltersFactory getAbundanceFilters() {
        return abundanceFilters;
    }

    public void setAbundanceFilters(final AbundanceFiltersFactory abundanceFilters) {
        invalidateCache();
        this.abundanceFilters = checkNotNull(abundanceFilters);
    }

}
