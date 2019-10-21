package uk.ac.ox.oxfish.geography.fads;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.utility.Measures;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.Arrays;
import java.util.function.Function;

import static tech.units.indriya.unit.Units.KILOGRAM;

public class FadInitializer implements Function<FadManager, Fad> {

    private final Quantity<Mass> carryingCapacity;
    private final double attractionRate;

    FadInitializer(Quantity<Mass> carryingCapacity, double attractionRate) {
        this.carryingCapacity = carryingCapacity;
        this.attractionRate = attractionRate;
    }

    @Override public Fad apply(@NotNull FadManager fadManager) {
        final int numSpecies = fadManager.getFadMap().getGlobalBiology().getSize();
        double[] currentBiomasses = new double[numSpecies];
        double[] carryingCapacities = new double[numSpecies];
        Arrays.fill(carryingCapacities, Measures.asDouble(carryingCapacity, KILOGRAM));
        final BiomassLocalBiology fadBiology = new BiomassLocalBiology(currentBiomasses, carryingCapacities);
        return new Fad(fadManager, fadBiology, attractionRate);
    }
}
