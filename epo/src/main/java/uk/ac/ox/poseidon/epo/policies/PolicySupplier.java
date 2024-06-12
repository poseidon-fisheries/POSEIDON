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

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.YearsActive;
import uk.ac.ox.poseidon.epo.scenarios.EpoScenario;
import uk.ac.ox.poseidon.regulations.core.conditions.AnyOfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.InYearFactory;

import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableList.toImmutableList;

abstract class PolicySupplier implements Supplier<List<Policy<EpoScenario<?>>>> {

    static final Policy<EpoScenario<?>> CURRENT_REGULATIONS = new Policy<>(
        "Current regulations",
        scenario -> {
        }
    );

    private final List<Integer> yearsActive;

    PolicySupplier(final List<Integer> yearsActive) {
        this.yearsActive = yearsActive;
    }

    public List<Integer> getYearsActive() {
        return yearsActive;
    }

    AnyOfFactory yearsActiveCondition() {
        return new AnyOfFactory(yearsActive.stream().map(InYearFactory::new));
    }

    protected void deactivateForYearsActive(final YearsActive regulation) {
        regulation.setYearsActive(
            regulation.getYearsActive()
                .stream()
                .filter(year -> !yearsActive.contains(year))
                .collect(toImmutableList())
        );
    }

    List<Policy<EpoScenario<?>>> getWithDefault() {
        return ImmutableList.<Policy<EpoScenario<?>>>builder()
            .add(CURRENT_REGULATIONS)
            .addAll(get())
            .build();
    }

    List<Policy<EpoScenario<?>>> getWithoutDefault() {
        return ImmutableList.<Policy<EpoScenario<?>>>builder()
            .addAll(get())
            .build();
    }

}
