package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.conditions.*;
import uk.ac.ox.oxfish.regulations.quantities.LastYearlyFisherValue;
import uk.ac.ox.oxfish.regulations.quantities.SecondLastYearlyFisherValue;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.List;
import java.util.Map;

import static java.time.Month.*;

public class IndividualBetLimits implements RegulationFactory {

    private Closure closureA;
    private Closure closureB;
    private Map<Integer, Integer> additionalClosureDaysByExcessTonnesOfBet;
    private List<Integer> yearsActive;

    @SuppressWarnings("unused")
    public IndividualBetLimits() {
    }

    @SuppressWarnings("WeakerAccess")
    public IndividualBetLimits(
        final Closure closureA,
        final Closure closureB,
        final Map<Integer, Integer> additionalClosureDaysByExcessTonnesOfBet,
        final List<Integer> yearsActive
    ) {
        this.closureA = closureA;
        this.closureB = closureB;
        this.additionalClosureDaysByExcessTonnesOfBet = additionalClosureDaysByExcessTonnesOfBet;
        this.yearsActive = yearsActive;
    }

    @SuppressWarnings("unused")
    public List<Integer> getYearsActive() {
        return yearsActive;
    }

    @SuppressWarnings("unused")
    public void setYearsActive(final List<Integer> yearsActive) {
        this.yearsActive = yearsActive;
    }

    @SuppressWarnings("unused")
    public Closure getClosureA() {
        return closureA;
    }

    @SuppressWarnings("unused")
    public void setClosureA(final Closure closureA) {
        this.closureA = closureA;
    }

    @SuppressWarnings("unused")
    public Closure getClosureB() {
        return closureB;
    }

    @SuppressWarnings("unused")
    public void setClosureB(final Closure closureB) {
        this.closureB = closureB;
    }

    @SuppressWarnings("unused")
    public Map<Integer, Integer> getAdditionalClosureDaysByExcessTonnesOfBet() {
        return additionalClosureDaysByExcessTonnesOfBet;
    }

    @SuppressWarnings("unused")
    public void setAdditionalClosureDaysByExcessTonnesOfBet(final Map<Integer, Integer> additionalClosureDaysByExcessTonnesOfBet) {
        this.additionalClosureDaysByExcessTonnesOfBet = additionalClosureDaysByExcessTonnesOfBet;
    }

    @Override
    public AlgorithmFactory<Regulations> get() {
        return new ForbiddenIf(
            new AllOf(
                new AnyOf(
                    yearsActive.stream().map(InYear::new).toArray(InYear[]::new)
                ),
                new AnyOf(
                    new AllOf(
                        new AgentHasTag(closureA.getAgentTag()),
                        new AnyOf(
                            additionalClosureDaysByExcessTonnesOfBet.entrySet().stream().map(entry ->
                                new AllOf(
                                    new Above(
                                        new LastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                        entry.getKey() * 1000 // convert from tonnes to kg
                                    ),
                                    new ClosureExtensionBefore(
                                        closureA,
                                        entry.getValue()
                                    )
                                )
                            )
                        )
                    ),
                    new AllOf(
                        new AgentHasTag(closureB.getAgentTag()),
                        new AnyOf(
                            additionalClosureDaysByExcessTonnesOfBet.entrySet().stream().map(entry ->
                                // This gets slightly complicated because we need to check for the catches
                                // the year before the closure _starts_, and once we get to Jan 1st, that
                                // not "last year" anymore, but the year before that, hence those different
                                // conditions depending on where we are in the year. March 30 is an arbitrary
                                // cutoff for the different checks. At least we do not need to deal with the
                                // pre-closure DPL ban, since closure B gets extended at the end.
                                new AllOf(
                                    new AnyOf(
                                        new AllOf(
                                            new BetweenYearlyDates(JANUARY, 1, MARCH, 30),
                                            new Above(
                                                new SecondLastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                                entry.getKey() * 1000 // convert from tonnes to kg
                                            )
                                        ),
                                        new AllOf(
                                            new BetweenYearlyDates(APRIL, 1, DECEMBER, 31),
                                            new Above(
                                                new LastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                                entry.getKey() * 1000 // convert from tonnes to kg
                                            )
                                        )
                                    ),
                                    new ClosureExtensionAfter(closureB, entry.getValue())
                                )
                            )
                        )
                    )
                )
            )
        );
    }
}
