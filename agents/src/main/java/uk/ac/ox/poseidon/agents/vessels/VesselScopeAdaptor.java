package uk.ac.ox.poseidon.agents.vessels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VesselScopeAdaptor<C> extends VesselScopeFactory<C> {

    private Factory<? extends C> delegate;

    @Override
    protected C newInstance(
        final Simulation simulation,
        final Vessel vessel
    ) {
        return delegate.get(simulation);
    }
    
}
