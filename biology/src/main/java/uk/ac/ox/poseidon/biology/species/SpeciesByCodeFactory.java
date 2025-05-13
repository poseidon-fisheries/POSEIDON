package uk.ac.ox.poseidon.biology.species;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SpeciesByCodeFactory extends GlobalScopeFactory<List<Species>> {

    private Factory<? extends List<String>> speciesCodes;
    private Factory<? extends List<Species>> speciesList;

    @Override
    protected List<Species> newInstance(final Simulation simulation) {
        final ImmutableSet<String> speciesCodes =
            ImmutableSet.copyOf(this.speciesCodes.get(simulation));
        return speciesList
            .get(simulation)
            .stream()
            .filter(s -> speciesCodes.contains(s.getCode()))
            .toList();
    }
}
