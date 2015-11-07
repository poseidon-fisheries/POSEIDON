package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.OsmoseBiologyInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.nio.file.Paths;

/**
 * Creates the OsmoseBiologyInitializer
 * Created by carrknight on 11/5/15.
 */
public class OsmoseBiologyFactory implements AlgorithmFactory<OsmoseBiologyInitializer>
{


    private int numberOfOsmoseStepsToPulseBeforeSimulationStart = 100;


    private String osmoseConfigurationFile = FishStateUtilities.getAbsolutePath(
            Paths.get("inputs", "osmose", "prototype", "osm_all-parameters.csv").toString());



    private boolean preInitializedConfiguration =true;

    private String preInitializedConfigurationDirectory =
            FishStateUtilities.getAbsolutePath(
                    Paths.get("inputs", "osmose", "prototype", "restart").toString()
            );





    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public OsmoseBiologyInitializer apply(FishState fishState) {
        return new OsmoseBiologyInitializer(osmoseConfigurationFile,
                                            preInitializedConfiguration,
                                            preInitializedConfigurationDirectory,
                                            numberOfOsmoseStepsToPulseBeforeSimulationStart);
    }


    public int getNumberOfOsmoseStepsToPulseBeforeSimulationStart() {
        return numberOfOsmoseStepsToPulseBeforeSimulationStart;
    }

    public void setNumberOfOsmoseStepsToPulseBeforeSimulationStart(int numberOfOsmoseStepsToPulseBeforeSimulationStart) {
        this.numberOfOsmoseStepsToPulseBeforeSimulationStart = numberOfOsmoseStepsToPulseBeforeSimulationStart;
    }

    public String getOsmoseConfigurationFile() {
        return osmoseConfigurationFile;
    }

    public void setOsmoseConfigurationFile(String osmoseConfigurationFile) {
        this.osmoseConfigurationFile = osmoseConfigurationFile;
    }

    public boolean isPreInitializedConfiguration() {
        return preInitializedConfiguration;
    }

    public void setPreInitializedConfiguration(boolean preInitializedConfiguration) {
        this.preInitializedConfiguration = preInitializedConfiguration;
    }

    public String getPreInitializedConfigurationDirectory() {
        return preInitializedConfigurationDirectory;
    }

    public void setPreInitializedConfigurationDirectory(String preInitializedConfigurationDirectory) {
        this.preInitializedConfigurationDirectory = preInitializedConfigurationDirectory;
    }
}
