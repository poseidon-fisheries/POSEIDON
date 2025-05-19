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

package uk.ac.ox.poseidon.epo.policies;

import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.IndividualBetLimits;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.TemporalClosure;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.YearsActive;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.epo.scenarios.EpoScenario;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.NamedRegulationsFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;

public class ExtendedClosurePolicies extends PolicySupplier {

    private final List<Integer> daysToExtend;

    ExtendedClosurePolicies(
        final List<Integer> yearsActive,
        final List<Integer> daysToExtend
    ) {
        super(yearsActive);
        this.daysToExtend = daysToExtend;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return daysToExtend.stream().map(days ->
            new Policy<EpoScenario<?>>(
                String.format("Closures extended by %02d days", days),
                scenario -> {
                    final NamedRegulationsFactory namedRegulations =
                        (NamedRegulationsFactory) scenario.getRegulations();
                    final Map<String, ComponentFactory<Regulations>> regulationMap = namedRegulations.getRegulations();
                    final TemporalClosure closureA = (TemporalClosure) regulationMap.get("Closure A");
                    final TemporalClosure closureB = (TemporalClosure) regulationMap.get("Closure B");
                    final IndividualBetLimits betLimits = (IndividualBetLimits) regulationMap.get("BET limits");
                    Stream.<YearsActive>of(closureA, closureB, betLimits).forEach(this::deactivateForYearsActive);
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
