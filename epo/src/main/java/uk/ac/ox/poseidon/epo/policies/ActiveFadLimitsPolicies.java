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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.ActionType;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.PlannedStrategy;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.PlannedStrategyProxy;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
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

    private final boolean addActionOverride;

    ActiveFadLimitsPolicies(
        final List<Integer> yearsActive,
        final int referenceYear,
        final List<Double> proportionsOfCurrentLimits,
        final boolean addActionOverride
    ) {
        super(yearsActive);
        this.referenceYear = referenceYear;
        this.proportionsOfCurrentLimits = proportionsOfCurrentLimits;
        this.addActionOverride = addActionOverride;
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
                    scenario -> {
                        modifyActiveFadsLimit(
                            referenceYear,
                            proportion,
                            getYearsActive(),
                            scenario
                        );
                        if (addActionOverride) {
                            addActionOverride(scenario);
                        }
                    }

                )
            )
            .collect(toImmutableList());
    }

    public static void modifyActiveFadsLimit(
        final int referenceYear,
        final Double proportion,
        final List<Integer> yearsActive,
        final EpoScenario<?> scenario
    ) {
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
                yearsActive.forEach(year -> builder.put(year, newLimits));
                return new ActiveFadLimitsFactory(builder.buildKeepingLast());
            }
        );
    }

    private void addActionOverride(
        final EpoScenario<?> scenario
    ) {
        scenario.getAdditionalStartables().put(
            "FAD deployment action override",
            // Those lambdas are crazy, but we:
            fishState1 -> // create an algorithm factory
                fishState2 -> // that creates a startable
                    fishState2.scheduleOnceInXDays(
                        simState -> { // that schedules a steppable...
                            final ImmutableList<ActionType> overrides =
                                ImmutableList.of(ActionType.DeploymentAction);
                            ((FishState) simState)
                                .getFishers()
                                .stream()
                                .map(Fisher::getDestinationStrategy)
                                .filter(PlannedStrategyProxy.class::isInstance)
                                .map(PlannedStrategyProxy.class::cast)
                                .map(PlannedStrategyProxy::getDelegate)
                                .map(PlannedStrategy::getPlanner)
                                // some vessels (mostly dolphin-setters) have zero empirical
                                // deployments
                                // and thus no "plan module" with deployment location
                                // preferences, so
                                // we exclude those from the "deploy as much as possible" variation
                                .filter(planner -> planner
                                    .getPlanningModules()
                                    .containsKey(ActionType.DeploymentAction))
                                .forEach(planner -> planner.setActionPreferenceOverrides(overrides));
                        },
                        StepOrder.DAWN,
                        (365 * 2) + 1 // Jan. 1st of the third year
                    )
        );
    }
}
