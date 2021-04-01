package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.collect.ImmutableMap;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

/**
 * Redistributes the biomass around according to a "schedule" that maps a simulation step to a grid index.
 * The biomass grids are normalized upon construction, but care must be taken to have the all non-empty
 * grid locations match with {@code SeaTile} that have a BiomassLocal biology, otherwise biomass will be lost.
 * The biomass grids are stored in mutable {@code DoubleGrid2D} fields, but those should never be mutated, so
 * the class is safe to share between parallel simulations. Note that the {@code reallocate} method mutates
 * the tiles biomass arrays directly.
 */
public class ScheduledBiomassRelocator implements Steppable {

    private final Map<Integer, Map<String, DoubleGrid2D>> biomassGrids;
    private final int period;

    /**
     * @param biomassGrids A map from time step to species names to biomass grid
     * @param period       The number to use as modulo for looping the schedule (normally 365)
     */
    public ScheduledBiomassRelocator(
        Map<Integer, Map<String, DoubleGrid2D>> biomassGrids,
        int period
    ) {
        this.biomassGrids = biomassGrids;
        this.period = period;
    }

    public Map<Integer, Map<String, DoubleGrid2D>> getBiomassGrids() {
        return biomassGrids;
    }

    public int getPeriod() {
        return period;
    }

    /**
     * This is meant to be executed every step, but will do the reallocation if we have one scheduled on that step
     */
    @Override
    public void step(SimState simState) {
        FishState fishState = (FishState) simState;
        if (fishState.getStep() > 0) // skip first step; we must rely on the initial allocators for that one
            reallocate(fishState.getStep() % period, fishState.getBiology(), fishState.getMap());
    }

    /**
     * Reallocates biomass by mutating the biomass array of sea tiles directly.
     * Only affects tiles with a {@code BiomassLocalBiology}.
     */
    public void reallocate(
        int gridIndex,
        GlobalBiology globalBiology,
        NauticalMap nauticalMap
    ) {
        Optional
            .ofNullable(biomassGrids.get(gridIndex)) // only run if we have a grid for that step
            .ifPresent(grids -> reallocate(grids, globalBiology, nauticalMap));
    }

    /**
     * Reallocates biomass but mutating the biomass array of sea tiles directly.
     * Only affects tiles with a {@code BiomassLocalBiology}.
     */
    public void reallocate(
        Map<String, DoubleGrid2D> grids,
        GlobalBiology globalBiology,
        NauticalMap nauticalMap
    ) {

        ImmutableMap<Integer, DoubleGrid2D> indexedGrids =
            grids.entrySet().stream().collect(toImmutableMap(
                entry -> globalBiology.getSpecie(entry.getKey()).getIndex(),
                entry -> {
                    double totalBiomass = nauticalMap.getTotalBiomass(globalBiology.getSpecie(entry.getKey()));
                    return new DoubleGrid2D(entry.getValue()).multiply(totalBiomass);
                }
            ));

        nauticalMap.getAllSeaTilesExcludingLandAsList()
            .stream()
            .filter(seaTile -> seaTile.getBiology() instanceof BiomassLocalBiology)
            .forEach(seaTile -> {
                double[] biomass = ((BiomassLocalBiology) seaTile.getBiology()).getCurrentBiomass();
                indexedGrids.forEach((i, grid) -> biomass[i] = grid.get(seaTile.getGridX(), seaTile.getGridY()));
            });
    }

}
