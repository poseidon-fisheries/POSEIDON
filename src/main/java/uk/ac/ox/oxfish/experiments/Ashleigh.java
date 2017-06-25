package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.TwoPopulationsScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by carrknight on 6/18/17.
 */
public class Ashleigh {


    public static final int FISHERS = 100;
    public static final int RUNS = 10;
    public static final int YEARS = 30;

    public static void main(String[] args) throws IOException {

        FileWriter writer = new FileWriter(Paths.get("/home/carrknight/Dropbox/poseidon_exec/ashleigh2_04_multirun.csv").toFile());

        String[] columns = new String[]{
               "Trips Exploring",
                "Trips Exploiting",
                "Trips Imitating",
                "Small Fishers Total Income",
                "Large Fishers Total Income",
                "Small Fishers Species 0 Landings",
                "Large Fishers Species 0 Landings",
                "Species 0 Landings",
                "Species 0 Earnings",
                "Species 0 Catches",
                "Biomass Species 0",
                "Average Cash-Flow",
                "Average Trip Duration",
                "Average Distance From Port",
                "Average Hours Out",
                "Total Effort"
        };

        StringBuilder header = new StringBuilder("value,runs,year");
        for(String column : columns)
            header.append(",").append(column);
        header.append("\n");
        System.out.print(header);
        writer.write(header.toString());
        writer.flush();
        for(int eei = 0; eei<= FISHERS; eei++)
        {
            for(int runs = 0; runs< RUNS; runs++)
            {
                FishState state = new FishState(runs);
                FishYAML yaml = new FishYAML();
                TwoPopulationsScenario scenario = yaml.loadAs(new FileReader(
                        Paths.get("social_3A2.yaml").toFile()
                ), TwoPopulationsScenario.class);

                scenario.setAllowTwoPopulationFriendships(true);
                scenario.setLargeFishers(eei);
                scenario.setSmallFishers(200-eei);
                state.setScenario(scenario);
                state.start();
                state.schedule.step(state);
                for(int year = 0; year< YEARS; year++)
                {
                    for(int day=0; day<365; day++)
                        state.schedule.step(state);
                    StringBuilder row = new StringBuilder(eei+","+runs+"," +year);
                    for(String column : columns)
                        row.append(",").append(state.getYearlyDataSet().getLatestObservation(column));
                    row.append("\n");
                    writer.write(row.toString());
                    writer.flush();
                    System.out.print(row.toString());
                }

            }
        }
    }
}
