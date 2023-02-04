/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomThenBackToPortFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MaximumSustainableYield {



    ///home/carrknight/code/oxfish/inputs/first_paper/sensitivity/quotas/separated.yaml
    private final static Path MAIN_FILE =
            Paths.get("inputs", "first_paper", "sensitivity","quotas","separated.yaml");

    private final static Path OUTPUT_FILE =
            Paths.get("docs", "20180201 msy");

    private final static int MAX_YEARS = 100;


    public static void main(String[] args) throws IOException {
        //anarchy();
        //mpa();
        random();
    }

    private static void random() throws IOException {

        FileWriter writer = new FileWriter(OUTPUT_FILE.resolve("random.csv").toFile(),true);

        if(!OUTPUT_FILE.resolve("random.csv").toFile().exists() ||
                OUTPUT_FILE.resolve("random.csv").toFile().length()/1024 < 1) {
            writer.write("fishers,run,landings_blue,landings_red,year");
            writer.write("\n");
            writer.flush();
        }

        for(int fishers=200; fishers<300; fishers+=1)
        {

            for(int run=0; run<5; run++)
            {


                FishYAML yaml = new FishYAML();
                PrototypeScenario scenario = yaml.loadAs(new FileReader(MAIN_FILE.toFile()),
                                                         PrototypeScenario.class);
                scenario.setFishers(fishers);
                scenario.setRegulation(new AnarchyFactory());
                scenario.setDestinationStrategy(new RandomThenBackToPortFactory());
                FishState model = new FishState(run);
                model.setScenario(scenario);


                model.start();
                while(model.getYear()<MAX_YEARS)
                {
                    model.schedule.step(model);
                    if(model.getDayOfTheYear()==1 && model.getYear()>0)
                    {
                        writer.write(Integer.toString(fishers));
                        writer.write(",");
                        writer.write(Integer.toString(run));
                        writer.write(",");
                        writer.write(Double.toString(model.getLatestYearlyObservation("Species 1 Landings")));
                        writer.write(",");
                        writer.write(Double.toString(model.getLatestYearlyObservation("Species 0 Landings")));
                        writer.write(",");
                        writer.write(Integer.toString(model.getYear()));
                        writer.write("\n");
                        writer.flush();
                    }
                }



            }

        }
        writer.close();


    }

    private static void mpa() throws IOException {

        FileWriter writer = new FileWriter(OUTPUT_FILE.resolve("mpa.csv").toFile());

        writer.write("fishers,run,landings_blue,landings_red,year");
        writer.write("\n");
        writer.flush();


        for(int fishers=10; fishers<200; fishers+=1)
        {

            for(int run=0; run<5; run++)
            {


                FishYAML yaml = new FishYAML();
                PrototypeScenario scenario = yaml.loadAs(new FileReader(MAIN_FILE.toFile()),
                                                         PrototypeScenario.class);
                scenario.setFishers(fishers);
                scenario.setRegulation(new ProtectedAreasOnlyFactory());
                scenario.getStartingMPAs().add(
                        new StartingMPA(
                                0,25,50,50
                        )
                );
                FishState model = new FishState(run);
                model.setScenario(scenario);

                model.start();
                while(model.getYear()<MAX_YEARS)
                {
                    model.schedule.step(model);
                    if(model.getDayOfTheYear()==1 && model.getYear()>0)
                    {
                        writer.write(Integer.toString(fishers));
                        writer.write(",");
                        writer.write(Integer.toString(run));
                        writer.write(",");
                        writer.write(Double.toString(model.getLatestYearlyObservation("Species 1 Landings")));
                        writer.write(",");
                        writer.write(Double.toString(model.getLatestYearlyObservation("Species 0 Landings")));
                        writer.write(",");
                        writer.write(Integer.toString(model.getYear()));
                        writer.write("\n");
                        writer.flush();
                    }
                }



            }

        }
        writer.close();


    }

    private static void anarchy() throws IOException {

        FileWriter writer = new FileWriter(OUTPUT_FILE.resolve("anarchy.csv").toFile());

        writer.write("fishers,run,landings_blue,landings_red,year");
        writer.write("\n");
        writer.flush();


        for(int fishers=10; fishers<200; fishers+=1)
        {

            for(int run=0; run<5; run++)
            {


                FishYAML yaml = new FishYAML();
                PrototypeScenario scenario = yaml.loadAs(new FileReader(MAIN_FILE.toFile()),
                                                                  PrototypeScenario.class);
                scenario.setFishers(fishers);
                scenario.setRegulation(new AnarchyFactory());
                FishState model = new FishState(run);
                model.setScenario(scenario);

                model.start();
                while(model.getYear()<MAX_YEARS)
                {
                    model.schedule.step(model);
                    if(model.getDayOfTheYear()==1 && model.getYear()>0)
                    {
                        writer.write(Integer.toString(fishers));
                        writer.write(",");
                        writer.write(Integer.toString(run));
                        writer.write(",");
                        writer.write(Double.toString(model.getLatestYearlyObservation("Species 1 Landings")));
                        writer.write(",");
                        writer.write(Double.toString(model.getLatestYearlyObservation("Species 0 Landings")));
                        writer.write(",");
                        writer.write(Integer.toString(model.getYear()));
                        writer.write("\n");
                        writer.flush();
                    }
                }



            }

        }
        writer.close();


    }

}
