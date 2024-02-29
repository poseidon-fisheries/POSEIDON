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

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.ActiveFadLimitsFactory;
import uk.ac.ox.poseidon.epo.scenarios.EpoScenario;
import uk.ac.ox.poseidon.regulations.core.NamedRegulationsFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.ACTIVE_FAD_LIMITS;

public class ActiveFadLimitsPolicies extends PolicySupplier {

    private final int referenceYear;
    private final List<Double> proportionsOfCurrentLimits;

    ActiveFadLimitsPolicies(
        final List<Integer> yearsActive,
        final int referenceYear,
        final List<Double> proportionsOfCurrentLimits
    ) {
        super(yearsActive);
        this.referenceYear = referenceYear;
        this.proportionsOfCurrentLimits = proportionsOfCurrentLimits;
    }

    @Override
    public List<Policy<EpoScenario<?>>> get() {
        return proportionsOfCurrentLimits
            .stream()
            .map(proportion ->
                new Policy<EpoScenario<?>>(
                    String.format(
                        "%02d%% of regular active FAD limits",
                        (int) (proportion * 100)
                    ),
                    scenario ->
                        ((NamedRegulationsFactory) scenario.getRegulations()).modify(
                            "Active-FAD limits",
                            () -> {
                                final ImmutableMap<String, Integer> newLimits = ACTIVE_FAD_LIMITS
                                    .get(referenceYear)
                                    .entrySet()
                                    .stream()
                                    .collect(toImmutableMap(
                                        Entry::getKey,
                                        entry -> (int) (entry.getValue() * proportion)
                                    ));
                                final ImmutableMap.Builder<Integer, Map<String, Integer>> builder =
                                    ImmutableMap.<Integer, Map<String, Integer>>builder()
                                        .putAll(ACTIVE_FAD_LIMITS);
                                getYearsActive().forEach(year -> builder.put(year, newLimits));
                                return new ActiveFadLimitsFactory(builder.buildKeepingLast());
                            }
                        )
                )
            )
            .collect(toImmutableList());
    }
}
