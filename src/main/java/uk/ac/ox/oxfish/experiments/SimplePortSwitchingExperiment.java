package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.PortBasedWaitTimesFactory;
import uk.ac.ox.oxfish.model.scenario.IndonesiaScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimplePortSwitchingExperiment {

    private static final Path DIRECTORY = Paths.get("docs", "20180214 indonesia");
    private static final int YEARS_PER_RUN = 10;


    public static void main(String[] args) throws IOException {

        FileWriter writer = new FileWriter(
                DIRECTORY.resolve("port_switching2.csv").toFile()
        );
        writer.write("kupang,benoa,delay");
        writer.write("\n");
        writer.flush();


        for(int hourDelay =11; hourDelay<1000; hourDelay++)
        {

            FishYAML yaml = new FishYAML();
            IndonesiaScenario scenario = yaml.loadAs(new FileReader(DIRECTORY.resolve("port_switching.yaml").toFile()),
                    IndonesiaScenario.class);
            ((PortBasedWaitTimesFactory) scenario.getRegulation()).getPortWaitTimes().put("Kupang",hourDelay);
            ((PortBasedWaitTimesFactory) scenario.getRegulation()).getPortWaitTimes().put("Benoa",0);

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
