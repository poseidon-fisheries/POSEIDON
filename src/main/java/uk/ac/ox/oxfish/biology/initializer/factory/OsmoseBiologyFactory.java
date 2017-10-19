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

import uk.ac.ox.oxfish.biology.initializer.OsmoseBiologyInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Creates the OsmoseBiologyInitializer
 * Created by carrknight on 11/5/15.
 */
public class OsmoseBiologyFactory implements AlgorithmFactory<OsmoseBiologyInitializer>
{


    private int numberOfOsmoseStepsToPulseBeforeSimulationStart = 100;


    private String osmoseConfigurationFile =
            Paths.get("inputs", "osmose", "prototype", "osm_all-parameters.csv").toString();



    private boolean preInitializedConfiguration =true;

    private String preInitializedConfigurationDirectory =

                    Paths.get("inputs", "osmose", "prototype", "restart").toString();


    private String indexOfSpeciesToBeManagedByThisModel = "0,1,2,3,4,5,6,7,8,9";


    private DoubleParameter scalingFactor = new FixedDoubleParameter(1d);

    private HashMap<Integer, Integer> recruitmentAges = new HashMap<>();

    private HashMap<Integer,Double> discardMortalityRate = new HashMap<>();


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
                                            recruitmentAges,
                                            discardMortalityRate,
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


    /**
     * Getter for property 'recruitmentAges'.
     *
     * @return Value for property 'recruitmentAges'.
     */
    public HashMap<Integer, Integer> getRecruitmentAges() {
        return recruitmentAges;
    }

    /**
     * Setter for property 'recruitmentAges'.
     *
     * @param recruitmentAges Value to set for property 'recruitmentAges'.
     */
    public void setRecruitmentAges(HashMap<Integer, Integer> recruitmentAges) {
        this.recruitmentAges = recruitmentAges;
    }

    /**
     * Getter for property 'discardMortalityRate'.
     *
     * @return Value for property 'discardMortalityRate'.
     */
    public HashMap<Integer, Double> getDiscardMortalityRate() {
        return discardMortalityRate;
    }

    /**
     * Setter for property 'discardMortalityRate'.
     *
     * @param discardMortalityRate Value to set for property 'discardMortalityRate'.
     */
    public void setDiscardMortalityRate(HashMap<Integer, Double> discardMortalityRate) {
        this.discardMortalityRate = discardMortalityRate;
    }
}

