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


import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.IndonesiaScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;

public class OLSFailure {


    private final static Path DIRECTORY  = Paths.get("docs","20180221 ols_failure");



    public static void main(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();
        PrototypeScenario mainScenario = yaml.loadAs(
                new FileReader(DIRECTORY.resolve("oned_variable.yaml").toFile()),
                PrototypeScenario.class
        );


        FileWriter writer = new FileWriter(DIRECTORY.resolve("oned_variable.csv").toFile());
        writer.write("day,gas_price,average_x,average_distance,landings,cash-flow");
        writer.write("\n");


        FishState state = new FishState(0);
        state.attachAdditionalGatherers();
        state.setScenario(mainScenario);



        state.start();
        while (state.getYear()<=200) {

            for (Port port : state.getPorts()) {
                port.setGasPricePerLiter(
                        Math.max(0,
                                 state.getRandom().nextDouble()*20d)

                )
                ;
            }

            //every 60 days a new gas price
            for (int i = 0; i < 60; i++) {
                state.schedule.step(state);
            }
            writeLineOneD(writer, state);
            for(Fisher fisher : state.getFishers())
                for (Fisher friend : fisher.getAllFriends()) {
                    if(!friend.getHomePort().equals(friend.getHomePort()))
                        throw new RuntimeException("Can't have friends from separate ports!");
                }


        }
        while (state.getYear()<=300) {

            for (Port port : state.getPorts()) {
                port.setGasPricePerLiter(
                        Math.max(0,
                                 state.getRandom().nextDouble()*20d+15d)

                )
                ;
            }

            //every 60 days a new gas price
            for (int i = 0; i < 60; i++) {
                state.schedule.step(state);
            }
            writeLineOneD(writer, state);
            for(Fisher fisher : state.getFishers())
                for (Fisher friend : fisher.getAllFriends()) {
                    if(!friend.getHomePort().equals(friend.getHomePort()))
                        throw new RuntimeException("Can't have friends from separate ports!");
                }


        }

        while (state.getYear()<=350) {

            //add 10 more fishers
            for(int i=0; i<5; i++)
                state.createFisher(FishState.DEFAULT_POPULATION_NAME);

            for (Port port : state.getPorts()) {
                port.setGasPricePerLiter(
                        Math.max(0,
                                 state.getRandom().nextDouble()*20d)

                )
                ;
            }

            //every 60 days a new gas price
            for (int i = 0; i < 60; i++) {
                state.schedule.step(state);
            }
            writeLineOneD(writer, state);
            for(Fisher fisher : state.getFishers())
                for (Fisher friend : fisher.getAllFriends()) {
                    if(!friend.getHomePort().equals(friend.getHomePort()))
                        throw new RuntimeException("Can't have friends from separate ports!");
                }


        }



    }


    public static void oneD() throws IOException {

        FishYAML yaml = new FishYAML();
        PrototypeScenario mainScenario = yaml.loadAs(
                new FileReader(DIRECTORY.resolve("oned.yaml").toFile()),
                PrototypeScenario.class
        );


        FileWriter writer = new FileWriter(DIRECTORY.resolve("oned2.csv").toFile());
        writer.write("day,gas_price,average_x,average_distance,landings,cash-flow");
        writer.write("\n");


        FishState state = new FishState(0);
        state.attachAdditionalGatherers();
        state.setScenario(mainScenario);



        state.start();
        while (state.getYear()<=200) {

            for (Port port : state.getPorts()) {
                port.setGasPricePerLiter(
                        Math.max(0,
                                 state.getRandom().nextDouble()*2.5d)

                )
                ;
            }

            //every 60 days a new gas price
            for (int i = 0; i < 60; i++) {
                state.schedule.step(state);
            }
            writeLineOneD(writer, state);
            for(Fisher fisher : state.getFishers())
                for (Fisher friend : fisher.getAllFriends()) {
                    if(!friend.getHomePort().equals(friend.getHomePort()))
                        throw new RuntimeException("Can't have friends from separate ports!");
                }


        }
        while (state.getYear()<=300) {

            for (Port port : state.getPorts()) {
                port.setGasPricePerLiter(
                        Math.max(0,
                                 state.getRandom().nextDouble()*1d+3d)

                )
                ;
            }

            //every 60 days a new gas price
            for (int i = 0; i < 60; i++) {
                state.schedule.step(state);
            }
            writeLineOneD(writer, state);
            for(Fisher fisher : state.getFishers())
                for (Fisher friend : fisher.getAllFriends()) {
                    if(!friend.getHomePort().equals(friend.getHomePort()))
                        throw new RuntimeException("Can't have friends from separate ports!");
                }


        }

        while (state.getYear()<=350) {

            for (Port port : state.getPorts()) {
                port.setGasPricePerLiter(
                        Math.max(0,
                                 state.getRandom().nextDouble()*2.5d)

                )
                ;
            }

            //every 60 days a new gas price
            for (int i = 0; i < 60; i++) {
                state.schedule.step(state);
            }
            writeLineOneD(writer, state);
            for(Fisher fisher : state.getFishers())
                for (Fisher friend : fisher.getAllFriends()) {
                    if(!friend.getHomePort().equals(friend.getHomePort()))
                        throw new RuntimeException("Can't have friends from separate ports!");
                }


        }



    }

