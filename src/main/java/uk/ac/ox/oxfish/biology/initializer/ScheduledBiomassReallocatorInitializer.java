package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.initializer.allocator.ScheduledBiomassRelocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.model.StepOrder.DAWN;

public class ScheduledBiomassReallocatorInitializer implements BiologyInitializer {

    private final ScheduledBiomassRelocator scheduledBiomassRelocator;
    private final List<SingleSpeciesBiomassInitializer> initializers;

    public ScheduledBiomassReallocatorInitializer(
        ScheduledBiomassRelocator scheduledBiomassRelocator,
        Collection<SingleSpeciesBiomassInitializer> initializers
    ) {
        this.scheduledBiomassRelocator = scheduledBiomassRelocator;
        this.initializers = ImmutableList.copyOf(initializers);
    }

    /**
     * if at least one species can live here, return a localBiomassBiology; else return a empty globalBiology
     */
    @Override
    public LocalBiology generateLocal(
        GlobalBiology globalBiology,
        SeaTile seaTile,
        MersenneTwisterFast random,
        int mapHeightInCells,
        int mapWidthInCells,
        NauticalMap map
    ) {
        assert !initializers.isEmpty();

        ImmutableList<LocalBiology> localBiologies =
            initializers.stream()
                .map(initializer ->
                    // We need to call `generateLocal` on each initializer for its side-effects
                    initializer.generateLocal(globalBiology, seaTile, random, mapHeightInCells, mapWidthInCells, map)
                )
                .collect(toImmutableList());

        // Just return the first BiomassLocalBiology, it doesn't matter which
        return localBiologies.stream()
            .filter(biology -> biology instanceof BiomassLocalBiology)
            .findFirst()
            .orElse(new EmptyLocalBiology());

    }

    @Override
    public void processMap(
        GlobalBiology biology,
        NauticalMap map,
        MersenneTwisterFast random,
        FishState fishState
    ) {
        initializers.forEach(initializer -> {
            initializer.setForceMovementOff(true);
            initializer.processMap(biology, map, random, fishState);
        });

        fishState.scheduleEveryStep(scheduledBiomassRelocator, DAWN);
    }

    @Override
    public GlobalBiology generateGlobal(MersenneTwisterFast rng, FishState fishState) {
        return new GlobalBiology(
            initializers.stream()
                .map(initializer -> initializer.generateGlobal(rng, fishState).getSpecie(0))
                .toArray(Species[]::new)
        );
    }

}
