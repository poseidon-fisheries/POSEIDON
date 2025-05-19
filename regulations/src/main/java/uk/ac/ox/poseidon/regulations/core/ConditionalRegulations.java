/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.regulations.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Mode;
import uk.ac.ox.poseidon.regulations.api.Regulations;

public class ConditionalRegulations implements uk.ac.ox.poseidon.regulations.api.ConditionalRegulations {

    private final Condition condition;
    private final Regulations regulationsIfTrue;
    private final Regulations regulationsIfFalse;

    public ConditionalRegulations(
        final Condition condition,
        final Regulations regulationsIfTrue,
        final Regulations regulationsIfFalse
    ) {
        this.condition = condition;
        this.regulationsIfTrue = regulationsIfTrue;
        this.regulationsIfFalse = regulationsIfFalse;
    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public Regulations getRegulationIfTrue() {
        return regulationsIfTrue;
    }

    @Override
    public Regulations getRegulationIfFalse() {
        return regulationsIfFalse;
    }

    @Override
    public Mode mode(final Action action) {
        final Regulations regulations =
            condition.test(action) ?
                regulationsIfTrue :
                regulationsIfFalse;
        return regulations.mode(action);
    }

}
