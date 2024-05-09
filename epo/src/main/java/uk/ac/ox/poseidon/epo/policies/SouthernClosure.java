/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
import uk.ac.ox.poseidon.regulations.core.conditions.AllOfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.InRectangularAreaFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.NotBelowFactory;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.poseidon.epo.policies.ActiveFadLimitsPolicies.modifyActiveFadsLimit;

public class SouthernClosure extends PolicySupplier {

    private final int referenceYear;
    private final List<Double> proportionsOfCurrentActiveFadLimits;
    private final List<Integer> southernObjectSetsLimits;

    public SouthernClosure(
        final List<Integer> yearsActive,
        final int referenceYear,
        final List<Double> proportionsOfCurrentActiveFadLimits,
        final List<Integer> southernObjectSetsLimits
    ) {
        super(yearsActive);
        this.referenceYear = referenceYear;
        this.proportionsOfCurrentActiveFadLimits = proportionsOfCurrentActiveFadLimits;
        this.southernObjectSetsLimits = southernObjectSetsLimits;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return proportionsOfCurrentActiveFadLimits.stream().flatMap(proportion ->
            southernObjectSetsLimits.stream().map(setsLimit ->
                new Policy<EpoScenario<?>>(
                    String.format(
                        "Southern closure after %d sets at %02d%% of regular active FAD limits",
                        setsLimit,
                        (int) (proportion * 100)
                    ),
                    epoScenario -> {
                        modifyActiveFadsLimit(
                            referenceYear,
                            proportion,
                            getYearsActive(),
                            epoScenario
                        );
                        final NamedRegulationsFactory namedRegulations =
                            (NamedRegulationsFactory) epoScenario.getRegulations();
                        namedRegulations.modify(
                            "Southern closure",
                            () -> new ForbiddenIfFactory(
                                new AllOfFactory(
                                    yearsActiveCondition(),
                                    new InRectangularAreaFactory(
                                        0,
                                        -125,
                                        -20,
                                        -80
                                    ),
                                    new NotBelowFactory(
                                        new YearlyGatherer(
                                            "Sum of FAD and OFS sets in southern area"
                                        ),
                                        setsLimit
                                    )
                                )
                            )
                        );
                    }
                )
            )
        ).collect(toImmutableList());
    }
}
