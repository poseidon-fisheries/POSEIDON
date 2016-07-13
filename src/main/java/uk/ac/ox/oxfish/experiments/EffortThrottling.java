package uk.ac.ox.oxfish.experiments;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedProbabilityDepartingFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.factory.CongestedMarketFactory;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A few eamples to show how agents modify their effort
 * Created by carrknight on 8/13/15.
 */
public class EffortThrottling {


    public static void main(String[] args)
    {

        Path root = Paths.get("runs", "effort");
        root.toFile().mkdirs();

        //low effort
        FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(2.0));

        EffortThrottling.effortThrottling(40, market,
                                          0,
                                          new UniformDoubleParameter(0.001, 1), root.resolve("low_effort.csv").toFile(),
                                          root.resolve("low_grid.csv").toFile());



        //high effort
        market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(10.0));

        EffortThrottling.effortThrottling(40, market,
                                          0,
                                          new UniformDoubleParameter(0.001, 1), root.resolve("high_effort.csv").toFile(),
                                          root.resolve("high_grid.csv").toFile());

        //self-regulating effort
        CongestedMarketFactory market2 = new CongestedMarketFactory();
        market2.setAcceptableBiomassThreshold(new FixedDoubleParameter(0));//no weird intercept
        market2.setDailyConsumption(new FixedDoubleParameter(8000000)); //no long term effect to demand
        market2.setDemandSlope(new FixedDoubleParameter(0.001));
        market2.setMaxPrice(new FixedDoubleParameter(10.0));

        FishState state = EffortThrottling.effortThrottling(80, market2,
                                                            0,
                                                            new UniformDoubleParameter(0.001, 1),
                                                            root.resolve("variable_effort.csv").toFile(),
                                                            root.resolve("variable_grid.csv").toFile()
        );

        //print out the price!
        FishStateUtilities.printCSVColumnToFile(
                root.resolve("variable_price.csv").toFile(),
                state.getPorts().iterator().next().getDefaultMarketMap().getMarket(state.getBiology().getSpecie(0)).getData().getColumn(
                        AbstractMarket.PRICE_COLUMN_NAME
                ));


        //self-regulating from below
        state = EffortThrottling.effortThrottling(80, market2,
                                                            0,
                                                            new UniformDoubleParameter(0.001, 0.1),
                                                            root.resolve("variable_effort2.csv").toFile(),
                                                            root.resolve("variable_grid2.csv").toFile()
        );

        Preconditions.checkArgument(state.seed()==0);

        FishStateUtilities.printCSVColumnToFile(
                root.resolve("variable_price2.csv").toFile(),
                state.getPorts().iterator().next().getDefaultMarketMap().getMarket(state.getBiology().getSpecie(0)).getData().getColumn(
                        AbstractMarket.PRICE_COLUMN_NAME
                ));
        //self-regulating from above
        state = EffortThrottling.effortThrottling(80, market2,
                                                  0,
                                                  new UniformDoubleParameter(0.8, 1),
                                                  root.resolve("variable_effort3.csv").toFile(),
                                                  root.resolve("variable_grid3.csv").toFile()
        );

        FishStateUtilities.printCSVColumnToFile(
                root.resolve("variable_price3.csv").toFile(),
                state.getPorts().iterator().next().getDefaultMarketMap().getMarket(state.getBiology().getSpecie(0)).getData().getColumn(
                        AbstractMarket.PRICE_COLUMN_NAME
                ));



    }


    public static FishState effortThrottling(
            final int simulationYears, final AlgorithmFactory<? extends Market> market, final long seed,
            final UniformDoubleParameter initialEffortProbability, File timeSeries, File grid)
    {


        //create the scenario
        PrototypeScenario scenario = new PrototypeScenario();
        //set the biology right
        FromLeftToRightFactory biologyInitializer = new FromLeftToRightFactory();
        biologyInitializer.setMaximumBiomass(new FixedDoubleParameter(1000d));
        scenario.setBiologyInitializer(biologyInitializer);

        //change getDistances
        SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCellSizeInKilometers(new FixedDoubleParameter(1d));
        scenario.setMapInitializer(simpleMap);
        scenario.setGasPricePerLiter(new FixedDoubleParameter(0.5));

        //set very low price
        scenario.setMarket(market);

        //set initially random chance of going out
        FixedProbabilityDepartingFactory departingStrategy = new FixedProbabilityDepartingFactory();
        departingStrategy.setProbabilityToLeavePort(initialEffortProbability);
        scenario.setDepartingStrategy(departingStrategy);

        //start it
        FishState state = new FishState(seed);
        state.setScenario(scenario);
        state.start();
        GearImitationAnalysis.attachGoingOutProbabilityToEveryone(state.getFishers(), state, 0.4, .2, .6);

   //     state.getMap().guiStart(state);
        state.schedule.step(state);
        System.out.println("start: " + state.getDailyDataSet().getLatestObservation("Probability to leave port"));


        //run it for 40 years
        while(state.getYear() < simulationYears)
            state.schedule.step(state);

        //probability should be very low!
        System.out.println("end: " + state.getDailyDataSet().getLatestObservation("Probability to leave port"));

        if(timeSeries != null)
            FishStateUtilities.printCSVColumnToFile(timeSeries,
                                                    state.getDailyDataSet().getColumn("Probability to leave port")
            );
        if(grid!=null)
            gridToCSV(state.getDailyTrawlsMap().field,grid);


        return state;
    }


    public static void gridToCSV(int[][] grid, File file)
    {

        try {
            FileWriter writer = new FileWriter(file);
            writer.write("x,y,value");
            writer.write("\n");
            for(int x = 0 ; x<grid.length; x++)
                for(int y=0; y<grid[x].length; y++)
                {
                    writer.write(x + "," + y +"," + grid[x][y]);
                    writer.write("\n");

                }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

