package uk.ac.ox.poseidon.agents.behaviours.disposition;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Getter
public class SelectedSpeciesRetention<C extends Content<C>> implements DispositionProcess<C> {

    private final Set<Species> selectedSpecies;

    SelectedSpeciesRetention(final Collection<Species> selectedSpecies) {
        this.selectedSpecies = ImmutableSet.copyOf(selectedSpecies);
    }

    @Override
    public Disposition<C> partition(
        final Disposition<C> currentDisposition,
        final double availableCapacityInKg
    ) {
        final Map<Boolean, Bucket<C>> partition =
            currentDisposition.getRetained().partitionBy((species, content) ->
                selectedSpecies.contains(species)
            );
        return new Disposition<>(
            currentDisposition.getRetained().add(partition.get(true)),
            currentDisposition.getDiscardedAlive().add(partition.get(false)),
            currentDisposition.getDiscardedDead()
        );
    }
}
