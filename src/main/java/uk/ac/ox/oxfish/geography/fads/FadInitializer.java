package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.function.Function;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class FadInitializer implements Function<FadManager, Fad> {

    private final double[] emptyBiomasses;
    private final double[] carryingCapacities;
    private final double[] attractionRates;
    private final double fishReleaseProbability;

    FadInitializer(
        GlobalBiology globalBiology,
        ImmutableMap<Species, Quantity<Mass>> carryingCapacities,
        ImmutableMap<Species, Double> attractionRates,
        double fishReleaseProbability
    ) {
        this.emptyBiomasses = new double[globalBiology.getSize()];
        this.carryingCapacities = new double[globalBiology.getSize()];
        carryingCapacities.forEach((species, qty) ->
            this.carryingCapacities[species.getIndex()] = asDouble(qty, KILOGRAM)
        );
        this.attractionRates = new double[globalBiology.getSize()];
        attractionRates.forEach((species, rate) ->
            this.attractionRates[species.getIndex()] = rate
        );
        this.fishReleaseProbability = fishReleaseProbability;
    }

    @Override public Fad apply(@NotNull FadManager fadManager) {
        final BiomassLocalBiology fadBiology = new BiomassLocalBiology(emptyBiomasses, carryingCapacities);
        return new Fad(fadManager, fadBiology, attractionRates, fishReleaseProbability);
    }
}
