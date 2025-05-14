package uk.ac.ox.poseidon.agents.behaviours.disposition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompositeDispositionProcessFactory<C extends Content<C>>
    extends GlobalScopeFactory<CompositeDispositionProcess<C>> {

    private List<Factory<? extends DispositionProcess<? super C>>> dispositionStrategies;

    @SafeVarargs
    @SuppressWarnings("varargs")
    public CompositeDispositionProcessFactory(
        final Factory<? extends DispositionProcess<? super C>>... dispositionStrategies
    ) {
        this(List.of(dispositionStrategies));
    }

    @Override
    protected CompositeDispositionProcess<C> newInstance(final Simulation simulation) {
        return new CompositeDispositionProcess<>(
            dispositionStrategies
                .stream()
                .map(factory -> factory.get(simulation))
                .collect(toImmutableList())
        );
    }
}
