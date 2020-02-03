package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;

public class FadInitializerTest {

    @Test
    public void fadBiomassInitializedToZero() {
        final Species speciesA = new Species("A");
        final Species speciesB = new Species("B");
        final GlobalBiology globalBiology = new GlobalBiology(speciesA, speciesB);
        final ImmutableMap<Species, Quantity<Mass>> carryingCapacities = ImmutableMap.of(
            speciesA, getQuantity(1d, TONNE),
            speciesB, getQuantity(2d, TONNE)
        );
        final FadInitializer fadInitializer =
            new FadInitializer(globalBiology, carryingCapacities, ImmutableMap.of(), 0);
        final FadMap fadMap =
            new FadMap(mock(NauticalMap.class), mock(CurrentVectors.class), globalBiology);
        final FadManager fadManager = new FadManager(fadMap, fadInitializer, 0, 0, ImmutableSet.of());

        final Fad fad = fadInitializer.apply(fadManager);
        for (Species species : globalBiology.getSpecies())
            assertEquals(fad.getBiology().getBiomass(species), 0, 0);
    }
}
