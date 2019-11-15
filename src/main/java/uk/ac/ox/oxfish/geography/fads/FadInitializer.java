package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import tech.units.indriya.quantity.Quantities;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.utility.Measures;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.Arrays;
import java.util.function.Function;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class FadInitializer implements Function<FadManager, Fad> {

    private double[] emptyBiomasses;
    private double[] carryingCapacities;
    private final double attractionRate;
    private final double fishReleaseProbability;

    FadInitializer(
        GlobalBiology globalBiology,
        ImmutableMap<Species, Quantity<Mass>> carryingCapacities,
        double attractionRate,
        double fishReleaseProbability
    ) {
        this.emptyBiomasses = new double[globalBiology.getSize()];
        this.carryingCapacities = new double[globalBiology.getSize()];
        carryingCapacities.entrySet().forEach(entry ->
            this.carryingCapacities[entry.getKey().getIndex()] = asDouble(entry.getValue(), KILOGRAM)
        );
        this.attractionRate = attractionRate;
        this.fishReleaseProbability = fishReleaseProbability;
    }

    @Override public Fad apply(@NotNull FadManager fadManager) {
        final BiomassLocalBiology fadBiology = new BiomassLocalBiology(emptyBiomasses, carryingCapacities);
        return new Fad(fadManager, fadBiology, attractionRate, fishReleaseProbability);
    }
}
