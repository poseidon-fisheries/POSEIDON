package uk.ac.ox.oxfish.biology.complicated.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.complicated.InitialAbundance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * Reads up a csv file containing male and female columns (+ a header) and use them as initial abundance
 * Created by carrknight on 7/8/17.
 */
public class InitialAbundanceFromFileFactory implements AlgorithmFactory<InitialAbundance>
{
    private Path pathToCsvFile = Paths.get("inputs",
                                           "california",
                                           "biology",
                                           "Sablefish",
                                           "count.csv");


    public InitialAbundanceFromFileFactory() {
    }


    public InitialAbundanceFromFileFactory(Path pathToCsvFile) {
        this.pathToCsvFile = pathToCsvFile;
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public InitialAbundance apply(FishState state) {
        try {

            List<String> countfile = null;
            countfile = Files.readAllLines(pathToCsvFile);
            //remove empty lines
            countfile.removeAll(Collections.singleton(null));
            countfile.removeAll(Collections.singleton(""));
            int maxAge = countfile.size()-2; //-1 because of the header and -1 because you start counting from 0
            //allow for one line of empty space
            String[] titleLine = countfile.get(0).split(",");
            //expect to be female and then male
            Preconditions.checkArgument(titleLine.length == 2);
            Preconditions.checkArgument(titleLine[0].trim().toLowerCase().equals("female"));
            Preconditions.checkArgument(titleLine[1].trim().toLowerCase().equals("male"));

            int[][] totalCount = new int[2][];
            totalCount[0] = new int[maxAge+1];
            totalCount[1] = new int[maxAge+1];
            for(int i=1;i<maxAge +2; i++)
            {
                String[] line = countfile.get(i).split(","); //this is not very efficient but it's a 100 lines at most so no big deal
                assert  line.length == 2;
                totalCount[FishStateUtilities.FEMALE][i-1] = Integer.parseInt(line[0]);
                totalCount[FishStateUtilities.MALE][i-1] = Integer.parseInt(line[1]);
            }

            return new InitialAbundance(totalCount);
        } catch (IOException e) {
            e.printStackTrace();
            throw  new RuntimeException(" failed to read a count file at " + pathToCsvFile.toString());
        }
    }

    /**
     * Getter for property 'pathToFile'.
     *
     * @return Value for property 'pathToFile'.
     */
    public Path getPathToCsvFile() {
        return pathToCsvFile;
    }

    /**
     * Setter for property 'pathToFile'.
     *
     * @param pathToCsvFile Value to set for property 'pathToFile'.
     */
    public void setPathToCsvFile(Path pathToCsvFile) {
        this.pathToCsvFile = pathToCsvFile;
    }
}
