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
 *
 */

package uk.ac.ox.poseidon.agents.behaviours.destination;

import ec.util.MersenneTwisterFast;
import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.choices.OptionValues;
import uk.ac.ox.poseidon.agents.behaviours.choices.Picker;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static uk.ac.ox.poseidon.core.MasonUtils.shuffledStream;

@RequiredArgsConstructor
public class NeighbourhoodCellPicker implements Picker<Int2D> {

    private final Vessel vessel;
    private final OptionValues<Int2D> optionValues;
    private final Predicate<Int2D> optionPredicate;
    private final GridPathFinder pathFinder;
    private final IntSupplier neighbourhoodSizeSupplier;
    private final MersenneTwisterFast rng;

    @Override
    public Optional<Int2D> pick() {
        final int maxNeighbourhoodSize = max(
            pathFinder.getModelGrid().getGridWidth(),
            pathFinder.getModelGrid().getGridHeight()
        );
        int neighbourhoodSize = neighbourhoodSizeSupplier.getAsInt();
        List<Int2D> candidates = List.of();
        while (candidates.isEmpty() && neighbourhoodSize <= maxNeighbourhoodSize) {
            final Int2D startingCell = optionValues.getBestOption(rng).orElse(vessel.getCell());
            candidates = pathFinder.getAccessibleWaterNeighbours(startingCell, neighbourhoodSize);
            neighbourhoodSize++;
        }
        return shuffledStream(candidates, rng)
            .filter(optionPredicate)
            .findFirst();
    }
}
