package uk.ac.ox.poseidon.agents.behaviours.disposition;

import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

public class ProportionallyLimitBiomassToHold
    implements DispositionStrategy<Biomass> {

    @Override
    public Disposition<Biomass> partition(
        final Disposition<Biomass> currentDisposition,
        final double availableCapacityInKg
    ) {
        final double currentlyRetainedInKg =
            currentDisposition.getRetained().getTotalBiomass().asKg();
        if (currentlyRetainedInKg <= availableCapacityInKg) {
            return currentDisposition;
        } else {
            final double proportionToKeep = availableCapacityInKg / currentlyRetainedInKg;
            final Bucket<Biomass> updatedRetained =
                currentDisposition.getRetained().mapContent(biomass ->
                    biomass.multiply(proportionToKeep)
                );
            final Bucket<Biomass> newlyDiscarded =
                currentDisposition.getRetained().subtract(updatedRetained);
            return new Disposition<>(
                updatedRetained,
                currentDisposition.getDiscardedAlive().add(newlyDiscarded),
                currentDisposition.getDiscardedDead()
            );
        }
    }

}
