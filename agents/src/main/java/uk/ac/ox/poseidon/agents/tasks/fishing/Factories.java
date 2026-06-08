/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.tasks.fishing;

import uk.ac.ox.poseidon.agents.catches.disposition.DispositionProcess;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.regulations.Regulations;

import java.util.function.Supplier;

public class Factories {

    private Factories() {}

    public static CheckIfFishingHereAndNowIsLegalFactory checkIfFishingHereAndNowIsLegal(
        final Factory<
            ? super VesselScope,
            ? extends Regulations<? super ExtendedFishingAction>
            > regulations
    ) {
        return new CheckIfFishingHereAndNowIsLegalFactory(regulations);
    }

    public static FishingEventAccumulatorFactory fishingEventAccumulator() {
        return new FishingEventAccumulatorFactory();
    }

    public static FishingFactory fishing(
        final Factory<? super VesselScope, ? extends Supplier<Fisheable>> fisheableSupplier,
        final Factory<? super VesselScope, ? extends DispositionProcess> dispositionProcess
    ) {
        return new FishingFactory(fisheableSupplier, dispositionProcess);
    }
}
