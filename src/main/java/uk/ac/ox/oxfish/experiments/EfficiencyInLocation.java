package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.scenario.OsmosePrototype;
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

/**
 * how to choose locations
 * Created by carrknight on 9/1/15.
 */
public class EfficiencyInLocation
{


    private static final int YEARS_TO_SIMULATE = 10;



    public static void main(String[] args) throws IOException {


        File file = Paths.get("runs", "destination").toFile();
        file.mkdirs();
        FileWriter writer = new FileWriter(Paths.get("runs", "destination", "results.csv").toFile());


        AlgorithmFactory[] probabilities = new AlgorithmFactory[3];
        probabilities[0] = new FixedProbabilityFactory(.8,1d);
        probabilities[1] = new DailyDecreasingProbabilityFactory();
        ((DailyDecreasingProbabilityFactory) probabilities[1]).setExplorationProbability(new FixedDoubleParameter(.8d));
        ((DailyDecreasingProbabilityFactory) probabilities[1]).setImitationProbability(new FixedDoubleParameter(1d));
        ((DailyDecreasingProbabilityFactory) probabilities[1]).setExplorationProbabilityMinimum(new FixedDoubleParameter(0.01d));
        probabilities[2] = new ExplorationPenaltyProbabilityFactory(.8,1d,.02,.01);


        AlgorithmFactory[] biologies = new AlgorithmFactory[2];
        biologies[0] = new FromLeftToRightFactory();
        biologies[1] = new DiffusingLogisticFactory();



        for(@SuppressWarnings("unchecked")
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
                        getColumn(YearlyFisherTimeSeries.FUEL_CONSUMPTION).
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

            OsmosePrototype scenario = new OsmosePrototype();
            scenario.setPreInitializedConfiguration(true);
            scenario.setFishers(100);

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
                    getColumn(YearlyFisherTimeSeries.FUEL_CONSUMPTION).
                    stream().reduce(0.0,Double::sum);

            writer.write(probability.getClass().getSimpleName() +
                                 "," +
                                 "Osmose" + "," + catches + "\n");
            writer.flush();






        }


        writer.close();
    }


    private EfficiencyInLocation() {
    }
}
