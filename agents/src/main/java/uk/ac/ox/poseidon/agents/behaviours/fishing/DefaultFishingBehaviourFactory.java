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

package uk.ac.ox.poseidon.agents.behaviours.fishing;

import lombok.*;
import uk.ac.ox.poseidon.agents.behaviours.BehaviourFactory;
import uk.ac.ox.poseidon.agents.behaviours.disposition.DispositionProcess;
import uk.ac.ox.poseidon.agents.regulations.Regulations;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.agents.vessels.gears.FishingGear;
import uk.ac.ox.poseidon.agents.vessels.hold.Hold;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.function.Supplier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultFishingBehaviourFactory<C extends Content<C>>
    extends BehaviourFactory<Fishing<C>> {

    @NonNull private VesselScopeFactory<? extends FishingGear<C>> fishingGear;
    @NonNull private VesselScopeFactory<? extends Hold<C>> hold;
    @NonNull private VesselScopeFactory<? extends Supplier<Fisheable<C>>> fisheableSupplier;
    @NonNull private Factory<? extends Regulations> regulations;
    @NonNull private Factory<? extends DispositionProcess<C>> dispositionProcess;

    @Override
    protected Fishing<C> newInstance(
        final Simulation simulation,
        final Vessel vessel
    ) {
        return new Fishing<>(
            fishingGear.get(simulation, vessel),
            hold.get(simulation, vessel),
            fisheableSupplier.get(simulation, vessel),
            regulations.get(simulation),
            dispositionProcess.get(simulation)
        );
    }
}
