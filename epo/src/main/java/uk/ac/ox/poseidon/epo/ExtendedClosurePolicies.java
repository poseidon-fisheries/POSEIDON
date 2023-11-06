package uk.ac.ox.poseidon.epo;

import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.IndividualBetLimits;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.TemporalClosure;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.regulations.NamedRegulations;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;

public class ExtendedClosurePolicies extends PolicySupplier {

    private final List<Integer> daysToExtend;

    ExtendedClosurePolicies(final List<Integer> yearsActive, final List<Integer> daysToExtend) {
        super(yearsActive);
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
                    final TemporalClosure closureA = (TemporalClosure) regulationMap.get("Closure A");
                    final TemporalClosure closureB = (TemporalClosure) regulationMap.get("Closure B");
                    final IndividualBetLimits betLimits = (IndividualBetLimits) regulationMap.get("BET limits");
                    Stream.of(closureA, closureB, betLimits).forEach(this::deactivateForYearsActive);
                    final TemporalClosure newClosureA = new TemporalClosure(
                        getYearsActive(),
                        closureA.getAgentTag().getValue(),
                        addDays(closureA.beginning(), -days),
                        closureA.end(),
                        closureA.getDaysToForbidDeploymentsBefore().getIntValue()
                    );
                    final TemporalClosure newClosureB = new TemporalClosure(
                        getYearsActive(),
                        closureA.getAgentTag().getValue(),
                        closureA.beginning(),
                        addDays(closureB.end(), days),
                        closureA.getDaysToForbidDeploymentsBefore().getIntValue()
                    );
                    namedRegulations.modify("New closure A", () -> newClosureA);
                    namedRegulations.modify("New closure B", () -> newClosureB);
                    namedRegulations.modify(
                        "New BET limits",
                        () -> new IndividualBetLimits(
                            newClosureA,
                            newClosureB,
                            betLimits.getAdditionalClosureDaysByExcessTonnesOfBet(),
                            getYearsActive()
                        )
                    );
                }
            )
        ).collect(toImmutableList());
    }

}
