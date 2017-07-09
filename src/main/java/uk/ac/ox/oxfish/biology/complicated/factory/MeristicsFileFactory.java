package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.MeristicsInput;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads meristics from file and puts them in the stock assessment.
 * Created by carrknight on 7/7/17.
 */
public class MeristicsFileFactory
        implements AlgorithmFactory<StockAssessmentCaliforniaMeristics> {



    private Path pathToMeristicFile = Paths.get("inputs",
                                                "california",
                                                "biology",
                                                "Sablefish",
                                                "meristics.yaml");


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
            MeristicsInput input = yaml.loadAs(new FileReader(
                    pathToMeristicFile.toFile()
            ), MeristicsInput.class);
            return new StockAssessmentCaliforniaMeristics(input);

        } catch (FileNotFoundException e) {
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
