package uk.ac.ox.oxfish.geography.fads;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.DummyFishBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.ReliableFishValueCalculator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectorsEPO;
import uk.ac.ox.oxfish.model.FishState;

public class BiomassFadInitializerTest {

    @Test
    public void fadBiomassInitializedToZero() {
        final Species speciesA = new Species("A");
        final Species speciesB = new Species("B");
        final GlobalBiology globalBiology = new GlobalBiology(speciesA, speciesB);
        final BiomassFadInitializer fadInitializer = new BiomassFadInitializer(
            globalBiology,
            Double.POSITIVE_INFINITY,
            new DummyFishBiomassAttractor(globalBiology.getSize()),
            0,
            () -> 0
        );
        final FadMap<BiomassLocalBiology, BiomassFad> fadMap =
            new FadMap<>(
                mock(NauticalMap.class),
                mock(CurrentVectorsEPO.class),
                globalBiology,
                BiomassLocalBiology.class,
                BiomassFad.class
            );
        final FadManager<BiomassLocalBiology, BiomassFad> fadManager =
            new FadManager<>(fadMap, fadInitializer, new ReliableFishValueCalculator(globalBiology));
        final SeaTile seaTile = mock(SeaTile.class);
        when(seaTile.getGridX()).thenReturn(0);
        when(seaTile.getGridY()).thenReturn(0);
        final FishState fishState = mock(FishState.class);
        when(fishState.getBiology()).thenReturn(globalBiology);
        final Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(seaTile);
        when(fisher.grabState()).thenReturn(fishState);
        fadManager.setFisher(fisher);

        final BiomassFad fad = fadInitializer.makeFad(fadManager,null ,seaTile);
        for (final Species species : globalBiology.getSpecies()) {
            assertEquals(fad.getBiology().getBiomass(species), 0, 0);
        }
    }

}