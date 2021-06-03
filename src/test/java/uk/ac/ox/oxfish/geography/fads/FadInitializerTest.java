package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;

import java.util.function.DoubleSupplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FadInitializerTest {

    @Test
    public void fadBiomassInitializedToZero() {
        final Species speciesA = new Species("A");
        final Species speciesB = new Species("B");
        final GlobalBiology globalBiology = new GlobalBiology(speciesA, speciesB);
        final ImmutableMap<Species, DoubleSupplier> carryingCapacities = ImmutableMap.of(
            speciesA, () -> 1000.0,
            speciesB, () -> 2000.0
        );
        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final FadInitializer fadInitializer = new FadInitializer(
            globalBiology,
            carryingCapacities,
            ImmutableMap.of(),
            rng,
            0,
            0,
            () -> 0
        );
        final FadMap fadMap =
            new FadMap(mock(NauticalMap.class), mock(CurrentVectors.class), globalBiology);
        final FadManager fadManager = new FadManager(fadMap, fadInitializer, 0);
        final SeaTile seaTile = mock(SeaTile.class);
        when(seaTile.getGridX()).thenReturn(0);
        when(seaTile.getGridY()).thenReturn(0);
        final Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(seaTile);
        fadManager.setFisher(fisher);

        final Fad fad = fadInitializer.apply(fadManager);
        for (final Species species : globalBiology.getSpecies())
            assertEquals(fad.getBiology().getBiomass(species), 0, 0);
    }

}
