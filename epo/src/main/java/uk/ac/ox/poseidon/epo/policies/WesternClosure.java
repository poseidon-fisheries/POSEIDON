/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
import uk.ac.ox.poseidon.epo.scenarios.EpoScenario;
import uk.ac.ox.poseidon.regulations.core.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.core.NamedRegulationsFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.AllOfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.BetweenYearlyDatesFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.InRectangularAreaFactory;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.*;

public class WesternClosure extends PolicySupplier {

    private final double eastLongitude;
    private final List<Integer> numberOfExtraDays;

    public WesternClosure(
        final List<Integer> yearsActive,
        final double eastLongitude,
        final List<Integer> numberOfExtraDays
    ) {
        super(yearsActive);
        this.eastLongitude = eastLongitude;
        this.numberOfExtraDays = numberOfExtraDays;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return numberOfExtraDays.stream().map(extraDays ->
            new Policy<EpoScenario<?>>(
                String.format(
                    "West of %.0f, %02d days before/after El Corralito",
                    eastLongitude,
                    extraDays
                ),
                epoScenario -> {
                    final NamedRegulationsFactory namedRegulations =
                        (NamedRegulationsFactory) epoScenario.getRegulations();
                    namedRegulations.modify(
                        "Western closure",
                        () -> new ForbiddenIfFactory(
                            new AllOfFactory(
                                yearsActiveCondition(),
                                new BetweenYearlyDatesFactory(
                                    addDays(EL_CORRALITO_BEGINNING, -extraDays),
                                    addDays(EL_CORRALITO_END, extraDays)
                                ),
                                new InRectangularAreaFactory(
                                    50,
                                    -150,
                                    -50,
                                    eastLongitude
                                )
                            )
                        )
                    );
                }
            )
        ).collect(toImmutableList());
    }
}
