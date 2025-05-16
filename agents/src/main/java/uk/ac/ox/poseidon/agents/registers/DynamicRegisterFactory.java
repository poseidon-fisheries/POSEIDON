package uk.ac.ox.poseidon.agents.registers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DynamicRegisterFactory<T> extends SimulationScopeFactory<Register<T>> {

    private VesselScopeFactory<? extends T> vesselScopeFactory;

    @Override
    protected Register<T> newInstance(final Simulation simulation) {
        return new DynamicRegister<>(
            vessel -> vesselScopeFactory.get(simulation, vessel)
        );
    }
}
