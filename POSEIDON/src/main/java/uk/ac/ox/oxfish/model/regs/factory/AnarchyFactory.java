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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Simple factory returning the same singleton
 * Created by carrknight on 6/14/15.
 */
public class AnarchyFactory implements AlgorithmFactory<Anarchy> {

    private static Anarchy singleton = new Anarchy();

    /**
     * Getter for property 'singleton'.
     *
     * @return Value for property 'singleton'.
     */
    public static Anarchy getSingleton() {
        return singleton;
    }

    /**
     * returns the same singleton all the time
     */
    @Override
    public Anarchy apply(FishState state) {
        return singleton;
    }
}
