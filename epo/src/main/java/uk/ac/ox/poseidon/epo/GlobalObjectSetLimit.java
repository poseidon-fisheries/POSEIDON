package uk.ac.ox.poseidon.epo;

import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.regulations.quantities.YearlyGatherer;
import uk.ac.ox.poseidon.regulations.core.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.core.NamedRegulationsFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.ActionCodeIsFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.AllOfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.AnyOfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.NotBelowFactory;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class GlobalObjectSetLimit extends PolicySupplier {

    private final List<Integer> limits;

    public GlobalObjectSetLimit(
        final List<Integer> yearsActive,
        final List<Integer> limits
    ) {
        super(yearsActive);
        this.limits = limits;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return limits
            .stream()
            .map(limit ->
                new Policy<EpoScenario<?>>(
                    String.format("Global limit of %04d object sets", limit),
                    scenario ->
                        ((NamedRegulationsFactory) scenario.getRegulations())
                            .modify(
                                "Global object-set limits",
                                () -> new ForbiddenIfFactory(
                                    new AllOfFactory(
                                        yearsActiveCondition(),
                                        new AnyOfFactory(
                                            new ActionCodeIsFactory("FAD"),
                                            new ActionCodeIsFactory("OFS")
                                        ),
                                        new NotBelowFactory(
                                            new YearlyGatherer("Number of FAD sets"),
                                            // this includes both FAD and OFS sets
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
