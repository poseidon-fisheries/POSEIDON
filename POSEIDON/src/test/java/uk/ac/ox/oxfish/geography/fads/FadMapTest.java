package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import sim.engine.Schedule;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.*;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.ReliableFishValueCalculator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities.fillBiomassFad;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities.makeBiology;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class FadMapTest {

    @Test
    public void fadBeaching() {

        // Make a 3x3 map, with a one tile island in the middle
        final NauticalMap nauticalMap = makeMap(new int[][]{
            {-1, -1, -1},
            {-1, 10, -1},
            {-1, -1, -1}
        });

        final Species speciesA = new Species("A");
        final Species speciesB = new Species("B");
        final GlobalBiology globalBiology = new GlobalBiology(speciesA, speciesB);

        for (final SeaTile tile : nauticalMap.getAllSeaTilesAsList()) {
            tile.setBiology(
                tile.isWater()
                    ? makeBiology(globalBiology, 1.0)
                    : new EmptyLocalBiology()
            );
        }

        // Make a current map that moves FADs west
        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final CurrentVectors currentVectors =
            TestUtilities.makeUniformCurrentVectors(nauticalMap, new Double2D(-0.3, 0), 1);
        final BiomassFadInitializer fadInitializer = new BiomassFadInitializer(
            globalBiology,
            new DummyFishBiomassAttractor(globalBiology.getSize()),
            0,
            () -> 0,
            new GlobalCarryingCapacityInitializer(new FixedDoubleParameter(2.0))
        );
        final FadMap fadMap = new FadMap(
            nauticalMap,
            currentVectors,
            globalBiology,
            BiomassLocalBiology.class
        );

        final Schedule schedule = mock(Schedule.class);
        final FishState fishState = mock(FishState.class);
        when(fishState.getRandom()).thenReturn(rng);
        when(fishState.getBiology()).thenReturn(globalBiology);
        fishState.schedule = schedule;

        final FadManager fadManager =
            new FadManager(fadMap, fadInitializer, new ReliableFishValueCalculator(globalBiology));
        final Fisher fisher = mock(Fisher.class, RETURNS_MOCKS);
        when(fisher.grabRandomizer()).thenReturn(rng);
        when(fisher.grabState()).thenReturn(fishState);
        fadManager.setFisher(fisher);
        fadManager.setNumFadsInStock(1);

        // Put a FAD at the East edge of the central row
        final SeaTile startTile = nauticalMap.getSeaTile(2, 1);
        final BiomassAggregatingFad fad = (BiomassAggregatingFad) fadManager.deployFadInCenterOfTile(startTile, rng);
        fillBiomassFad(fad);
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        final VariableBiomassBasedBiology startTileBiology =
            (VariableBiomassBasedBiology) startTile.getBiology();
        assertEquals(Arrays.stream(fad.getBiomass()).sum(), fad.getCarryingCapacity().getTotal(), 0.0);
        assertTrue(startTileBiology.isEmpty());

        // If we step once, the FAD should still be in its starting tile
        // and the biologies should not have changed
        when(fishState.getStep()).thenReturn(1);
        fadMap.step(fishState);
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        assertEquals(Arrays.stream(fad.getBiomass()).sum(), fad.getCarryingCapacity().getTotal(), 0.0);
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
