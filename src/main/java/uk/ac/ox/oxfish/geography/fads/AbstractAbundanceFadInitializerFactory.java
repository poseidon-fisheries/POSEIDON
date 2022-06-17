package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LogisticFishAbundanceAttractor;
import uk.ac.ox.oxfish.model.FishState;


import java.util.Map;
import java.util.function.DoubleSupplier;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class  AbstractAbundanceFadInitializerFactory
        extends FadInitializerFactory<AbundanceLocalBiology, AbundanceFad> implements PluggableSelectivity {

    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();

    /**
     * Empty constructor for YAML
     */
    public AbstractAbundanceFadInitializerFactory() {
    }

    public AbstractAbundanceFadInitializerFactory(final String... speciesNames) {
        super(speciesNames);
    }

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        checkNotNull(selectivityFilters);
        checkNotNull(getSpeciesCodes());
        final MersenneTwisterFast rng = fishState.getRandom();
        final double totalCarryingCapacity = getTotalCarryingCapacity().apply(rng);
        DoubleSupplier capacityGenerator = buildCapacityGenerator(rng, totalCarryingCapacity);

        return new AbundanceFadInitializer(
                fishState.getBiology(),
                capacityGenerator,
                makeFishAttractor(fishState, rng),
                getFishReleaseProbabilityInPercent().apply(rng) / 100d,
                fishState::getStep
        );
    }

    @NotNull
    abstract protected DoubleSupplier buildCapacityGenerator(MersenneTwisterFast rng, double maximumCarryingCapacity);

    private LogisticFishAbundanceAttractor makeFishAttractor(
            final FishState fishState,
            final MersenneTwisterFast rng
    ) {
        return new LogisticFishAbundanceAttractor(
                fishState.getRandom(),
                processParameterMap(getCompressionExponents(), fishState.getBiology(), rng),
                processParameterMap(getAttractableBiomassCoefficients(), fishState.getBiology(), rng),
                processParameterMap(getBiomassInteractionsCoefficients(), fishState.getBiology(), rng),
                processParameterMap(getGrowthRates(), fishState.getBiology(), rng),
                getSelectivityFilters()
        );
    }

    @SuppressWarnings("WeakerAccess")
    public Map<Species, NonMutatingArrayFilter> getSelectivityFilters() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return selectivityFilters;
    }

    public void setSelectivityFilters(final Map<Species, NonMutatingArrayFilter> selectivityFilters) {
        this.selectivityFilters = ImmutableMap.copyOf(selectivityFilters);
    }


}



