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

package uk.ac.ox.oxfish.model.data;

import uk.ac.ox.oxfish.model.FishState;

/**
 * Any object that wants to write a file out at the end of the simulation can register as a OutputPlugin with the state
 * and the YamlMain will output it before closing the simulation
 * Created by carrknight on 1/10/17.
 */
public interface OutputPlugin {


    public void reactToEndOfSimulation(FishState state);

    public String getFileName();

    public String composeFileContents();

}
