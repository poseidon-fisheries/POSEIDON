package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.Species;

import java.util.Arrays;
import java.util.Map;

public class PerSpeciesCarryingCapacity implements CarryingCapacity {
    private final double[] carryingCapacities;

    public PerSpeciesCarryingCapacity(final double[] carryingCapacities) {
        this.carryingCapacities = carryingCapacities.clone();
    }

    @SuppressWarnings("unused")
    public PerSpeciesCarryingCapacity(final Map<Species, Double> carryingCapacities) {
        this.carryingCapacities = makeArray(carryingCapacities);
    }

    private static double[] makeArray(final Map<? extends Species, Double> carryingCapacities) {
        final int size = carryingCapacities.keySet().stream()
            .map(Species::getIndex)
            .mapToInt(i -> i + 1)
            .max()
            .orElse(0);
        final double[] a = new double[size];
        carryingCapacities.forEach((species, value) ->
            a[species.getIndex()] = value
        );
        return a;
    }

    public double[] getCarryingCapacities() {
        return carryingCapacities;
    }

    @Override
    public double getTotal() {
        return Arrays.stream(carryingCapacities).sum();
    }

    public double get(final Species species) {
        return carryingCapacities[species.getIndex()];
    }
}
