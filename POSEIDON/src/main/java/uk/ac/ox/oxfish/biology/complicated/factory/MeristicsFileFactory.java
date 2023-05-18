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

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads meristics from file and puts them in the stock assessment.
 * Created by carrknight on 7/7/17.
 */
public class MeristicsFileFactory
    implements AlgorithmFactory<StockAssessmentCaliforniaMeristics> {


    private Path pathToMeristicFile = Paths.get(
        "inputs",
        "california",
        "biology",
        "Sablefish",
        "meristics.yaml"
    );


    public MeristicsFileFactory(Path pathToMeristicFile) {
        this.pathToMeristicFile = pathToMeristicFile;
    }


    public MeristicsFileFactory() {
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public StockAssessmentCaliforniaMeristics apply(FishState fishState) {

        FishYAML yaml = new FishYAML();
        try {
            FileReader io = new FileReader(
                pathToMeristicFile.toFile()
            );
            MeristicsInput input = yaml.loadAs(io, MeristicsInput.class);
            io.close();
            return new StockAssessmentCaliforniaMeristics(input);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("can't find the meristics file");
        }


    }

    /**
     * Getter for property 'path'.
     *
     * @return Value for property 'path'.
     */
    public Path getPathToMeristicFile() {
        return pathToMeristicFile;
    }

    /**
     * Setter for property 'path'.
     *
     * @param pathToMeristicFile Value to set for property 'path'.
     */
    public void setPathToMeristicFile(Path pathToMeristicFile) {
        this.pathToMeristicFile = pathToMeristicFile;
    }
}
