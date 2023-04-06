package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.PerSpeciesCarryingCapacitiesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbundanceFadInitializerFactory
    extends FadInitializerFactory<AbundanceLocalBiology, AbundanceFad> {

    private AbundanceFiltersFactory abundanceFiltersFactory;

    public AbundanceFadInitializerFactory(
        final PerSpeciesCarryingCapacitiesFactory carryingCapacitiesFactory,
        final Map<String, DoubleParameter> catchabilities,
        final DoubleParameter fishValueCalculatorStandardDeviation,
        final DoubleParameter fadDudRate,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter maximumDaysAttractions,
        final DoubleParameter fishReleaseProbabilityInPercent,
        final AbundanceFiltersFactory abundanceFiltersFactory
    ) {
        super(
            carryingCapacitiesFactory,
            catchabilities,
            fishValueCalculatorStandardDeviation,
            fadDudRate,
            daysInWaterBeforeAttraction,
            maximumDaysAttractions,
            fishReleaseProbabilityInPercent
        );
        this.abundanceFiltersFactory = checkNotNull(abundanceFiltersFactory);
    }

    protected AbundanceFadInitializerFactory(
        final PerSpeciesCarryingCapacitiesFactory carryingCapacitiesFactory,
        final Map<String, DoubleParameter> catchabilities,
        final AbundanceFiltersFactory abundanceFiltersFactory
    ) {
        super(carryingCapacitiesFactory, catchabilities);
        this.abundanceFiltersFactory = checkNotNull(abundanceFiltersFactory);
    }

    public AbundanceFadInitializerFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory
    ) {
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
