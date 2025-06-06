/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.maximization;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.maximization.generic.FixedDataTarget;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

class EvaluationResultsRowProvider implements RowProvider {

    private final FishState fishState;
    private final GenericOptimization optimization;

    private final List<String> HEADERS = ImmutableList.of(
        "target_class",
        "target_name",
        "target_value",
        "output_value",
        "error"
    );

    EvaluationResultsRowProvider(
        final FishState fishState,
        final GenericOptimization optimization
    ) {
        this.fishState = fishState;
        this.optimization = optimization;
    }

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    public Iterable<? extends List<?>> getRows() {
        return optimization
            .getTargets()
            .stream()
            .filter(FixedDataTarget.class::isInstance)
            .map(FixedDataTarget.class::cast)
            .map(target ->
                ImmutableList.of(
                    target.getClass().getSimpleName(),
                    target.getColumnName(),
                    target.getFixedTarget(),
                    target.getValue(fishState),
                    target.computeError(fishState)
                )
            )
            .collect(toImmutableList());
    }

}
