package uk.ac.ox.poseidon.epo;

import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.NamedRegulations;
import uk.ac.ox.oxfish.regulations.conditions.*;
import uk.ac.ox.oxfish.regulations.quantities.YearlyGatherer;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class GlobalObjectSetLimit implements PolicySupplier {

    private final List<Integer> yearsActive;
    private final List<Integer> limits;

    public GlobalObjectSetLimit(
        final List<Integer> yearsActive,
        final List<Integer> limits
    ) {
        this.yearsActive = yearsActive;
        this.limits = limits;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return limits
            .stream()
            .map(limit ->
                new Policy<EpoScenario<?>>(
                    String.format("Global limit of %d object sets", limit),
                    scenario ->
                        ((NamedRegulations) scenario.getRegulations())
                            .modify(
                                "Global object-set limits",
                                ignored -> new ForbiddenIf(
                                    new AllOf(
                                        new AnyOf(yearsActive.stream().map(InYear::new)),
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