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
import uk.ac.ox.oxfish.regulations.quantities.YearlyGatherer;
import uk.ac.ox.poseidon.epo.scenarios.EpoScenario;
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
