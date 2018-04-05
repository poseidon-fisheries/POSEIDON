package uk.ac.ox.oxfish.experiments.indonesia;

import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionHomogeneousGearFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HoldLimitingDecoratorFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscarding;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.ThreePricesMarket;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.scenario.IndonesiaScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

public class PartialSubsidy {


    private static final Path DIRECTORY = Paths.get("docs","20180214 indonesia");
    private static final Path SCENARIO_FILE = DIRECTORY.resolve("partial.yaml");
    public static final int DISCARDING_BIN = 56;
    public static final int MAXIMUM_FINED_BIN = 55;
    public static final int EXPECTED_NUMBER_OF_BINS = 100;
    public static final int NUMBER_OF_RUNS = 5;
    public static final int NUMBER_OF_YEARS_NO_FISHING = 0;
    public static final int NUMBER_OF_YEARS_FISHING = 5;


    @NotNull
    public static FileWriter prepWriter(File outputFile) throws IOException {
        FileWriter writer = new FileWriter(outputFile);
        writer.write("run,price_low,price_high,landings,earnings,biomass,kupang,benoa,");
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
        {
            writer.write("landings_" + i);
            writer.write(",");
        }
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
        {
            writer.write(",");
            writer.write("catches_" + i);
        }

        writer.write("\n");
        writer.flush();
        return writer;
    }


    public   static void dumpObservation(FileWriter writer, int run, double fine, FishState state, double subsidy) throws IOException {
        StringBuffer observation = new StringBuffer();
        observation.append(Integer.toString(run)).append(",");
        observation.append(fine).append(",");
        observation.append(subsidy).append(",");
        observation.append(state.getLatestYearlyObservation("Aphareus rutilans Landings")).append(",");
        observation.append(state.getLatestYearlyObservation("Aphareus rutilans Earnings")).append(",");
        observation.append(state.getLatestYearlyObservation("Biomass Aphareus rutilans")).append(",");
        observation.append(Double.toString(
                state.getLatestYearlyObservation("Kupang Number Of Fishers"))).append(",");
        observation.append(Double.toString(
                state.getLatestYearlyObservation("Benoa Number Of Fishers"))).append(",");
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
            observation.append(state.getLatestYearlyObservation("Aphareus rutilans Landings - age bin "+i)).append(",");


        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
            observation.append(",").append(state.getLatestYearlyObservation("Aphareus rutilans Catches - age bin "+i));
        observation.append("\n");
        writer.write(observation.toString());
        writer.flush();
        System.out.println(observation);
    }



    public static void main(String[] args) throws IOException {
        //fine();
        subsidy();
       // subsidyTotal();
      //  seasonLength(false);
      //  seasonLength(true);
    }

