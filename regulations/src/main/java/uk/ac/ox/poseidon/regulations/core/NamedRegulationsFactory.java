package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class NamedRegulationsFactory implements ComponentFactory<Regulations> {
    private Map<String, ComponentFactory<Regulations>> regulations;

    public NamedRegulationsFactory(final Map<String, ComponentFactory<Regulations>> regulations) {
        this.regulations = regulations;
    }

    @SuppressWarnings("unused")
    public NamedRegulationsFactory() {
    }

    @Override
    public Regulations apply(final ModelState modelState) {
        return new ConjunctiveRegulations(
            regulations.values().stream()
                .map(regulation -> regulation.apply(modelState))
                .collect(toImmutableSet())
        );
    }

    public void modify(
        final String regulationName,
        final Supplier<? extends ComponentFactory<Regulations>> supplier
    ) {
        setRegulations(
            ImmutableMap.<String, ComponentFactory<Regulations>>builder()
                .putAll(getRegulations())
                .put(regulationName, supplier.get())
                .buildKeepingLast()
        );
    }

    public Map<String, ComponentFactory<Regulations>> getRegulations() {
        return regulations;
    }

    public void setRegulations(final Map<String, ComponentFactory<Regulations>> regulations) {
        this.regulations = regulations;
    }
}
