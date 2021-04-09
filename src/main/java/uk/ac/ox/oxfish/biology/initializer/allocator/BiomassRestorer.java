package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateSteppable;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.model.StepOrder.DAWN;
import static uk.ac.ox.oxfish.model.StepOrder.POLICY_UPDATE;

/**
 * This class does a similar job as the various implementations of
 * {@link uk.ac.ox.oxfish.biology.complicated.BiologyResetter},
 * but it relies on a {@link BiomassReallocator} and deals with
 * all species at once.
 */
public class BiomassRestorer implements AdditionalStartable {

    private final BiomassReallocator biomassReallocator;
    private final Map<Integer, Integer> schedule;

    /**
     * @param biomassReallocator The {@link BiomassReallocator} that will distribute the fish when biomass is restored
     * @param schedule           A map from the step to record the biomass from the step to restore the recorded biomass
     */
    BiomassRestorer(
        final BiomassReallocator biomassReallocator,
        final Map<Integer, Integer> schedule
    ) {
        this.biomassReallocator = biomassReallocator;
        this.schedule = ImmutableMap.copyOf(schedule);
    }

    @Override
    public void start(final FishState fishState) {
        schedule.forEach((recordingStep, restoringStep) ->
            // record the biomass at dawn, before anything else happens
            schedule(fishState, recordingStep, DAWN, fishState1 -> {
                System.out.println("Taking biomass record at step " + fishState1.getStep());
                final ImmutableMap<String, Double> recordedBiomass = recordBiomass(fishState1);
                // Schedule the biomass restoration at POLICY_UPDATE step order so it runs after the grower
                // at BIOLOGY_PHASE step order, but before the data gatherers at later orders
                schedule(fishState1, restoringStep, POLICY_UPDATE, fishState2 -> {
                    System.out.printf("Restoring biomass recorded at step %d at step %d\n", recordingStep, fishState2.getStep());
                    restoreBiomass(recordedBiomass, fishState2);
                });
            })
        );
    }

    private static void schedule(
        final FishState fishState,
        final int step,
        final StepOrder stepOrder,
        @SuppressWarnings("TypeMayBeWeakened") final FishStateSteppable stepper
    ) {
        fishState.schedule.scheduleOnce(step, stepOrder.ordinal(), stepper);
    }

    private static ImmutableMap<String, Double> recordBiomass(final FishState fishState) {
        final FadMap fadMap = fishState.getFadMap();
        final NauticalMap nauticalMap = fishState.getMap();
        return fishState.getBiology().getSpecies().stream().collect(toImmutableMap(
            Species::getName,
            species -> nauticalMap.getTotalBiomass(species) + (fadMap == null ? 0 : fadMap.getTotalBiomass(species))
        ));
    }

    private void restoreBiomass(final Map<String, Double> recordedBiomass, final FishState fishState) {
        final GlobalBiology globalBiology = fishState.getBiology();
        final NauticalMap nauticalMap = fishState.getMap();
        final FadMap fadMap = fishState.getFadMap();
        // if we have a snapshot of the biomass, we need to subtract the biomass
        // that's currently under FADs in order to avoid reallocating it
        final ImmutableMap<String, Double> biomassToReallocate =
            recordedBiomass.entrySet().stream().collect(toImmutableMap(
                Map.Entry::getKey,
                entry -> entry.getValue() -
                    (fadMap == null ? 0 : fadMap.getTotalBiomass(globalBiology.getSpecie(entry.getKey())))
            ));
        biomassReallocator.reallocate(
            fishState.getStep(),
            biomassToReallocate,
            globalBiology,
            nauticalMap.getAllSeaTilesExcludingLandAsList()
        );
    }

}
