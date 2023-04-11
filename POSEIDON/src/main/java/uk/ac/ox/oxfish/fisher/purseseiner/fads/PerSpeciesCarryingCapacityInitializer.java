package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class PerSpeciesCarryingCapacityInitializer
    extends AbstractCarryingCapacityInitializer<PerSpeciesCarryingCapacity> {

    private final Map<? extends Species, ? extends DoubleParameter> carryingCapacities;

    public PerSpeciesCarryingCapacityInitializer(
        final double probabilityOfFadBeingDud,
        final Map<? extends Species, ? extends DoubleParameter> carryingCapacities
    ) {
        super(probabilityOfFadBeingDud);
        this.carryingCapacities = carryingCapacities;
    }

    @Override
    protected PerSpeciesCarryingCapacity makeDud() {
        return new PerSpeciesCarryingCapacity(
            carryingCapacities
                .keySet()
                .stream()
                .collect(toImmutableMap(
                    identity(),
                    species -> 0.0
                ))
        );
    }

    @Override
    protected PerSpeciesCarryingCapacity makeCarryingCapacity(final MersenneTwisterFast rng) {
        return new PerSpeciesCarryingCapacity(
            carryingCapacities
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().applyAsDouble(rng)
                ))
        );
    }
}