    public static void seasonLength(boolean increase) throws IOException {
        File outputFile = increase ?
                DIRECTORY.resolve( "season_i.csv").toFile() :
                DIRECTORY.resolve( "season.csv").toFile();
        FileWriter writer = prepWriter(outputFile);
        for(int season = 1; season<=365; season++)
        {
            for(int run = 0; run< 1; run++) {

                FishState state = new FishState(System.currentTimeMillis());
                FishYAML yaml = new FishYAML();
                IndonesiaScenario scenario = yaml.loadAs(
                        new FileReader(
                                DIRECTORY.resolve("season.yaml").toFile()
                        ), IndonesiaScenario.class
                );
                state.setScenario(scenario);
                scenario.getFisherDefinition().setRegulation(new FishingSeasonFactory(season,false));
                state.start();


                if(increase)
                    state.scheduleEveryYear(new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            FixedProportionHomogeneousGearFactory factory = new FixedProportionHomogeneousGearFactory();
                            factory.setLitersOfGasConsumed(new FixedDoubleParameter(5));
                            factory.setAverageCatchability(new FixedDoubleParameter(.001*Math.pow(1.02,state.getYear()+1)));
                            HoldLimitingDecoratorFactory parent = new HoldLimitingDecoratorFactory();
                            parent.setDelegate(factory);
                            for(Fisher fisher : state.getFishers())
                            {
                                fisher.setGear(parent.apply(state));
                            }

                        }
                    }, StepOrder.DAWN);

                while (state.getYear() <= 10)
                    state.schedule.step(state);


                dumpObservation(writer,run, -1, state, season);
            }
        }
    }

    public static void subsidy() throws IOException {
        File outputFile = DIRECTORY.resolve( "subsidy.csv").toFile();
        FileWriter writer = prepWriter(outputFile);
        for(double subsidy=10; subsidy<=30; subsidy++)
        {

            for(int run = 0; run< NUMBER_OF_RUNS; run++) {
                FishState state = new FishState(System.currentTimeMillis());
                FishYAML yaml = new FishYAML();
                IndonesiaScenario scenario = yaml.loadAs(
                        new FileReader(
                                SCENARIO_FILE.toFile()
                        ), IndonesiaScenario.class
                );
                state.setScenario(scenario);

                ThreePricesMarketFactory market = new ThreePricesMarketFactory();
                scenario.setMarket(market);

                state.start();

                Species species = state.getSpecies().get(0);
                Port benoa = state.getPorts().stream().filter(port -> port.getName().
                        equalsIgnoreCase("Benoa")).findFirst().get();
                ((ThreePricesMarket) benoa.getDefaultMarketMap().getMarket(species)).setPriceBetweenThresholds(subsidy);
                ((ThreePricesMarket) benoa.getDefaultMarketMap().getMarket(species)).setPriceAboveThresholds(subsidy);


                for (Fisher fisher : state.getFishers()) {
                    fisher.setRegulation(new Anarchy());
                }

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING)
                    state.schedule.step(state);


                dumpObservation(writer,run, 10, state, subsidy);

            }

        }
    }

    public static void subsidyTotal() throws IOException {
        File outputFile = DIRECTORY.resolve( "subsidytotal.csv").toFile();
        FileWriter writer = prepWriter(outputFile);
        for(double subsidy=10; subsidy<=30; subsidy++)
        {

            for(int run = 0; run< NUMBER_OF_RUNS; run++) {
                FishState state = new FishState(System.currentTimeMillis());
                FishYAML yaml = new FishYAML();
                IndonesiaScenario scenario = yaml.loadAs(
                        new FileReader(
                                SCENARIO_FILE.toFile()
                        ), IndonesiaScenario.class
                );
                state.setScenario(scenario);

                ThreePricesMarketFactory market = new ThreePricesMarketFactory();
                scenario.setMarket(market);

                state.start();

                Species species = state.getSpecies().get(0);
                for(Port benoa : state.getPorts()) {
                    ((ThreePricesMarket) benoa.getDefaultMarketMap().getMarket(species)).setPriceBetweenThresholds(subsidy);
                    ((ThreePricesMarket) benoa.getDefaultMarketMap().getMarket(species)).setPriceAboveThresholds(subsidy);
                }

                for (Fisher fisher : state.getFishers()) {
                    fisher.setRegulation(new Anarchy());
                }

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING)
                    state.schedule.step(state);


                dumpObservation(writer,run, 10, state, subsidy);

            }

        }
    }

    public static void fine() throws IOException {
        File outputFile = DIRECTORY.resolve( "fine.csv").toFile();
        FileWriter writer = prepWriter(outputFile);
        for(double fine=10; fine>=-30; fine= fine-1d)
        {

            for(int run = 0; run< NUMBER_OF_RUNS; run++) {
                FishState state = new FishState(System.currentTimeMillis());
                FishYAML yaml = new FishYAML();
                IndonesiaScenario scenario = yaml.loadAs(
                        new FileReader(
                                SCENARIO_FILE.toFile()
                        ), IndonesiaScenario.class
                );
                state.setScenario(scenario);

                ThreePricesMarketFactory market = new ThreePricesMarketFactory();
                scenario.setMarket(market);

                state.start();

                Species species = state.getSpecies().get(0);
                Port kupang = state.getPorts().stream().filter(port -> port.getName().
                        equalsIgnoreCase("Kupang")).findFirst().get();
                ((ThreePricesMarket) kupang.getDefaultMarketMap().getMarket(species)).setPriceBelowThreshold(fine);


                for (Fisher fisher : state.getFishers()) {
                    fisher.setRegulation(new Anarchy());
                }

                while (state.getYear() <= NUMBER_OF_YEARS_FISHING)
                    state.schedule.step(state);


                dumpObservation(writer,run, fine, state, 10d);

            }

        }
    }
}
