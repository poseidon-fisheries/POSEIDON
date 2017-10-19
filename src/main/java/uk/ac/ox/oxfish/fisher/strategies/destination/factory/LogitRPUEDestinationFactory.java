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

import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.SimulatedHourlyProfitExtractor;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.IdentityDiscretizerFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * Logit destination strategy using RPUE - travel costs with full knowledge
 * Created by carrknight on 2/6/17.
 */
public class LogitRPUEDestinationFactory implements AlgorithmFactory<LogitDestinationStrategy>
{



    /**
     * everybody shares the parent same destination logit strategy
     */
    private Locker<FishState,MapDiscretization> discretizationLocker = new Locker<>();


    private AlgorithmFactory<? extends MapDiscretizer> discretizer = new IdentityDiscretizerFactory();


    private DoubleParameter profitBeta = new FixedDoubleParameter(1d);
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
                presentKey(
                        state,
                        new Supplier<MapDiscretization>()
                        {
                            @Override
                            public MapDiscretization get() {

                                MapDiscretizer mapDiscretizer = discretizer.apply(state);
                                MapDiscretization toReturn = new MapDiscretization(mapDiscretizer);
                                toReturn.discretize(state.getMap());
                                return toReturn;
                            }
                        }
                );


        //betas are just +1 for revenue and -1 for gas costs
        int numberOfGroups = discretization.getNumberOfGroups();
        double[][] betas = new double[numberOfGroups][1];

        for(int i=0; i<numberOfGroups; i++)
        {
            betas[i][0] = profitBeta.apply(state.getRandom());;

        }

        //use trip simulator (poorly) to simulate trips so you can figure out what revenues and costs are
        //0: revenue
        //1: gas costs
        ObservationExtractor[][] extractors = buildRPUEExtractors(numberOfGroups);


        //"names" are one to one
        LinkedList<Integer> rowNames = new LinkedList<>();
        for(int i=0; i<numberOfGroups; i++)
            rowNames.add(i);

        return
                new LogitDestinationStrategy(
                        betas,
                        extractors,
                        rowNames,
                        discretization,
                        new FavoriteDestinationStrategy(state.getMap(), state.getRandom()),
                        state.getRandom(), automaticallyAvoidMPA, automaticallyAvoidWastelands);


    }

    private ObservationExtractor[][] buildRPUEExtractors(int numberOfGroups) {
        ObservationExtractor[] commonExtractor = new ObservationExtractor[1];
        commonExtractor[0] = new SimulatedHourlyProfitExtractor(5*24d);
        ObservationExtractor[][] extractors = new ObservationExtractor[numberOfGroups][];
        for(int i=0; i<numberOfGroups; i++)
            extractors[i] = commonExtractor;
        return extractors;
    }


    /**
     * Getter for property 'discretizer'.
     *
     * @return Value for property 'discretizer'.
     */
    public AlgorithmFactory<? extends MapDiscretizer> getDiscretizer() {
        return discretizer;
    }

    /**
     * Setter for property 'discretizer'.
     *
     * @param discretizer Value to set for property 'discretizer'.
     */
    public void setDiscretizer(
            AlgorithmFactory<? extends MapDiscretizer> discretizer) {
        this.discretizer = discretizer;
    }


    /**
     * Getter for property 'profitBeta'.
     *
     * @return Value for property 'profitBeta'.
     */
    public DoubleParameter getProfitBeta() {
        return profitBeta;
    }

    /**
     * Setter for property 'profitBeta'.
     *
     * @param profitBeta Value to set for property 'profitBeta'.
     */
    public void setProfitBeta(DoubleParameter profitBeta) {
        this.profitBeta = profitBeta;
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
