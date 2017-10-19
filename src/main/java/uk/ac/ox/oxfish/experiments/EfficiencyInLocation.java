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

package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.geography.mapmakers.OsmoseMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.DailyDecreasingProbabilityFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.ExplorationPenaltyProbabilityFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.DoubleStream;

/**
 * how to choose locations
 * Created by carrknight on 9/1/15.
 */
public class EfficiencyInLocation
{


    private static final int YEARS_TO_SIMULATE = 5;
    private static final double imitationProbability = 0d;


    public static void main(String[] args) throws IOException {


        File file = Paths.get("runs", "destination").toFile();
        file.mkdirs();
        FileWriter writer = new FileWriter(Paths.get("runs", "destination", "non-imitation.csv").toFile());


        AlgorithmFactory[] probabilities = new AlgorithmFactory[3];
        probabilities[0] = new FixedProbabilityFactory(.8, imitationProbability);
        probabilities[1] = new DailyDecreasingProbabilityFactory();
        ((DailyDecreasingProbabilityFactory) probabilities[1]).setExplorationProbability(new FixedDoubleParameter(.8d));
        ((DailyDecreasingProbabilityFactory) probabilities[1]).setImitationProbability(new FixedDoubleParameter(
                imitationProbability));
        ((DailyDecreasingProbabilityFactory) probabilities[1]).setExplorationProbabilityMinimum(new FixedDoubleParameter(0.01d));
        probabilities[2] = new ExplorationPenaltyProbabilityFactory(.8, imitationProbability,.02,0.01);


        AlgorithmFactory[] biologies = new AlgorithmFactory[2];
        biologies[0] = new FromLeftToRightFactory();
        biologies[1] = new DiffusingLogisticFactory();



        for(@SuppressWarnings   ("unchecked")
        AlgorithmFactory<? extends AdaptationProbability> probability : probabilities)
        {
            for(@SuppressWarnings("unchecked")
            AlgorithmFactory<? extends BiologyInitializer> biology : biologies)
            {
                PrototypeScenario scenario = new PrototypeScenario();
                scenario.setFishers(100);
                scenario.setBiologyInitializer(biology);

                PerTripImitativeDestinationFactory imitative = new PerTripImitativeDestinationFactory();
                imitative.setProbability(probability);
                scenario.setDestinationStrategy(imitative);

                FishState state = new FishState(0,1);
                state.setScenario(scenario);

                state.start();
                while(state.getYear() < YEARS_TO_SIMULATE)
                {
                    state.schedule.step(state);
                }
                state.schedule.step(state);

                //grab all catches and all fuel consumption and compute the ratio
                double catches = state.getYearlyDataSet().
                        getColumn(state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).
                        stream().reduce(0.0,Double::sum);

                double fuel = state.getYearlyDataSet().
                        getColumn(FisherYearlyTimeSeries.FUEL_CONSUMPTION).
                        stream().reduce(0.0,Double::sum);

                writer.write(probability.getClass().getSimpleName() +
                                     "," +
                                     biology.getClass().getSimpleName() + "," + catches + "\n");
                writer.flush();



            }

        }




        //now try OSMOSE


        for(@SuppressWarnings("unchecked")
        AlgorithmFactory<? extends AdaptationProbability> probability : probabilities)
        {


            //unfortunately OSMOSE has no easy way of setting a random seed. This means we have to average out over multiple runs!
            double[] catches = new double[10];
            double[] fuels = new double[10];

            for(int i=0; i<10; i++) {
                PrototypeScenario scenario = new PrototypeScenario();
                OsmoseBiologyFactory biology = new OsmoseBiologyFactory();
                biology.setPreInitializedConfiguration(true);
                scenario.setBiologyInitializer(biology);
                scenario.setMapInitializer(new OsmoseMapInitializerFactory());
                scenario.setFishers(100);

                PerTripImitativeDestinationFactory imitative = new PerTripImitativeDestinationFactory();
                imitative.setProbability(probability);
                scenario.setDestinationStrategy(imitative);


                FishState state = new FishState(0, 1);
                state.setScenario(scenario);

                state.start();
                while (state.getYear() < YEARS_TO_SIMULATE) {
                    state.schedule.step(state);
                }
                state.schedule.step(state);

                //grab all catches and all fuel consumption and compute the ratio
                catches[i] = state.getYearlyDataSet().
                        getColumn(state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME).
                        stream().reduce(0.0, Double::sum);

                fuels[i] = state.getYearlyDataSet().
                        getColumn(FisherYearlyTimeSeries.FUEL_CONSUMPTION).
                        stream().reduce(0.0, Double::sum);

            }
            writer.write(probability.getClass().getSimpleName() +
                                 "," +
                                 "Osmose" + "," + DoubleStream.of(catches).sum()/10 + "\n");
            writer.flush();






        }


        writer.close();
    }


    private EfficiencyInLocation() {
    }
}
