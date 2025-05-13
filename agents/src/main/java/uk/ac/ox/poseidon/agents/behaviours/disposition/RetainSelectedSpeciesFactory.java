package uk.ac.ox.poseidon.agents.behaviours.disposition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetainSelectedSpeciesFactory<C extends Content<C>>
    extends GlobalScopeFactory<RetainSelectedSpecies<C>> {

    private Factory<? extends Collection<Species>> selectedSpecies;

    @Override
    protected RetainSelectedSpecies<C> newInstance(final Simulation simulation) {
        return new RetainSelectedSpecies<>(selectedSpecies.get(simulation));
    }

}
