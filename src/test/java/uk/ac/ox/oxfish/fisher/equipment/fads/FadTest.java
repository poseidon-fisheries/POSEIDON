package uk.ac.ox.oxfish.fisher.equipment.fads;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.equipment.fads.TestUtilities.*;

public class FadTest {

    private final GlobalBiology globalBiology = new GlobalBiology(new Species("A"), new Species("B"));

    @Test
    public void releaseFish() {

        // Make a full FAD, with a carrying capacity of 0.75...
        final BiomassLocalBiology fadBiology = makeBiology(globalBiology, 0.75);
        fillBiology(fadBiology);
        final FadManager fadManager = mock(FadManager.class, RETURNS_DEEP_STUBS);
        final Fad fad = new Fad(fadManager, fadBiology, new double[globalBiology.getSize()], 0);

        // ...and an empty tile biology, with a carrying capacity of 1.0:
        VariableBiomassBasedBiology tileBiology = makeBiology(globalBiology, 1.0);

        // release the FAD's fish into the tile biology
        fad.releaseFish(globalBiology.getSpecies(), tileBiology);

        // Check that the FAD is now empty and the tile has received the fish
        assertEmptyBiology(fadBiology);
        for (Species species : globalBiology.getSpecies())
            assertEquals(tileBiology.getBiomass(species), fadBiology.getCarryingCapacity(species), 0d);

        // Refill the FAD and release another batch of FAD fish into the tile biology
        fillBiology(fadBiology);
        fad.releaseFish(globalBiology.getSpecies(), tileBiology);

        // Check that the FAD is now empty and the tile is now at full carrying capacity
        assertEmptyBiology(fadBiology);
        assertFullBiology(tileBiology);
    }
}