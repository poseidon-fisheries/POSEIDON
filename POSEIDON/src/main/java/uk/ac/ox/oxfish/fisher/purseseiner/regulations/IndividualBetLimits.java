/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    private Map<String, Integer> additionalClosureDaysByExcessTonnesOfBet;
    private List<Integer> yearsActive;

    @SuppressWarnings("unused")
    public IndividualBetLimits() {
    }

    @SuppressWarnings("WeakerAccess")
    public IndividualBetLimits(
        final TemporalClosure closureA,
        final TemporalClosure closureB,
        final Map<String, Integer> additionalClosureDaysByExcessTonnesOfBet,
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
    public Map<String, Integer> getAdditionalClosureDaysByExcessTonnesOfBet() {
        return additionalClosureDaysByExcessTonnesOfBet;
    }

    @SuppressWarnings("unused")
    public void setAdditionalClosureDaysByExcessTonnesOfBet(final Map<String, Integer> additionalClosureDaysByExcessTonnesOfBet) {
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
                            additionalClosureDaysByExcessTonnesOfBet
                                .entrySet()
                                .stream()
                                .map(entry ->
                                    new AllOfFactory(
                                        new AboveFactory(
                                            new LastYearlyFisherValue("Bigeye tuna Catches (kg)"),
                                            Integer.parseInt(entry.getKey()) * 1000
                                            // convert from tonnes to kg
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
                            additionalClosureDaysByExcessTonnesOfBet
                                .entrySet()
                                .stream()
                                .map(entry ->
                                    // This gets slightly complicated because we need to check
                                    // for the catches
                                    // the year before the closure _starts_, and once we get to
                                    // Jan 1st, that
                                    // not "last year" anymore, but the year before that, hence
                                    // those different
                                    // conditions depending on where we are in the year. March 30
                                    // is an arbitrary
                                    // cutoff for the different checks. At least we do not need
                                    // to deal with the
                                    // pre-closure DPL ban, since closure B gets extended at the
                                    // end.
                                    new AllOfFactory(
                                        new AnyOfFactory(
                                            new AllOfFactory(
                                                new BetweenYearlyDatesFactory(
                                                    JANUARY,
                                                    1,
                                                    MARCH,
                                                    30
                                                ),
                                                new AboveFactory(
                                                    new SecondLastYearlyFisherValue(
                                                        "Bigeye tuna Catches (kg)"),
                                                    Integer.parseInt(entry.getKey()) * 1000
                                                    // convert from tonnes to kg
                                                )
                                            ),
                                            new AllOfFactory(
                                                new BetweenYearlyDatesFactory(
                                                    APRIL,
                                                    1,
                                                    DECEMBER,
                                                    31
                                                ),
                                                new AboveFactory(
                                                    new LastYearlyFisherValue(
                                                        "Bigeye tuna Catches (kg)"),
                                                    Integer.parseInt(entry.getKey()) * 1000
                                                    // convert from tonnes to kg
                                                )
                                            )
                                        ),
                                        new TemporalClosureExtensionAfterFactory(
                                            closureB,
                                            entry.getValue()
                                        )
                                    )
                                )
                        )
                    )
                )
            )
        ).apply(modelState);
    }
}
