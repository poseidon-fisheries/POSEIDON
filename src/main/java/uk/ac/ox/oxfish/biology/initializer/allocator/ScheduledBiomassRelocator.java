package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;

/**
 * Redistributes the biomass around according to a "schedule" that maps a simulation step to a grid index.
 * The biomass grids are normalized upon construction, but care must be taken to have the all non-empty
 * grid locations match with {@code SeaTile} that have a BiomassLocal biology, otherwise biomass will be lost.
 * The biomass grids are stored in mutable {@code DoubleGrid2D} fields, but those should never be mutated, so
 * the class is safe to share between parallel simulations. Note that the {@code reallocate} method mutates
 * the tiles biomass arrays directly.
 */
public class ScheduledBiomassRelocator implements Steppable {

    private final Map<String, List<DoubleGrid2D>> biomassGrids;
    private final IntUnaryOperator schedule;

    /**
     * @param biomassGrids A map from species names to collection of biomass grids
     * @param schedule     A function from a simulation step to the desired index in the grids collection.
     */
    public ScheduledBiomassRelocator(
        Map<String, Collection<DoubleGrid2D>> biomassGrids,
        IntUnaryOperator schedule
    ) {
        this.biomassGrids = biomassGrids.entrySet().stream()
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> entry.getValue().stream().map(this::normalize).collect(toImmutableList())
            ));
        this.schedule = schedule;
    }

    private DoubleGrid2D normalize(DoubleGrid2D grid) {
        double sum = Arrays.stream(grid.field).flatMapToDouble(Arrays::stream).sum();
        return new DoubleGrid2D(grid).multiply(1 / sum);
    }

    @Override
    public void step(SimState simState) {
        FishState fishState = (FishState) simState;
        reallocate(fishState.getBiology(), fishState.getMap(), fishState.getStep());
    }

    public void reallocate(
        GlobalBiology globalBiology,
        NauticalMap nauticalMap,
        int step
    ) {
        ImmutableMap<Species, Double> speciesBiomasses =
            globalBiology.getSpecies().stream().collect(toImmutableMap(
                Function.identity(),
                nauticalMap::getTotalBiomass
            ));
        reallocate(speciesBiomasses, nauticalMap.getAllSeaTilesExcludingLandAsList(), step);
    }

    /**
     * Reallocates biomass but mutating the biomass array of sea tiles directly.
     * Only affects tiles with a {@code BiomassLocalBiology}.
     */
    public void reallocate(
        Map<Species, Double> totalBiomasses,
        Iterable<SeaTile> seaTiles,
        int step
    ) {

        List<DoubleGrid2D> biomassGrids = totalBiomasses.entrySet()
            .stream()
            .map(entry -> getGridCopy(entry.getKey(), step).multiply(entry.getValue()))
            .collect(toImmutableList());

        stream(seaTiles)
            .filter(seaTile -> seaTile.getBiology() instanceof BiomassLocalBiology)
            .forEach(seaTile -> {
                double[] biomass = ((BiomassLocalBiology) seaTile.getBiology()).getCurrentBiomass();
                for (int i = 0; i < biomass.length; i++)
                    biomass[i] = biomassGrids.get(i).get(seaTile.getGridX(), seaTile.getGridY());
            });
    }

    @NotNull
    private DoubleGrid2D getGridCopy(Species species, int step) {
        DoubleGrid2D grid = biomassGrids.get(species.getName()).get(schedule.applyAsInt(step));
        return new DoubleGrid2D(grid);
    }
}
