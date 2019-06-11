package uk.ac.ox.oxfish.fisher.equipment.fads;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.utility.Measures;

import javax.measure.quantity.Mass;
import java.util.Arrays;

import static org.apache.sis.measure.Units.KILOGRAM;
import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

/**
 * Just a bunch of statics to make testing stuff around FADs easier
 */
public class TestUtilities {

    /**
     * Make a new biology with the given carrying capacity and zero biomass
     */
    public static BiomassLocalBiology makeBiology(GlobalBiology globalBiology, Mass carryingCapacity) {
        return makeBiology(globalBiology, asDouble(carryingCapacity, KILOGRAM));
    }

    /**
     * Make a new biology with the given carrying capacity and zero biomass
     */
    public static BiomassLocalBiology makeBiology(GlobalBiology globalBiology, double carryingCapacityValue) {
        double[] biomass = new double[globalBiology.getSize()];
        Arrays.fill(biomass, 0.0);
        double[] carryingCapacity = new double[globalBiology.getSize()];
        Arrays.fill(carryingCapacity, carryingCapacityValue);
        return new BiomassLocalBiology(biomass, carryingCapacity);
    }

    public static void fillBiology(VariableBiomassBasedBiology biology) {
        final double[] biomassArray = biology.getCurrentBiomass();
        for (int i = 0; i < biomassArray.length; i++)
            biomassArray[i] = biology.getCarryingCapacity(i);
    }

    public static void assertEmptyBiology(VariableBiomassBasedBiology biology) {
        for (double biomass : biology.getCurrentBiomass())
            assertEquals(0.0, biomass, 0.0);
    }

    public static void assertFullBiology(VariableBiomassBasedBiology biology) {
        final double[] biomassArray = biology.getCurrentBiomass();
        for (int i = 0; i < biomassArray.length; i++)
            assertEquals(biology.getCarryingCapacity(i), biomassArray[i], 0.0);
    }

}
