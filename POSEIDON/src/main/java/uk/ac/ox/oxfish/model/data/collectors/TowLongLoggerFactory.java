/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.data.collectors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.AltitudeOutput;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class TowLongLoggerFactory implements AlgorithmFactory<TowLongLogger> {


    private String fileName = "tow_log.csv";

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public TowLongLogger apply(FishState state) {

        state.getOutputPlugins().add(new AltitudeOutput(state.getMap()));

        return new TowLongLogger(fileName);
    }


    /**
     * Getter for property 'fileName'.
     *
     * @return Value for property 'fileName'.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter for property 'fileName'.
     *
     * @param fileName Value to set for property 'fileName'.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
