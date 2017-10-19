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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFromDirectoryFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbstractScenario;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class CaliforniaBiomassDynamics {


    public static final Path MAIN_DIRECTORY =
            Paths.get("docs", "20170730 validation", "best");

    public static void main(String[] args) throws IOException {

        CaliforniaAbundanceScenario scenario = new CaliforniaAbundanceScenario();
        scenario.setResetBiologyAtYear1(false);
        scenario.setRegulationPreReset(new FishingSeasonFactory(0,true));
        scenario.setUsePremadeInput(false);
        scenario.setSablefishDiffusingRate(0);
        scenario.setCountFileName("count_2011.csv");
        scenario.setMortalityAt100PercentForOldestFish(false);
        scenario.setExogenousCatches(new HashMap<>());
        scenario.setSablefishDiffusingRate(0);
        scenario.setPortFileName("no_ports.csv");

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        Species sablefish = state.getBiology().getSpecie("Sablefish");
        Species rockfish = state.getBiology().getSpecie("Yelloweye Rockfish");
        Species shortThornyhead = state.getBiology().getSpecie("Shortspine Thornyhead");
        Species longspineThornyhead = state.getBiology().getSpecie("Longspine Thornyhead");
        Species doverSole = state.getBiology().getSpecie("Dover Sole");

        state.schedule.step(state);
        StringBuilder demography = new StringBuilder();
        demography.append("year,age,abundance").append("\n");
        for(int age=0; age<sablefish.getMaxAge(); age++)
        {
            demography.append(state.getYear()).append(",").append(age).append(",").append(
                    state.getTotalAbundance(sablefish,age)
            ).append("\n");
        }


        FileWriter writer = new FileWriter(MAIN_DIRECTORY.resolve("dynamics.csv").toFile());
        writer.write("year,sablefish,yelloweye,short,long,doversole");
        writer.write("\n");


        while(state.getYear()<500)
        {
            state.schedule.step(state);
            if(state.getDayOfTheYear()==2) {
                System.out.println(state.getYear() + "," +
                                           state.getTotalBiomass(sablefish) / 1000 + ","
                                           +
                                           state.getTotalBiomass(rockfish) / 1000 +
                                           ","
                                           +
                                           state.getTotalBiomass(shortThornyhead) / 1000 +
                                           ","
                                           +
                                           state.getTotalBiomass(longspineThornyhead) / 1000 + ","
                                           +
                                           state.getTotalBiomass(doverSole) / 1000

                );
                writer.write(state.getYear() + "," +
                                     state.getTotalBiomass(sablefish) / 1000 + ","
                                     +
                                     state.getTotalBiomass(rockfish) / 1000 +
                                     ","
                                     +
                                     state.getTotalBiomass(shortThornyhead) / 1000 +
                                     ","
                                     +
                                     state.getTotalBiomass(longspineThornyhead) / 1000 + ","
                                     +
                                     state.getTotalBiomass(doverSole) / 1000);

                writer.write("\n");
                writer.flush();
            }
        }

        writer.close();
        demography.append("year,age,abundance").append("\n");
        for(int age=0; age<sablefish.getMaxAge(); age++)
        {
            demography.append(state.getYear()).append(",").append(age).append(",").append(
                    state.getTotalAbundance(sablefish,age)
            ).append("\n");
        }

        Files.write(MAIN_DIRECTORY.resolve("demography.csv"),demography.toString().getBytes());




    }
}
