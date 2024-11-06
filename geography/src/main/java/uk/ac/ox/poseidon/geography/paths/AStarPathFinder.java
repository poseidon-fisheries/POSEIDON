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

package uk.ac.ox.poseidon.geography.paths;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import sim.util.Int2D;

import java.util.Optional;

@AllArgsConstructor
public class AStarPathFinder implements PathFinder<Int2D> {

    private final GridAdaptor gridAdaptor;
    private final IndexedAStarPathFinder<Int2D> pathFinder;
    private final Heuristic<Int2D> heuristic;

    public AStarPathFinder(
        final GridAdaptor gridAdaptor
    ) {
        this(
            gridAdaptor,
            new IndexedAStarPathFinder<>(gridAdaptor),
            (Int2D a, Int2D b) -> (float) gridAdaptor.getDistance().distanceBetween(a, b)
        );
    }

    @Override
    public Optional<ImmutableList<Int2D>> getPath(
        final Int2D start,
        final Int2D end
    ) {
        final DefaultGraphPath<Int2D> path = new DefaultGraphPath<>();
        final boolean found =
            pathFinder.searchNodePath(
                gridAdaptor.intern(start),
                gridAdaptor.intern(end),
                heuristic,
                path
            );
        return found
            ? Optional.of(ImmutableList.copyOf(path))
            : Optional.empty();
    }
}
