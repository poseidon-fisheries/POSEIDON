package uk.ac.ox.poseidon.epo;

import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.Closure;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.IndividualBetLimits;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.YearsActive;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.regulations.NamedRegulations;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;

public class ExtendedClosurePolicies implements PolicySupplier {

    private final List<Integer> yearsActive;

    private final List<Integer> daysToExtend;

    public ExtendedClosurePolicies(final List<Integer> yearsActive, final List<Integer> daysToExtend) {
        this.yearsActive = yearsActive;
        this.daysToExtend = daysToExtend;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return daysToExtend.stream().map(days ->
            new Policy<EpoScenario<?>>(
                String.format("Closures extended by %d days", days),
                scenario -> {
                    final NamedRegulations namedRegulations = (NamedRegulations) scenario.getRegulations();
                    final Map<String, AlgorithmFactory<Regulations>> regulationMap = namedRegulations.getRegulations();
                    final Closure closureA = (Closure) regulationMap.get("Closure A");
                    final Closure closureB = (Closure) regulationMap.get("Closure B");
                    final IndividualBetLimits betLimits = (IndividualBetLimits) regulationMap.get("BET limits");
                    Stream.of(closureA, closureB, betLimits).forEach(this::deactivateForYearsActive);
                    final Closure newClosureA = new Closure(
                        yearsActive,
                        closureA.getAgentTag().getValue(),
                        addDays(closureA.getBeginning(), -days),
                        closureA.getEnd(),
                        closureA.getDaysToForbidDeploymentsBefore().getIntValue()
                    );
                    final Closure newClosureB = new Closure(
                        yearsActive,
                        closureA.getAgentTag().getValue(),
                        closureA.getBeginning(),
                        addDays(closureB.getEnd(), days),
                        closureA.getDaysToForbidDeploymentsBefore().getIntValue()
                    );
                    namedRegulations.modify("New closure A", __ -> newClosureA);
                    namedRegulations.modify("New closure B", __ -> newClosureB);
                    namedRegulations.modify(
                        "New BET limits",
                        __ -> new IndividualBetLimits(
                            newClosureA,
                            newClosureB,
                            betLimits.getAdditionalClosureDaysByExcessTonnesOfBet(),
                            yearsActive
                        )
                    );
                }
            )
        ).collect(toImmutableList());
    }

    private void deactivateForYearsActive(final YearsActive regulation) {
        regulation.setYearsActive(
            regulation.getYearsActive()
                .stream()
                .filter(year -> !yearsActive.contains(year))
                .collect(toImmutableList())
        );
    }

}
