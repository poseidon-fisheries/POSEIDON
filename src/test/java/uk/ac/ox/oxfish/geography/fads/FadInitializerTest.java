package uk.ac.ox.oxfish.geography.fads;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;

public class FadInitializerTest {

    @Test
    public void fadBiomassInitializedToZero() {
        final GlobalBiology globalBiology =
            new GlobalBiology(new Species("A"), new Species("B"));
        final FadInitializer fadInitializer =
            new FadInitializer(getQuantity(1d, TONNE), 0d, 0);
        final FadMap fadMap =
            new FadMap(mock(NauticalMap.class), mock(CurrentVectors.class), globalBiology);
        final FadManager fadManager = new FadManager(fadMap, fadInitializer, 0);

        final Fad fad = fadInitializer.apply(fadManager);
        for (Species species : globalBiology.getSpecies())
            assertEquals(fad.getBiology().getBiomass(species), 0, 0);
    }
}