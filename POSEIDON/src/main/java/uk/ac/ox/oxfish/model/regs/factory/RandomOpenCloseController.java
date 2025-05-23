/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs.factory;

import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.ExternalOpenCloseSeason;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Special use controller that is basically a random shodan. Not listed.
 * Created by carrknight on 1/14/17.
 */
public class RandomOpenCloseController implements AlgorithmFactory<ExternalOpenCloseSeason> {

    @SuppressWarnings("deprecation")
    private final uk.ac.ox.oxfish.utility.Locker<String, ExternalOpenCloseSeason> locker =
        new uk.ac.ox.oxfish.utility.Locker<>();

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public ExternalOpenCloseSeason apply(final FishState fishState) {
        return locker.presentKey
            (
                fishState.getUniqueID(),
                () -> {
                    final ExternalOpenCloseSeason toReturn = new ExternalOpenCloseSeason();

                    fishState.scheduleEveryXDay((Steppable) simState -> toReturn.setOpen(fishState.getRandom()
                        .nextBoolean()), StepOrder.POLICY_UPDATE, 30);

                    return toReturn;
                }
            );

    }
}
