package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.OsmoseBiologyInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

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


    private String indexOfSpeciesToBeManagedByThisModel = "0,1,2,3,4,5,6,7,8,9";


    private DoubleParameter scalingFactor = new FixedDoubleParameter(1d);



    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public OsmoseBiologyInitializer apply(FishState fishState) {
        String[] split = indexOfSpeciesToBeManagedByThisModel.trim().split(",");
        Integer[] parsed;

        if(split.length > 0 && split[0].length()>0) {
            parsed = new Integer[split.length];

            for (int i = 0; i < split.length; i++)
                parsed[i] = Integer.parseInt(split[i]);
        }
        else
            parsed = new Integer[0];
        return new OsmoseBiologyInitializer(osmoseConfigurationFile,
                                            preInitializedConfiguration,
                                            preInitializedConfigurationDirectory,
                                            numberOfOsmoseStepsToPulseBeforeSimulationStart,
                                            scalingFactor.apply(fishState.getRandom()),
                                            parsed);
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

    /**
     * Getter for property 'indexOfSpeciesToBeManagedByThisModel'.
     *
     * @return Value for property 'indexOfSpeciesToBeManagedByThisModel'.
     */
    public String getIndexOfSpeciesToBeManagedByThisModel() {
        return indexOfSpeciesToBeManagedByThisModel;
    }

    /**
     * Setter for property 'indexOfSpeciesToBeManagedByThisModel'.
     *
     * @param indexOfSpeciesToBeManagedByThisModel Value to set for property 'indexOfSpeciesToBeManagedByThisModel'.
     */
    public void setIndexOfSpeciesToBeManagedByThisModel(String indexOfSpeciesToBeManagedByThisModel) {
        this.indexOfSpeciesToBeManagedByThisModel = indexOfSpeciesToBeManagedByThisModel;
    }

    /**
     * Getter for property 'scalingFactor'.
     *
     * @return Value for property 'scalingFactor'.
     */
    public DoubleParameter getScalingFactor() {
        return scalingFactor;
    }

    /**
     * Setter for property 'scalingFactor'.
     *
     * @param scalingFactor Value to set for property 'scalingFactor'.
     */
    public void setScalingFactor(DoubleParameter scalingFactor) {
        this.scalingFactor = scalingFactor;
    }
}
