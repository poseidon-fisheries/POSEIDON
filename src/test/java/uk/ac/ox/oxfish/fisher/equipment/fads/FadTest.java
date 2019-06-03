package uk.ac.ox.oxfish.fisher.equipment.fads;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class FadTest {

    private final GlobalBiology globalBiology = new GlobalBiology(new Species("A"), new Species("B"));

    @Test
    public void releaseFish() {

        // Make a full FAD, with a carrying capacity of 0.75...
        final VariableBiomassBasedBiology fadBiology = makeBiology(0.75);
        fillBiology(fadBiology);
        final Fad fad = new Fad(mock(FadManager.class), fadBiology, 0);

        // ...and an empty tile biology, with a carrying capacity of 1.0:
        VariableBiomassBasedBiology tileBiology = makeBiology(1.0);

        // release the FAD's fish into the tile biology
        fad.releaseFish(tileBiology, globalBiology);

        // Check that the FAD is now empty and the tile has received the fish
        for (Species species : globalBiology.getSpecies()) {
            assertEquals(fadBiology.getBiomass(species), 0d, 0d);
            assertEquals(tileBiology.getBiomass(species), fadBiology.getCarryingCapacity(species), 0d);
        }

        // Refill the FAD and release another batch of FAD fish into the tile biology
        fillBiology(fadBiology);
        fad.releaseFish(tileBiology, globalBiology);

        // Check that the FAD is now empty and the tile is now at full carrying capacity
        for (Species species : globalBiology.getSpecies()) {
            assertEquals(fadBiology.getBiomass(species), 0d, 0d);
            assertEquals(tileBiology.getBiomass(species), tileBiology.getCarryingCapacity(species), 0d);
        }

    }

    /**
     * Make a new biology with the given carrying capacity and zero biomass
     */
    private VariableBiomassBasedBiology makeBiology(double carryingCapacityValue) {
        double[] biomass = new double[globalBiology.getSize()];
        Arrays.fill(biomass, 0.0);
        double[] carryingCapacity = new double[globalBiology.getSize()];
        Arrays.fill(carryingCapacity, carryingCapacityValue);
        return new BiomassLocalBiology(biomass, carryingCapacity);
    }

    private void fillBiology(VariableBiomassBasedBiology biology) {
        for (Species species : globalBiology.getSpecies())
            biology.setCurrentBiomass(species, biology.getCarryingCapacity(species));
    }

}