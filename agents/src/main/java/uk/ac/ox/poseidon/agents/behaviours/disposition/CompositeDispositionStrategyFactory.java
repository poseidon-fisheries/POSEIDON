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
public class CompositeDispositionStrategyFactory<C extends Content<C>>
    extends GlobalScopeFactory<CompositeDispositionStrategy<C>> {

    private List<Factory<? extends DispositionStrategy<? super C>>> dispositionStrategies;

    @SafeVarargs
    @SuppressWarnings("varargs")
    public CompositeDispositionStrategyFactory(
        final Factory<? extends DispositionStrategy<? super C>>... dispositionStrategies
    ) {
        this(List.of(dispositionStrategies));
    }

    @Override
    protected CompositeDispositionStrategy<C> newInstance(final Simulation simulation) {
        return new CompositeDispositionStrategy<>(
            dispositionStrategies
                .stream()
                .map(factory -> factory.get(simulation))
                .collect(toImmutableList())
        );
    }
}
