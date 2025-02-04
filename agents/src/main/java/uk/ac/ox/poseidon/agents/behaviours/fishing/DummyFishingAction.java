/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.agents.behaviours.fishing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.AbstractAction;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.biology.Bucket;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@SuppressFBWarnings(value = "EI2", justification = "Int2D is actually immutable.")
public class DummyFishingAction extends AbstractAction implements FishingAction {

    private final Int2D cell;

    public DummyFishingAction(
        final LocalDateTime start,
        final Vessel vessel,
        final Int2D cell
    ) {
        super(vessel, start, Duration.ofSeconds(1));
        this.cell = cell;
    }

    @Override
    public Bucket<?> getFishCaught() {
        return Bucket.empty();
    }

}
