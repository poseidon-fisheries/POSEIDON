package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.regulations.quantities.LastYearlyFisherValue;
import uk.ac.ox.oxfish.regulations.quantities.SecondLastYearlyFisherValue;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.*;

import java.util.List;
import java.util.Map;

import static java.time.Month.*;

public class IndividualBetLimits implements ComponentFactory<Regulations>, YearsActive {

    private TemporalClosure closureA;
    private TemporalClosure closureB;
    private Map<Integer, Integer> additionalClosureDaysByExcessTonnesOfBet;
    private List<Integer> yearsActive;

    @SuppressWarnings("unused")
    public IndividualBetLimits() {
    }

    @SuppressWarnings("WeakerAccess")
    public IndividualBetLimits(
        final TemporalClosure closureA,
        final TemporalClosure closureB,
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
    public TemporalClosure getClosureA() {
        return closureA;
    }

    @SuppressWarnings("unused")
    public void setClosureA(final TemporalClosure closureA) {
        this.closureA = closureA;
    }

    @SuppressWarnings("unused")
    public TemporalClosure getClosureB() {
        return closureB;
    }

    @SuppressWarnings("unused")
    public void setClosureB(final TemporalClosure closureB) {
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
    public Regulations apply(final ModelState modelState) {
        return new ForbiddenIfFactory(
            new AllOfFactory(
                new AnyOfFactory(
                    yearsActive.stream().map(InYearFactory::new).toArray(InYearFactory[]::new)
                ),
                new AnyOfFactory(
                    new AllOfFactory(
                        new AgentHasTagFactory(closureA.getAgentTag()),
                        new AnyOfFactory(
                            additionalClosureDaysByExcessTonnesOfBet.entrySet().stream().map(entry ->
                                new AllOfFactory(
                                    new AboveFactory(
                                        new LastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                        entry.getKey() * 1000 // convert from tonnes to kg
                                    ),
                                    new TemporalClosureExtensionBeforeFactory(
                                        closureA,
                                        entry.getValue()
                                    )
                                )
                            )
                        )
                    ),
                    new AllOfFactory(
                        new AgentHasTagFactory(closureB.getAgentTag()),
                        new AnyOfFactory(
                            additionalClosureDaysByExcessTonnesOfBet.entrySet().stream().map(entry ->
                                // This gets slightly complicated because we need to check for the catches
                                // the year before the closure _starts_, and once we get to Jan 1st, that
                                // not "last year" anymore, but the year before that, hence those different
                                // conditions depending on where we are in the year. March 30 is an arbitrary
                                // cutoff for the different checks. At least we do not need to deal with the
                                // pre-closure DPL ban, since closure B gets extended at the end.
                                new AllOfFactory(
                                    new AnyOfFactory(
                                        new AllOfFactory(
                                            new BetweenYearlyDatesFactory(JANUARY, 1, MARCH, 30),
                                            new AboveFactory(
                                                new SecondLastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                                entry.getKey() * 1000 // convert from tonnes to kg
                                            )
                                        ),
                                        new AllOfFactory(
                                            new BetweenYearlyDatesFactory(APRIL, 1, DECEMBER, 31),
                                            new AboveFactory(
                                                new LastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                                entry.getKey() * 1000 // convert from tonnes to kg
                                            )
                                        )
                                    ),
                                    new TemporalClosureExtensionAfterFactory(closureB, entry.getValue())
                                )
                            )
                        )
                    )
                )
            )
        ).apply(modelState);
    }
}