    public static void writeLineOneD(FileWriter writer, FishState state) throws IOException {
        //"benoa,kupang,gas_price,average_x,average_distance,landings,cash-flow"
        writer.write(
                Double.toString(
                        state.getDay()
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        state.getPorts().get(0).getGasPricePerLiter()
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        getLast7DaysAverage("Average X Towed",state,true)
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        getLast7DaysAverage("Average Distance From Port",state,true)
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        getLast7DaysAverage("Species 0 Landings",state,false)
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        getLast7DaysAverage("Average Cash-Flow",state,false)
                )
        );
        writer.write("\n");
    }



    public static void indonesia(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();
        IndonesiaScenario mainScenario = yaml.loadAs(
                new FileReader(DIRECTORY.resolve("indo_simple.yaml").toFile()),
                IndonesiaScenario.class
        );


        FileWriter writer = new FileWriter(DIRECTORY.resolve("indo_simple.csv").toFile());
        writer.write("day,benoa,kupang,gas_price,average_x,average_distance,landings,cash-flow");
        writer.write("\n");


        FishState state = new FishState(0);
        state.attachAdditionalGatherers();
        state.setScenario(mainScenario);



        state.start();
        while (state.getYear()<=200) {

            for (Port port : state.getPorts()) {
                port.setGasPricePerLiter(state.getRandom().nextDouble()*.001 +  state.getYear()/200d);
            }

            //every 60 days a new gas price
            for (int i = 0; i < 60; i++) {
                state.schedule.step(state);
            }
            writeLineIndonesia(writer, state);
            for(Fisher fisher : state.getFishers())
                for (Fisher friend : fisher.getAllFriends()) {
                    if(!friend.getHomePort().equals(friend.getHomePort()))
                        throw new RuntimeException("Can't have friends from separate ports!");
                }


        }




    }

    public static void writeLineIndonesia(FileWriter writer, FishState state) throws IOException {
        //"benoa,kupang,gas_price,average_x,average_distance,landings,cash-flow"
        writer.write(
                Double.toString(
                        state.getDay()
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        state.getLatestYearlyObservation("Benoa Number Of Active Fishers")
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        state.getLatestYearlyObservation("Kupang Number Of Active Fishers")
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        state.getPorts().get(0).getGasPricePerLiter()
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        getLast7DaysAverage("Average X Towed",state,true)
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        getLast7DaysAverage("Average Distance From Port",state,true)
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        getLast7DaysAverage("Species 0 Landings",state,false)
                )
        );
        writer.write(",");
        writer.write(
                Double.toString(
                        getLast7DaysAverage("Average Cash-Flow",state,false)
                )
        );
        writer.write("\n");
    }



    private static double getLast7DaysAverage(String columnName, FishState state,boolean ignore0)
    {

        DataColumn column = state.getDailyDataSet().getColumn(columnName);
        DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
        Iterator<Double> iterator = column.descendingIterator();
        for(int i=0; i<15; i++) {
            Double value = iterator.next();
            if(value != 0 || !ignore0)
                statistics.accept(value);
        }
        return statistics.getAverage();




    }
}
