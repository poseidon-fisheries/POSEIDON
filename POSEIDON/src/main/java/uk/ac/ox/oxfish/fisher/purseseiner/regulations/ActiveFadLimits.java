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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.regulations.quantities.NumberOfActiveFads;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Mode;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.*;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class ActiveFadLimits implements Regulations {

    private final Map<Integer, ? extends Map<String, Integer>> limitsPerYearAndClass;
    private final Regulations regulations;
    public ActiveFadLimits(
        final Map<Integer, ? extends Map<String, Integer>> limitsPerYearAndClass,
        final ModelState modelState
    ) {
        this.limitsPerYearAndClass =
            limitsPerYearAndClass.entrySet().stream().collect(toImmutableMap(
                Map.Entry::getKey,
                entry -> ImmutableMap.copyOf(entry.getValue())
            ));
        this.regulations = makeRegulations(modelState);
    }

    public Map<Integer, ? extends Map<String, Integer>> getLimitsPerYearAndClass() {
        return limitsPerYearAndClass;
    }

    private Regulations makeRegulations(final ModelState modelState) {
        return new ForbiddenIfFactory(
            new AllOfFactory(
                new ActionCodeIsFactory("DPL"),
                new AnyOfFactory(
                    limitsPerYearAndClass.entrySet().stream().map(yearAndLimits ->
                        new AllOfFactory(
                            new InYearFactory(yearAndLimits.getKey()),
                            new AnyOfFactory(
                                yearAndLimits.getValue().entrySet().stream().map(classAndLimit ->
                                    new AllOfFactory(
                                        new AgentHasTagFactory("class " + classAndLimit.getKey()),
                                        new NotBelowFactory(new NumberOfActiveFads(), classAndLimit.getValue())
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).apply(modelState);
    }

    @Override
    public Mode mode(final Action action) {
        return regulations.mode(action);
    }

    @Override
    public Collection<Regulations> getSubRegulations() {
        return ImmutableList.of(regulations);
    }
}
