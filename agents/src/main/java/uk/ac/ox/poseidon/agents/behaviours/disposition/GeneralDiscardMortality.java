package uk.ac.ox.poseidon.agents.behaviours.disposition;

import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;

import java.util.function.DoubleSupplier;

import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

@RequiredArgsConstructor(access = PACKAGE)
public class GeneralDiscardMortality implements DispositionProcess<Biomass> {

    private final DoubleSupplier mortalityRateSupplier;

    @Override
    public Disposition<Biomass> partition(
        final Disposition<Biomass> currentDisposition,
        final double availableCapacityInKg
    ) {
        final Bucket<Biomass> newlyDead =
            currentDisposition
                .getDiscardedAlive()
                .mapContent(biomass ->
                    biomass.multiply(
                        checkUnitRange(
                            mortalityRateSupplier.getAsDouble(),
                            "Mortality"
                        )
                    )
                );
        return new Disposition<>(
            currentDisposition.getRetained(),
            currentDisposition.getDiscardedAlive().subtract(newlyDead),
            currentDisposition.getDiscardedDead().add(newlyDead)
        );
    }
}
