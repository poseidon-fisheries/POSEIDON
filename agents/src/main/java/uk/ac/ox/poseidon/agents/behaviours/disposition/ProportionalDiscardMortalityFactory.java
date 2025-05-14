package uk.ac.ox.poseidon.agents.behaviours.disposition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.function.DoubleSupplier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProportionalDiscardMortalityFactory
    extends GlobalScopeFactory<ProportionalDiscardMortality> {

    private Factory<? extends DoubleSupplier> mortalityRateSupplier;

    @Override
    protected ProportionalDiscardMortality newInstance(final Simulation simulation) {
        return new ProportionalDiscardMortality(mortalityRateSupplier.get(simulation));
    }
}
