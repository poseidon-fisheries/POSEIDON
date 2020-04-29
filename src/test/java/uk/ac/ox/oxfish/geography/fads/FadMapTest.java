package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
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
import uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.fillBiology;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.makeBiology;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

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

        for (SeaTile tile : nauticalMap.getAllSeaTilesAsList()) {
            tile.setBiology(tile.isWater() ? makeBiology(globalBiology, k) : new EmptyLocalBiology());
        }

        // Make a current map that moves FADs west
        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final CurrentVectors currentVectors =
            TestUtilities.makeUniformCurrentVectors(nauticalMap, new Double2D(-0.3, 0), 1);
        final FadInitializer fadInitializer = new FadInitializer(
            globalBiology,
            fadCarryingCapacities,
            ImmutableMap.of(),
            rng,
            0,
            0,
            () -> 0
        );
        final FadMap fadMap = new FadMap(nauticalMap, currentVectors, globalBiology);

        final Schedule schedule = mock(Schedule.class);
        final FishState fishState = mock(FishState.class);
        when(fishState.getRandom()).thenReturn(rng);
        fishState.schedule = schedule;

        final FadManager fadManager = new FadManager(fadMap, fadInitializer, 1);
        final Fisher fisher = mock(Fisher.class, RETURNS_MOCKS);
        when(fisher.grabRandomizer()).thenReturn(rng);
        fadManager.setFisher(fisher);

        // Put a FAD at the East edge of the central row
        final SeaTile startTile = nauticalMap.getSeaTile(2, 1);
        final Fad fad = fadManager.deployFad(startTile, 0);
        fillBiology(fad.getBiology());
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        final VariableBiomassBasedBiology startTileBiology = (VariableBiomassBasedBiology) startTile.getBiology();
        assertTrue(fad.getBiology().isFull());
        assertTrue(startTileBiology.isEmpty());

        // If we step once, the FAD should still be in its starting tile
        // and the biologies should not have changed
        when(fishState.getStep()).thenReturn(1);
        fadMap.step(fishState);
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        assertTrue(fad.getBiology().isFull());
        assertTrue(startTileBiology.isEmpty());

        // Let it drift to the island
        when(fishState.getStep()).thenReturn(2);
        fadMap.step(fishState);

        // The FAD should have been removed from the map
        assertEquals(Optional.empty(), fadMap.getFadTile(fad));
        // And the fish should be released in the starting cell
        assertTrue(fad.getBiology().isEmpty());
        assertTrue(startTileBiology.isFull());
    }

}
