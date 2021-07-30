package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.model.StepOrder.DAWN;
import static uk.ac.ox.oxfish.model.StepOrder.POLICY_UPDATE;

import java.util.Map;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectArrayMessage;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This class does a similar job as the various implementations of {@link
 * uk.ac.ox.oxfish.biology.complicated.BiologyResetter}, but it relies on a {@link
 * BiomassReallocator} and deals with all species at once.
 */
public class BiomassRestorer extends Restorer<BiomassReallocator, Double> {

    private static final Logger logger = LogManager.getLogger("biomass_events");

    /**
     * Create a new BiomassRestorer.
     *
     * @param biomassReallocator The {@link BiomassReallocator} that will distribute the fish when
     *                           biomass is restored
     * @param schedule           A map from the step to record the biomass from the step to restore
     *                           the recorded biomass
     */
    BiomassRestorer(
        final BiomassReallocator biomassReallocator,
        final Map<Integer, Integer> schedule
    ) {
        super(biomassReallocator, schedule);
    }

     @Override Map<Species, Double> record(final FishState fishState) {

        final Map<Species, Double> recordedBiomass = fishState.getTotalBiomasses();

        recordedBiomass.forEach((species, biomass) ->
            logger.debug(new ObjectArrayMessage(
                fishState.getStep(),
                DAWN,
                "MEMORIZE_FOR_RESTORE",
                species,
                biomass,
                biomass
            )));

        return recordedBiomass;
    }

    @Override void restore(
        final Map<Species, Double> records,
        final FishState fishState
    ) {
        final GlobalBiology globalBiology = fishState.getBiology();
        final NauticalMap nauticalMap = fishState.getMap();
        final FadMap fadMap = fishState.getFadMap();

        // if we have a snapshot of the biomass, we need to subtract the biomass
        // that's currently under FADs in order to avoid reallocating it

        final Map<Species, Double> biomassUnderFads =
            fishState.getBiology().getSpecies().stream().collect(toImmutableMap(
                identity(),
                species -> fadMap == null ? 0 : fadMap.getTotalBiomass(species)
            ));

        final Map<Species, Double> biomassToReallocate =
            records.entrySet().stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> entry.getValue() - biomassUnderFads.get(entry.getKey())
                ));

        final Map<Species, Double> biomassBefore = fishState.getTotalBiomasses();

        reallocator.reallocate(
            fishState,
            globalBiology,
            nauticalMap,
            biomassToReallocate
        );

        fishState.getTotalBiomasses().forEach((species, biomassAfter) ->
            logger.debug(() -> new ObjectArrayMessage(
                fishState.getStep(),
                POLICY_UPDATE,
                "RESTORE",
                species.getName(),
                biomassBefore.get(species),
                biomassAfter
            ))
        );
    }
}
