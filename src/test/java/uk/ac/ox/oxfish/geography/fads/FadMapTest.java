package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.engine.Schedule;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.assertEmptyBiology;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.assertFullBiology;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.fillBiology;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.makeBiology;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.NEUTRAL;

public class FadMapTest {

    @Test
    public void fadBeaching() {

        // Make a 3x3 map, with a one tile island in the middle
        NauticalMap nauticalMap = makeMap(new int[][]{
            {-1, -1, -1},
            {-1, 10, -1},
            {-1, -1, -1}
        });

        final Species speciesA = new Species("A");
        final Species speciesB = new Species("B");
        final GlobalBiology globalBiology = new GlobalBiology(speciesA, speciesB);
        final Quantity<Mass> k = getQuantity(1, TONNE);
        final ImmutableMap<Species, Quantity<Mass>> fadCarryingCapacities = ImmutableMap.of(speciesA, k, speciesB, k);

        // Make a current map that moves FADs west
        final Double2D currentVector = new Double2D(-0.3, 0);
        final Map<SeaTile, Double2D> vectors = nauticalMap
            .getAllSeaTilesExcludingLandAsList().stream()
            .collect(toMap(identity(), __ -> currentVector));
        for (SeaTile tile : nauticalMap.getAllSeaTilesAsList()) {
            tile.setBiology(tile.isWater() ? makeBiology(globalBiology, k) : new EmptyLocalBiology());
        }

        final TreeMap<Integer, EnumMap<CurrentPattern, Map<SeaTile, Double2D>>> vectorMaps = new TreeMap<>();
        vectorMaps.put(1, new EnumMap<>(ImmutableMap.of(NEUTRAL, vectors)));
        final CurrentVectors currentVectors = new CurrentVectors(vectorMaps, __ -> NEUTRAL, 1);
        final FadInitializer fadInitializer = new FadInitializer(globalBiology, fadCarryingCapacities, ImmutableMap.of(), 0);
        final FadMap fadMap = new FadMap(nauticalMap, currentVectors, globalBiology);

        final Schedule schedule = mock(Schedule.class);
        final FishState fishState = mock(FishState.class);
        fishState.random = new MersenneTwisterFast();
        fishState.schedule = schedule;

        final FadManager fadManager = new FadManager(fadMap, fadInitializer, 1, 0, ImmutableSet.of());
        final Fisher fisher = mock(Fisher.class);
        when(fisher.grabRandomizer()).thenReturn(fishState.random);
        fadManager.setFisher(fisher);

        // Put a FAD at the East edge of the central row
        final SeaTile startTile = nauticalMap.getSeaTile(2, 1);
        final Fad fad = fadManager.deployFad(startTile, 0);
        fillBiology(fad.getBiology());
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        assertFullBiology(fad.getBiology());
        assertEmptyBiology((VariableBiomassBasedBiology) startTile.getBiology());

        // If we step once, the FAD should still be in its starting tile
        // and the biologies should not have changed
        when(fishState.getStep()).thenReturn(1);
        fadMap.step(fishState);
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        assertFullBiology(fad.getBiology());
        assertEmptyBiology((VariableBiomassBasedBiology) startTile.getBiology());

        // Let it drift to the island
        when(fishState.getStep()).thenReturn(2);
        fadMap.step(fishState);

        // The FAD should have been removed from the map
        assertEquals(Optional.empty(), fadMap.getFadTile(fad));
        // And the fish should be released in the starting cell
        assertEmptyBiology(fad.getBiology());
        assertFullBiology((VariableBiomassBasedBiology) startTile.getBiology());
    }

}
