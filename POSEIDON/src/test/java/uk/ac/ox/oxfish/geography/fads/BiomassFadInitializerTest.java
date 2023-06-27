package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.DummyFishBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.GlobalCarryingCapacityInitializer;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.ReliableFishValueCalculator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentVectorsEPO;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.regulations.core.EverythingPermitted;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BiomassFadInitializerTest {

    @Test
    public void fadBiomassInitializedToZero() {
        final Species speciesA = new Species("A");
        final Species speciesB = new Species("B");
        final GlobalBiology globalBiology = new GlobalBiology(speciesA, speciesB);
        final BiomassFadInitializer fadInitializer = new BiomassFadInitializer(
            globalBiology,
            new DummyFishBiomassAttractor(globalBiology.getSize()),
            0,
            () -> 0,
            new GlobalCarryingCapacityInitializer(new FixedDoubleParameter(Double.POSITIVE_INFINITY))
        );
        final FadMap fadMap =
            new FadMap(
                mock(NauticalMap.class),
                mock(CurrentVectorsEPO.class),
                globalBiology,
                BiomassLocalBiology.class
            );
        final FadManager fadManager =
            new FadManager(
                new EverythingPermitted<>(),
                fadMap,
                fadInitializer,
                null,
                new ReliableFishValueCalculator(globalBiology)
            );
        final SeaTile seaTile = mock(SeaTile.class);
        when(seaTile.getGridX()).thenReturn(0);
        when(seaTile.getGridY()).thenReturn(0);
        final FishState fishState = mock(FishState.class);
        when(fishState.getBiology()).thenReturn(globalBiology);
        final Fisher fisher = mock(Fisher.class);
        when(fisher.getLocation()).thenReturn(seaTile);
        when(fisher.grabState()).thenReturn(fishState);
        fadManager.setFisher(fisher);

        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final BiomassAggregatingFad fad = fadInitializer.makeFad(fadManager, null, seaTile, rng);
        for (final Species species : globalBiology.getSpecies()) {
            assertEquals(fad.getBiology().getBiomass(species), 0, 0);
        }
    }

}
