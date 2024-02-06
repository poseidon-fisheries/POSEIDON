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
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.epo.scenarios.EpoScenario;
import uk.ac.ox.poseidon.regulations.core.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.core.NamedRegulationsFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.*;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.*;

public class ExtendedElCorralitoPolicy extends PolicySupplier {

    private final double newSouthLatitude;
    private final double newNorthLatitude;
    private final double newWestLongitude;
    private final List<Integer> numberOfExtraDays;

    ExtendedElCorralitoPolicy(
        final List<Integer> yearsActive,
        final double newSouthLatitude,
        final double newNorthLatitude,
        final double newWestLongitude,
        final List<Integer> numberOfExtraDays
    ) {
        super(yearsActive);
        this.newSouthLatitude = newSouthLatitude;
        this.newNorthLatitude = newNorthLatitude;
        this.newWestLongitude = newWestLongitude;
        this.numberOfExtraDays = numberOfExtraDays;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return numberOfExtraDays.stream().map(extraDays ->
            new Policy<EpoScenario<?>>(
                String.format("Larger El Corralito + %02d days before/after", extraDays),
                scenario -> {
                    final NamedRegulationsFactory namedRegulations =
                        (NamedRegulationsFactory) scenario.getRegulations();
                    namedRegulations.modify(
                        "El Corralito",
                        () -> new ForbiddenIfFactory(
                            new AnyOfFactory(
                                new AllOfFactory(
                                    new NotFactory(yearsActiveCondition()),
                                    ((ForbiddenIfFactory) namedRegulations.getRegulations()
                                        .get("El Corralito"))
                                        .getCondition()
                                ),
                                new AllOfFactory(
                                    yearsActiveCondition(),
                                    new BetweenYearlyDatesFactory(
                                        addDays(EL_CORRALITO_BEGINNING, -extraDays),
                                        addDays(EL_CORRALITO_END, extraDays)
                                    ),
                                    new InRectangularAreaFactory(
                                        newNorthLatitude,
                                        newWestLongitude,
                                        newSouthLatitude,
                                        ((FixedDoubleParameter) EL_CORRALITO_AREA.getEastLongitude()).getDoubleValue()
                                    )
                                )
                            )
                        )
                    );
                }
            )
        ).collect(toImmutableList());
    }
}
