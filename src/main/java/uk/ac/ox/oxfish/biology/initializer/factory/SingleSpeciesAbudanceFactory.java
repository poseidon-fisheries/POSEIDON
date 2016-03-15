package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Paths;

/**
 * The factory that generates the single species abundance biology.
 * Created by carrknight on 3/11/16.
 */
public class SingleSpeciesAbudanceFactory implements AlgorithmFactory<SingleSpeciesAbundanceInitializer>
{


    private String folderPath = "inputs/california/biology/Dover Sole/";

    private String speciesName = "Dover Sole";

    private double scaling = 0.1;


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
                scaling
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
