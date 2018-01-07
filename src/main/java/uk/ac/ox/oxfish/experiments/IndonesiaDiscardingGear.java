package uk.ac.ox.oxfish.experiments;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.PeriodicUpdateSelectivityFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.scenario.IndonesiaScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IndonesiaDiscardingGear {


    //boxcar
    private static final Path DIRECTORY = Paths.get("docs", "20180105 boxcar_gears");
    public static final int MAXIMUM_FINED_BIN = 55;
    public static final int NUMBER_OF_RUNS = 20;
    public static final int NUMBER_OF_YEARS_NO_FISHING = 0;
    public static final int NUMBER_OF_YEARS_FISHING = 5;


    public static void main(String[] args) throws IOException {
       // compareContinuous();
        compareDiscrete();
    }

    public static void compareContinuous() throws IOException {

        Path scenarioFile = DIRECTORY.resolve("continuous.yaml");
        File outputFile = DIRECTORY.resolve("continuous.csv").toFile();
        File timeline = DIRECTORY.resolve("continuous_timeline.csv").toFile();
        FileWriter writer = IndonesiaDiscarding.prepWriter(outputFile);
        FileWriter timeLine = prepTimelineWriter(timeline);

        double[] juvenilePrices = new double[]{-30,10,0};
        double[] maturePrices = new double[]{10,30};

        for (double juvenilePrice : juvenilePrices) {
            for (double maturePrice : maturePrices) {

                for (int run = 0; run < NUMBER_OF_RUNS; run++) {
                    FishState state = new FishState(System.currentTimeMillis());
                    FishYAML yaml = new FishYAML();
                    IndonesiaScenario scenario = yaml.loadAs(
                            new FileReader(
                                    scenarioFile.toFile()
                            ), IndonesiaScenario.class
                    );
                    state.setScenario(scenario);

                    ThreePricesMarketFactory market = new ThreePricesMarketFactory();
                    scenario.setMarket(market);
                    market.setLowAgeThreshold(new FixedDoubleParameter(MAXIMUM_FINED_BIN));
                    market.setHighAgeThreshold(new FixedDoubleParameter(1000000));
                    market.setPriceBelowThreshold(new FixedDoubleParameter(juvenilePrice));
                    market.setPriceBetweenThresholds(new FixedDoubleParameter(maturePrice));

                    state.start();
                    if(NUMBER_OF_YEARS_NO_FISHING>0) {

                        for (Fisher fisher : state.getFishers()) {
                            fisher.setRegulation(new FishingSeason(true,0));
                        }
                        while (state.getYear() <= NUMBER_OF_YEARS_NO_FISHING)
                            state.schedule.step(state);
                    }
                    for (Fisher fisher : state.getFishers()) {
                        fisher.setRegulation(new Anarchy());
                    }

                    while (state.getYear() <= NUMBER_OF_YEARS_FISHING+NUMBER_OF_YEARS_NO_FISHING)
                        state.schedule.step(state);

                    state.schedule.step(state);
                    IndonesiaDiscarding.dumpObservation(writer, run, juvenilePrice, state, maturePrice);
                    dumpTimeLine(timeLine,run,state,juvenilePrice,maturePrice);
                }

            }
        }
    }




    @NotNull
    public static FileWriter prepTimelineWriter(File outputFile) throws IOException {
        FileWriter writer = new FileWriter(outputFile);
        //writer.write("price_low,price_high,landings,earnings,cash-flow,landings_0,landings_1,landings_2,discarding_agents,catches_0");
        writer.write("run,year,price_juvenile,price_mature,selectivityA,selectivityB");
        writer.write("\n");
        writer.flush();
        return writer;
    }

    public static void dumpTimeLine(FileWriter writer, int run,FishState model, double juvenilePrice, double maturePrice) throws IOException {
        for(int year=0; year<model.getYear(); year++)
        {
            writer.write(Integer.toString(run));
            writer.write(",");

            writer.write(Integer.toString(year));
            writer.write(",");
            writer.write(Double.toString(juvenilePrice));
            writer.write(",");
            writer.write(Double.toString(maturePrice));
            writer.write(",");

            writer.write(Double.toString(model.getYearlyDataSet().getColumn("Average Selectivity A Parameter").get(year)));
            writer.write(",");

            writer.write(Double.toString(model.getYearlyDataSet().getColumn("Average Selectivity B Parameter").get(year)));
            writer.write("\n");
        }
        writer.flush();
    }



    public static void compareDiscrete() throws IOException {

        Path scenarioFile = DIRECTORY.resolve("discrete.yaml");
        File outputFile = DIRECTORY.resolve("discrete.csv").toFile();
        File timeline = DIRECTORY.resolve("discrete_timeline.csv").toFile();
        FileWriter writer = IndonesiaDiscarding.prepWriter(outputFile);
        FileWriter timeLine = prepTimelineWriter(timeline);

        double[] juvenilePrices = new double[]{-30,10,0};
        double[] maturePrices = new double[]{10,30};

        for (double juvenilePrice : juvenilePrices) {
            for (double maturePrice : maturePrices) {

                for (int run = 0; run < NUMBER_OF_RUNS; run++) {
                    FishState state = new FishState(System.currentTimeMillis());
                    FishYAML yaml = new FishYAML();
                    IndonesiaScenario scenario = yaml.loadAs(
                            new FileReader(
                                    scenarioFile.toFile()
                            ), IndonesiaScenario.class
                    );
                    state.setScenario(scenario);

                    ThreePricesMarketFactory market = new ThreePricesMarketFactory();
                    scenario.setMarket(market);
                    market.setLowAgeThreshold(new FixedDoubleParameter(MAXIMUM_FINED_BIN));
                    market.setHighAgeThreshold(new FixedDoubleParameter(1000000));
                    market.setPriceBelowThreshold(new FixedDoubleParameter(juvenilePrice));
                    market.setPriceBetweenThresholds(new FixedDoubleParameter(maturePrice));

                    state.registerStartable(PeriodicUpdateSelectivityFactory.SELECTIVITY_DATA_GATHERERS);

                    state.start();
                    if(NUMBER_OF_YEARS_NO_FISHING>0) {

                        for (Fisher fisher : state.getFishers()) {
                            fisher.setRegulation(new FishingSeason(true,0));
                        }
                        while (state.getYear() <= NUMBER_OF_YEARS_NO_FISHING)
                            state.schedule.step(state);
                    }
                    for (Fisher fisher : state.getFishers()) {
                        fisher.setRegulation(new Anarchy());
                    }

                    while (state.getYear() <= NUMBER_OF_YEARS_FISHING+NUMBER_OF_YEARS_NO_FISHING)
                        state.schedule.step(state);

                    state.schedule.step(state);
                    IndonesiaDiscarding.dumpObservation(writer, run, juvenilePrice, state, maturePrice);
                    dumpTimeLine(timeLine,run,state,juvenilePrice,maturePrice);
                }

            }
        }
    }





}
