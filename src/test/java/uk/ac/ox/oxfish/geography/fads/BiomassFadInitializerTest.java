package uk.ac.ox.oxfish.geography.fads;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.function.DoubleSupplier;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;

public class BiomassFadInitializerTest {

    @Test
    public void fadBiomassInitializedToZero() {
        final Species speciesA = new Species("A");
        final Species speciesB = new Species("B");
        final GlobalBiology globalBiology = new GlobalBiology(speciesA, speciesB);
        final ImmutableMap<Species, DoubleSupplier> carryingCapacities = ImmutableMap.of(
            speciesA, () -> 1000.0,
            speciesB, () -> 2000.0
        );
        final BiomassFadInitializer fadInitializer = new BiomassFadInitializer(
            globalBiology,
            carryingCapacities,
            ImmutableMap.of(),
            0,
            () -> 0
        );
        final FadMap<BiomassLocalBiology, BiomassFad> fadMap =
            new FadMap<>(
                mock(NauticalMap.class),
                mock(CurrentVectors.class),
                globalBiology,
                BiomassLocalBiology.class,
                BiomassFad.class
            );
        final FadManager<BiomassLocalBiology, BiomassFad> fadManager =
            new FadManager<>(fadMap, fadInitializer);
        final SeaTile seaTile = mock(SeaTile.class);
        when(seaTile.getGridX()).thenReturn(0);
        when(seaTile.getGridY()).thenReturn(0);
        final Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(seaTile);
        fadManager.setFisher(fisher);

        final BiomassFad fad = fadInitializer.apply(fadManager);
        for (final Species species : globalBiology.getSpecies()) {
            assertEquals(fad.getBiology().getBiomass(species), 0, 0);
        }
    }

}
