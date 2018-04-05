package uk.ac.ox.oxfish.experiments;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscarding;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.factory.PortBasedWaitTimesFactory;
import uk.ac.ox.oxfish.model.scenario.IndonesiaScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class SimplePortSwitchingExperiment {

    private static final Path DIRECTORY = Paths.get("docs", "20180214 indonesia");
    private static final int YEARS_PER_RUN = 5;
    public static final int NUMBER_OF_RUNS = 1;
    private static final int EXPECTED_NUMBER_OF_BINS = 100;

    @NotNull
    public static FileWriter prepWriter(File outputFile) throws IOException {
        FileWriter writer = new FileWriter(outputFile);
        //writer.write("price_low,price_high,landings,earnings,cash-flow,landings_0,landings_1,landings_2,discarding_agents,catches_0");
        writer.write("run,delay,kupang,benoa");
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
        {
            writer.write(",");
            writer.write("rusty_landings_" + i);
        }
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
        {
            writer.write(",");
            writer.write("rusty_catches_" + i);
        }
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
        {
            writer.write(",");
            writer.write("lutjanus_landings_" + i);
        }
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
        {
            writer.write(",");
            writer.write("lutjanus_catches_" + i);
        }

        writer.write("\n");
        writer.flush();
        return writer;
    }

    public   static void dumpObservation(FileWriter writer,int run, int hourDelay, FishState state) throws IOException {
        StringBuffer observation = new StringBuffer();
        observation.append(Integer.toString(run)).append(",");
        observation.append(Integer.toString(hourDelay)).append(",");
        observation.append(Double.toString(
                state.getLatestYearlyObservation("Kupang Number Of Fishers"))
        ).append(",");
        observation.append(Double.toString(
                state.getLatestYearlyObservation("Benoa Number Of Fishers"))
        );
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
            observation.append(",").append(state.getLatestYearlyObservation("Rusty Snapper Landings - age bin "+i));
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
            observation.append(",").append(state.getLatestYearlyObservation("Rusty Snapper Catches - age bin "+i));

        //observation.append(",");
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
            observation.append(",")
                    .append(state.getLatestYearlyObservation("Crimson Snapper Landings - age bin "+i));
        for(int i=0; i<EXPECTED_NUMBER_OF_BINS; i++)
            observation.append(",").
                    append(state.getLatestYearlyObservation("Crimson Snapper Catches - age bin "+i));

        observation.append("\n");
        writer.write(observation.toString());
        writer.flush();
        System.out.println(observation);
    }

    public static void  main(String[] args) throws IOException {
        twospecies();
    }

    public static void twospecies() throws IOException {

        File outputFile = DIRECTORY.resolve("2species.csv").toFile();
        FileWriter writer = prepWriter(outputFile);

        for(int hourDelay =-500; hourDelay<500; hourDelay+=100)
        {

            for (int run = 0; run < NUMBER_OF_RUNS; run++) {
                FishState state = new FishState(System.currentTimeMillis());
                FishYAML yaml = new FishYAML();
                IndonesiaScenario scenario = yaml.loadAs(new FileReader(DIRECTORY.resolve("2species.yaml").toFile()),
                                                         IndonesiaScenario.class);
                if(hourDelay<0) {
                    ((PortBasedWaitTimesFactory) scenario.getFisherDefinition().getRegulation()).getPortWaitTimes().put("Kupang",
                                                                                                  Integer.toString(Math.abs(hourDelay)));
                    ((PortBasedWaitTimesFactory) scenario.getFisherDefinition().getRegulation()).getPortWaitTimes().put("Benoa", "0");
                }
                else
                {
                    ((PortBasedWaitTimesFactory) scenario.getFisherDefinition().getRegulation()).getPortWaitTimes().put("Kupang",
                                                                                                  "0");
                    ((PortBasedWaitTimesFactory) scenario.getFisherDefinition().getRegulation()).getPortWaitTimes().put("Benoa",
                                                                                                  Integer.toString(hourDelay));
                }
                state.setScenario(scenario);

                state.start();


                while (state.getYear() <= YEARS_PER_RUN)
                    state.schedule.step(state);

                state.schedule.step(state);
                dumpObservation(writer, run, hourDelay,state);
            }

        }
    }







    public static void switching(String[] args) throws IOException {

        FileWriter writer = new FileWriter(
                DIRECTORY.resolve("port_switching5.csv").toFile()
        );
        writer.write("kupang,benoa,delay");
        writer.write("\n");
        writer.flush();


        for(int hourDelay =150; hourDelay<400; hourDelay+=1)
        {

            FishYAML yaml = new FishYAML();
            IndonesiaScenario scenario = yaml.loadAs(new FileReader(DIRECTORY.resolve("port_switching.yaml").toFile()),
                                                     IndonesiaScenario.class);
            ((PortBasedWaitTimesFactory) scenario.getFisherDefinition().getRegulation()).getPortWaitTimes().put("Kupang",Integer.toString(hourDelay));
            ((PortBasedWaitTimesFactory) scenario.getFisherDefinition().getRegulation()).getPortWaitTimes().put("Benoa","0");

            FishState fishState = new FishState(0);
            fishState.setScenario(scenario);
            fishState.start();
            while(fishState.getYear()<YEARS_PER_RUN)
                fishState.schedule.step(fishState);
            fishState.schedule.step(fishState);


            writer.write(
                    Double.toString(
                            fishState.getLatestYearlyObservation("Kupang Number Of Fishers"))
            );
            writer.write(",");
            writer.write(
                    Double.toString(
                            fishState.getLatestYearlyObservation("Benoa Number Of Fishers"))
            );
            writer.write(",");
            writer.write(
                    Integer.toString(
                            hourDelay)
            );
            writer.write("\n");
            writer.flush();

        }




    }

}
