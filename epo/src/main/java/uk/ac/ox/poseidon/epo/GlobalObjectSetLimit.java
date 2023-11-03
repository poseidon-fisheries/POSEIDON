package uk.ac.ox.poseidon.epo;

import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.NamedRegulations;
import uk.ac.ox.oxfish.regulations.conditions.*;
import uk.ac.ox.oxfish.regulations.quantities.YearlyGatherer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class GlobalObjectSetLimit implements PolicySupplier {

    private final List<Integer> years;
    private final List<Integer> limits;

    public GlobalObjectSetLimit(
        final List<Integer> years,
        final List<Integer> limits
    ) {
        this.years = years;
        this.limits = limits;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return limits
            .stream()
            .map(limit ->
                new Policy<EpoScenario<?>>(
                    "Global limit of %d object sets",
                    scenario ->
                        ((NamedRegulations) scenario.getRegulations())
                            .modify(
                                "Global object-set limits",
                                ignored -> new ForbiddenIf(
                                    new AllOf(
                                        new AnyOf(
                                            years.stream().map(InYear::new).toArray(AlgorithmFactory[]::new)
                                        ),
                                        new AnyOf(
                                            new ActionCodeIs("FAD"),
                                            new ActionCodeIs("OFS")
                                        ),
                                        new NotBelow(
                                            new YearlyGatherer("Number of FAD sets"),
                                            limit
                                        )
                                    )
                                )
                            )
                )
            )
            .collect(toImmutableList());
    }
}
