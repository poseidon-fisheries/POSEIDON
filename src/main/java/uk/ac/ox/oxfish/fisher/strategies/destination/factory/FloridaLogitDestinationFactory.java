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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.*;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.OsmoseWFSScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.CsvColumnsToLists;
import uk.ac.ox.oxfish.utility.Locker;

import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Steve saul's stuff, initialized here.
 * Created by carrknight on 12/6/16.
 */
public class FloridaLogitDestinationFactory implements
        AlgorithmFactory<LogitDestinationStrategy> {


    /**
     * file containing all the betas
     */
    private String coefficientsFile =

                    Paths.get("temp_wfs", "longline.csv").toString();


    private String coefficientsStandardDeviationFile =
                    Paths.get("temp_wfs", "longline_dummy.csv").toString();

    /**
     * file containing all the centroids
     */
    private String centroidFile =
                    Paths.get("temp_wfs", "areas.txt").toString();


    //



    /**
     * everybody shares the parent same destination logit strategy
     */
    private Locker<String,MapDiscretization> discretizationLocker = new Locker<>();

    private boolean automaticallyAvoidMPA = true;

    private boolean automaticallyAvoidWastelands = true;



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public LogitDestinationStrategy apply(FishState state) {

        MapDiscretization discretization = discretizationLocker.
                presentKey(state.getHopefullyUniqueID(), () -> OsmoseWFSScenario.createDiscretization(state, centroidFile)
                );

        CsvColumnsToLists reader = new CsvColumnsToLists(
                coefficientsFile,
                ',',
                new String[]{"area", "intercept", "distance", "habit", "fuel_price", "wind_speed"}
        );
        LinkedList<Double>[] lists = reader.readColumns();
        reader = new CsvColumnsToLists(
                coefficientsStandardDeviationFile,
                ',',
                new String[]{"area", "intercept", "distance", "habit", "fuel_price", "wind_speed"}
        );
        LinkedList<Double>[] std =  reader.readColumns();
        LinkedList<Integer> rowNames = new LinkedList<Integer>();
        double[][] betas = new double[lists[0].size()][];
        for (int i = 0; i < lists[0].size(); i++) {
            //record which site this belongs to
            rowNames.add(lists[0].get(i).intValue());
            //record its coordinates
            betas[i] = new double[lists.length - 1];
            for (int j = 1; j < lists.length; j++)
                betas[i][j - 1] = lists[j].get(i) + state.getRandom().nextGaussian() * std[j].get(i) ;

        }
        ObservationExtractor[][] extractors = new ObservationExtractor[betas.length][];
        ObservationExtractor[] commonExtractor =
                    longlineFloridaCommonExtractor(discretization);
        for (int i = 0; i < extractors.length; i++)
            extractors[i] = commonExtractor;

        return new LogitDestinationStrategy(betas, extractors, rowNames, discretization,
                                            new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
                                            state.getRandom(), automaticallyAvoidMPA, automaticallyAvoidWastelands);

    }

    public static ObservationExtractor[] longlineFloridaCommonExtractor(
            MapDiscretization discretization) {
        return new ObservationExtractor[]{
                //intercept
                new InterceptExtractor(),
                //distance
                new PortDistanceExtractor(),
                //habit
                new PeriodHabitBooleanExtractor(discretization, 90),
                //fuel_price TODO: adjust from gallon
                new GasPriceExtractor(),
                //wind_speed TODO: adjust from mph
                new WindSpeedExtractor()
        };
    }

    public String getCoefficientsFile() {
        return coefficientsFile;
    }

    public void setCoefficientsFile(String coefficientsFile) {
        this.coefficientsFile = coefficientsFile;
    }

    public String getCoefficientsStandardDeviationFile() {
        return coefficientsStandardDeviationFile;
    }

    public void setCoefficientsStandardDeviationFile(String coefficientsStandardDeviationFile) {
        this.coefficientsStandardDeviationFile = coefficientsStandardDeviationFile;
    }

    public String getCentroidFile() {
        return centroidFile;
    }

    public void setCentroidFile(String centroidFile) {
        this.centroidFile = centroidFile;
    }

    /**
     * Getter for property 'automaticallyAvoidMPA'.
     *
     * @return Value for property 'automaticallyAvoidMPA'.
     */
    public boolean isAutomaticallyAvoidMPA() {
        return automaticallyAvoidMPA;
    }

    /**
     * Setter for property 'automaticallyAvoidMPA'.
     *
     * @param automaticallyAvoidMPA Value to set for property 'automaticallyAvoidMPA'.
     */
    public void setAutomaticallyAvoidMPA(boolean automaticallyAvoidMPA) {
        this.automaticallyAvoidMPA = automaticallyAvoidMPA;
    }

    /**
     * Getter for property 'automaticallyAvoidWastelands'.
     *
     * @return Value for property 'automaticallyAvoidWastelands'.
     */
    public boolean isAutomaticallyAvoidWastelands() {
        return automaticallyAvoidWastelands;
    }

    /**
     * Setter for property 'automaticallyAvoidWastelands'.
     *
     * @param automaticallyAvoidWastelands Value to set for property 'automaticallyAvoidWastelands'.
     */
    public void setAutomaticallyAvoidWastelands(boolean automaticallyAvoidWastelands) {
        this.automaticallyAvoidWastelands = automaticallyAvoidWastelands;
    }
}
