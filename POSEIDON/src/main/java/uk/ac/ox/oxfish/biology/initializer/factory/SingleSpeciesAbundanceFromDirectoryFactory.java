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

package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Paths;

/**
 * The factory that generates the single species abundance biology.
 * Created by carrknight on 3/11/16.
 */
public class SingleSpeciesAbundanceFromDirectoryFactory implements AlgorithmFactory<SingleSpeciesAbundanceInitializer> {


    private String folderPath = "inputs/california/biology/Dover Sole/";

    private String speciesName = "Dover Sole";

    private double scaling = 1d;


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public SingleSpeciesAbundanceInitializer apply(FishState fishState) {
        return new SingleSpeciesAbundanceInitializer(
            Paths.get(folderPath),
            speciesName,
            scaling,
            fishState
        );
    }

    /**
     * Getter for property 'folderPath'.
     *
     * @return Value for property 'folderPath'.
     */
    public String getFolderPath() {
        return folderPath;
    }

    /**
     * Setter for property 'folderPath'.
     *
     * @param folderPath Value to set for property 'folderPath'.
     */
    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    /**
     * Getter for property 'speciesName'.
     *
     * @return Value for property 'speciesName'.
     */
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Setter for property 'speciesName'.
     *
     * @param speciesName Value to set for property 'speciesName'.
     */
    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    /**
     * Getter for property 'scaling'.
     *
     * @return Value for property 'scaling'.
     */
    public double getScaling() {
        return scaling;
    }

    /**
     * Setter for property 'scaling'.
     *
     * @param scaling Value to set for property 'scaling'.
     */
    public void setScaling(double scaling) {
        this.scaling = scaling;
    }
}
