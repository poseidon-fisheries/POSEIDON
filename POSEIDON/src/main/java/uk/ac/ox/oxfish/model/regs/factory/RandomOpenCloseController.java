/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs.factory;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.ExternalOpenCloseSeason;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;

import java.util.function.Supplier;

/**
 * Special use controller that is basically a random shodan. Not listed.
 * Created by carrknight on 1/14/17.
 */
public class RandomOpenCloseController implements AlgorithmFactory<ExternalOpenCloseSeason>{

    private final Locker<String,ExternalOpenCloseSeason> locker = new Locker<>();

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public ExternalOpenCloseSeason apply(FishState fishState) {
        return locker.presentKey
                (fishState.getHopefullyUniqueID(),
                 new Supplier<ExternalOpenCloseSeason>() {
                     @Override
                     public ExternalOpenCloseSeason get() {
                         ExternalOpenCloseSeason toReturn = new ExternalOpenCloseSeason();

                         fishState.scheduleEveryXDay(new Steppable() {
                             @Override
                             public void step(SimState simState) {
                                 toReturn.setOpen(fishState.getRandom().nextBoolean());
                             }
                         }, StepOrder.POLICY_UPDATE,30);

                         return toReturn;
                     }
                 });

    }
}
