package uk.ac.ox.oxfish.experiments.dedicated.habitat;


import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.HalfBycatchFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMultiFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PolicyAndLocations
{



    public static String gridToCSV(double[][] grid)
    {

        StringBuilder buffer = new StringBuilder();
        for(int x=0; x<grid.length; x++)
        {
            for(int y=0; y<grid[x].length; y++)
            {
                buffer.append(grid[x][y]);
                if(y<grid[x].length-1)
                    buffer.append(",");
            }
            buffer.append("\n");
        }

        return buffer.toString();

    }


    //one dimensional TAC
    public static void main(String[] args) throws IOException {


        Path mainDirectory = Paths.get("runs", "analysis");
        mainDirectory.toFile().mkdirs();


        /***
         *       ___             ___              _
         *      / _ \ _ _  ___  / __|_ __  ___ __(_)___ ___
         *     | (_) | ' \/ -_) \__ \ '_ \/ -_) _| / -_|_-<
         *      \___/|_||_\___| |___/ .__/\___\__|_\___/__/
         *                          |_|
         */

        FileWriter writer = new FileWriter(mainDirectory.resolve("tac1.csv").toFile());
        TACMultiFactory tacRegulation = new TACMultiFactory();
        tacRegulation.setFirstSpeciesQuota(new FixedDoubleParameter(400000));
        tacRegulation.setOtherSpeciesQuota(new FixedDoubleParameter(100000)); //80-20 proportion (used in the two species world)
        ExperimentResult simulation = policyLocationGrid(new FromLeftToRightFactory(),
                                                         tacRegulation,
                                                         0l,
                                                         5,
                                                         null);
        writer.write(simulation.getCumulativeTrawls()); writer.close();



        writer = new FileWriter(mainDirectory.resolve("itq1.csv").toFile());
        MultiITQFactory multiITQFactory = new MultiITQFactory();
        multiITQFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4000));
        multiITQFactory.setQuotaOtherSpecies(new FixedDoubleParameter(1000)); //80-20 proportion (used in the two species world)
        simulation = policyLocationGrid(new FromLeftToRightFactory(),
                                        multiITQFactory,
                                        0l,
                                        5, null);
        writer.write(simulation.getCumulativeTrawls()); writer.close();



        writer = new FileWriter(mainDirectory.resolve("free1.csv").toFile());
        simulation = policyLocationGrid(new FromLeftToRightFactory(),
                                        new AnarchyFactory(),
                                        0l,
                                        5, null);
        writer.write(simulation.getCumulativeTrawls()); writer.close();


        /***
         *      _____              ___              _
         *     |_   _|_ __ _____  / __|_ __  ___ __(_)___ ___
         *       | | \ V  V / _ \ \__ \ '_ \/ -_) _| / -_|_-<
         *       |_|  \_/\_/\___/ |___/ .__/\___\__|_\___/__/
         *                            |_|
         */
        HalfBycatchFactory splitBiology = new HalfBycatchFactory();
        splitBiology.setCarryingCapacity(new FixedDoubleParameter(5000d));
        FileWriter efficiency = new FileWriter(mainDirectory.resolve("efficiency.csv").toFile());


        writer = new FileWriter(mainDirectory.resolve("tac2.csv").toFile());
        simulation = policyLocationGrid(splitBiology,
                                        tacRegulation,
                                        0l,
                                        5,
                                        mainDirectory.resolve("tac2_hist.csv").toFile());
        writer.write(simulation.getCumulativeTrawls()); writer.close();

        efficiencyWrite(simulation, efficiency, "tac");


        writer = new FileWriter(mainDirectory.resolve("itq2.csv").toFile());
        simulation = policyLocationGrid(splitBiology,
                                        multiITQFactory,
                                        0l,
                                        5,
                                        mainDirectory.resolve("itq2_hist.csv").toFile());
        writer.write(simulation.getCumulativeTrawls()); writer.close();


        efficiencyWrite(simulation, efficiency, "itq");


        writer = new FileWriter(mainDirectory.resolve("free2.csv").toFile());
        simulation = policyLocationGrid(splitBiology,
                                        new AnarchyFactory(),
                                        0l,
                                        5, mainDirectory.resolve("free2_hist.csv").toFile());
        writer.write(simulation.getCumulativeTrawls()); writer.close();
        efficiencyWrite(simulation, efficiency, "free");

        efficiency.flush();
        efficiency.close();

    }

    private static void efficiencyWrite(
            ExperimentResult simulation, FileWriter efficiency, final String policy) throws IOException {

        efficiency.write(
                        policy + "," +
                        simulation.getModel().
                                getYearlyDataSet().getColumn(simulation.getModel().getSpecies().get(0) +
                                                                   " " +
                                                                   AbstractMarket.LANDINGS_COLUMN_NAME).stream().reduce(0d, (a, b) -> a+b)  +
                                ", " +
                                simulation.getModel().getYearlyDataSet().
                                        getColumn(simulation.getModel().getSpecies().get(1) +
                                                          " " +
                                                          AbstractMarket.LANDINGS_COLUMN_NAME).stream().reduce(0d,
                                                                                                               (a, b) -> a+b)   +
                                ", " +
                                simulation.getModel().getYearlyDataSet().
                                        getColumn(YearlyFisherTimeSeries.FUEL_CONSUMPTION).stream().reduce(0d,
                                                                                                           (a, b) -> a +b)
                                + "\n") ;
    }


    private static ExperimentResult policyLocationGrid(
            final AlgorithmFactory<? extends BiologyInitializer> biologyInitializer,
            final AlgorithmFactory<? extends Regulation> regulation,
            long seed,
            int burnIn,
            File histogramFile)
    {

        final FishState state = new FishState(seed);


        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setMapMakerDedicatedRandomSeed(seed);
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyInitializer);
        SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCellSizeInKilometers(new FixedDoubleParameter(2d));
        simpleMap.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setMapInitializer(simpleMap);
        scenario.forcePortPosition(new int[]{40,25});
        scenario.setRegulation(regulation);
        scenario.setUsePredictors(true);


        state.start();
        double[][] theGrid = new double[state.getMap().getWidth()][state.getMap().getHeight()];

        while(state.getYear()<burnIn)
        {
            state.schedule.step(state);
            IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
            for(int x =0; x<state.getMap().getWidth(); x++)
            {
                for (int y = 0; y < state.getMap().getHeight(); y++)
                {
                    theGrid[x][state.getMap().getHeight()-y-1] += trawls.get(x, y);
                }
            }
        }


        if(histogramFile != null)
            FishStateUtilities.pollHistogramToFile(
                    state.getFishers(), histogramFile,
                    fisher -> fisher.getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_FLOW_COLUMN)
            );

        return new ExperimentResult(state,gridToCSV(theGrid));

    }


    public static class ExperimentResult
    {

        private final FishState model;

        private final String cumulativeTrawls;

        public ExperimentResult(FishState model, String cumulativeTrawls) {
            this.model = model;
            this.cumulativeTrawls = cumulativeTrawls;
        }

        public FishState getModel() {
            return model;
        }

        public String getCumulativeTrawls() {
            return cumulativeTrawls;
        }
    }





}
