package uk.ac.ox.oxfish.geography.fads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities.fillBiomassFad;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities.makeBiology;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

import ec.util.MersenneTwisterFast;
import java.util.Optional;
import org.junit.Test;
import sim.engine.Schedule;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.DummyFishBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.TestUtilities;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;

public class FadMapTest {

    @Test
    public void fadBeaching() {

        // Make a 3x3 map, with a one tile island in the middle
        final NauticalMap nauticalMap = makeMap(new int[][] {
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
            2.0,
            new DummyFishBiomassAttractor(globalBiology.getSize()),
            0,
            () -> 0
        );
        final FadMap<BiomassLocalBiology, BiomassFad> fadMap = new FadMap<>(
            nauticalMap,
            currentVectors,
            globalBiology,
            BiomassLocalBiology.class,
            BiomassFad.class
        );

        final Schedule schedule = mock(Schedule.class);
        final FishState fishState = mock(FishState.class);
        when(fishState.getRandom()).thenReturn(rng);
        when(fishState.getBiology()).thenReturn(globalBiology);
        fishState.schedule = schedule;

        final FadManager<BiomassLocalBiology, BiomassFad> fadManager =
            new FadManager<>(fadMap, fadInitializer);
        final Fisher fisher = mock(Fisher.class, RETURNS_MOCKS);
        when(fisher.grabRandomizer()).thenReturn(rng);
        when(fisher.grabState()).thenReturn(fishState);
        fadManager.setFisher(fisher);
        fadManager.setNumFadsInStock(1);

        // Put a FAD at the East edge of the central row
        final SeaTile startTile = nauticalMap.getSeaTile(2, 1);
        final BiomassFad fad = fadManager.deployFad(startTile);
        fillBiomassFad(fad);
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        final VariableBiomassBasedBiology startTileBiology =
            (VariableBiomassBasedBiology) startTile.getBiology();
        assertTrue(fad.isFull());
        assertTrue(startTileBiology.isEmpty());

        // If we step once, the FAD should still be in its starting tile
        // and the biologies should not have changed
        when(fishState.getStep()).thenReturn(1);
        fadMap.step(fishState);
        assertEquals(Optional.of(startTile), fadMap.getFadTile(fad));
        assertTrue(fad.isFull());
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
