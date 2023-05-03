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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NearestNeighborRegressionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.NearestNeighborRegression;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SmootherFileAllocatorFactory implements FileBiomassAllocatorFactory
{


    private Path biomassPath = Paths.get("inputs", "tests", "fake_indo_abundance.csv");

    private boolean inputFileHasHeader = true;

    private  AlgorithmFactory<
        ? extends GeographicalRegression<Double>> smoother = new NearestNeighborRegressionFactory();
    {
        ((NearestNeighborRegressionFactory) smoother).setNeighbors(new FixedDoubleParameter(1));
    }



    @Override
    public SmoothFileBiomassAllocator apply(FishState fishState) {
        return new SmoothFileBiomassAllocator(biomassPath,inputFileHasHeader,
                                              smoother.apply(fishState)
        );
    }

    /**
     * Getter for property 'biomassPath'.
     *
     * @return Value for property 'biomassPath'.
     */
    public Path getBiomassPath() {
        return biomassPath;
    }

    /**
     * Setter for property 'biomassPath'.
     *
     * @param biomassPath Value to set for property 'biomassPath'.
     */
    public void setBiomassPath(Path biomassPath) {
        this.biomassPath = biomassPath;
    }

    /**
     * Getter for property 'inputFileHasHeader'.
     *
     * @return Value for property 'inputFileHasHeader'.
     */
    public boolean isInputFileHasHeader() {
        return inputFileHasHeader;
    }

    /**
     * Setter for property 'inputFileHasHeader'.
     *
     * @param inputFileHasHeader Value to set for property 'inputFileHasHeader'.
     */
    public void setInputFileHasHeader(boolean inputFileHasHeader) {
        this.inputFileHasHeader = inputFileHasHeader;
    }

    /**
     * Getter for property 'smoother'.
     *
     * @return Value for property 'smoother'.
     */
    public AlgorithmFactory<? extends GeographicalRegression<Double>> getSmoother() {
        return smoother;
    }

    /**
     * Setter for property 'smoother'.
     *
     * @param smoother Value to set for property 'smoother'.
     */
    public void setSmoother(
            AlgorithmFactory<? extends GeographicalRegression<Double>> smoother) {
        this.smoother = smoother;
    }
}
