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

package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgent;
import uk.ac.ox.oxfish.biology.boxcars.SprOracle;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Slice2SPR {

    public static final String DIRECTORY = "docs/indonesia_hub/runs/712/sclice2/policy/";

    public static final String scenarioFileName = "guess_4";


    private static final int NUMBER_OF_RUNS = 100;
    public static final int YEARS_TO_RUN = 10;


    static private final double assumedLinfAreolatus =45;
    static private final double assumedKParameterAreolatus = 0.3300512;
    static private final double assumedNaturalMortalityAreolatus = 0.6011646;
    static private final double assumedVarAAreolatus = 0.01142;
    static private final double assumedVarBAreolatus = 3.048;
    static private final double assumedLenghtAtMaturityAreolatus = 21 ;



    static private final double assumedLinfMultidens = 86;
    static private final double assumedKParameterMultidens = 0.4946723;
    static private final double assumedNaturalMortalityMultidens = 0.394192;
    static private final double assumedVarAMultidens = 0.02;
    static private final double assumedVarBMultidens = 2.944;
    static private final double assumedLenghtAtMaturityMultidens = 48.0;


    static private final double assumedLinfMalabaricus = 86;
    static private final double assumedKParameterMalabaricus = 0.4438437;
    static private final double assumedNaturalMortalityMalabaricus = 0.3775984;
    static private final double assumedVarAMalabaricus = 0.00853;
    static private final double assumedVarBMalabaricus = 3.137;
    static private final double assumedLenghtAtMaturityMalabaricus = 50.0;
    public static final int VIRGIN_SPAWNING_BIOMASS_AREOLATUS = 5389362;
    public static final int VIRGIN_SPAWNING_BIOMASS_MULTIDENS = 40160483;
    public static final int VIRGIN_SPAWNING_BIOMASS_MALABARICUS = 153988743;


    public static void main(String[] args) throws IOException {
        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, scenarioFileName + "_SPRs.csv").toFile());
        fileWriter.write("run,year,variable,value\n");
        fileWriter.flush();

        //run the SPR runner once just to get the column names
        FlexibleScenario mock = new FlexibleScenario();
        List<String> columnNames = addSPRAgents(mock, new MersenneTwisterFast(0));

        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Average Cash-Flow",
                "Epinephelus areolatus Landings of population0",
                "Pristipomoides multidens Landings of population0",
                "Lutjanus malabaricus Landings of population0",
                "Lutjanus erythropterus Landings of population0",
                "Others Landings of population0",

                "Epinephelus areolatus Landings of population1",
                "Pristipomoides multidens Landings of population1",
                "Lutjanus malabaricus Landings of population1",
                "Lutjanus erythropterus Landings of population1",
                "Others Landings of population1",
                "Epinephelus areolatus Landings of population2",
                "Pristipomoides multidens Landings of population2",
                "Lutjanus malabaricus Landings of population2",
                "Lutjanus erythropterus Landings of population2",
                "Others Landings of population2",

                "Biomass Epinephelus areolatus",
                "Biomass Pristipomoides multidens",
                "Biomass Lutjanus malabaricus",
                "Biomass Lutjanus erythropterus",
                "Total Landings of population0",
                "Total Landings of population1",
                "Total Landings of population2"

        );
        columnsToPrint.addAll(columnNames);

        for(int run=0; run<NUMBER_OF_RUNS; run++)
        {

            MersenneTwisterFast randomizer = new MersenneTwisterFast();
            BatchRunner runner = new BatchRunner(
                    Paths.get(DIRECTORY,
                              scenarioFileName + ".yaml"),
                    YEARS_TO_RUN,
                    columnsToPrint,
                    Paths.get(DIRECTORY,
                              scenarioFileName),
                    null,
                    System.currentTimeMillis(),
                    -1
            );
            runner.setScenarioSetup(new Consumer<Scenario>() {
                @Override
                public void accept(Scenario scenario) {
                    addSPRAgents(((FlexibleScenario) scenario),randomizer);
                }
            });


            StringBuffer tidy = new StringBuffer();
            runner.run(tidy);
            fileWriter.write(tidy.toString());
            fileWriter.flush();
        }

        fileWriter.close();

    }



    private static List<String>  addSPRAgents(FlexibleScenario scenario, MersenneTwisterFast random){

        List<String> columns = new LinkedList<>();
        for(int i=0; i<10; i++) {
            //aereolatus

            //add 10, 2% sampler (16 fishers, on average)
            String surveyTag = "2.5_areolatus_v"+i;
            randomAreolatusSampling(scenario, random, surveyTag, .025, columns);
            //add 10, 5% sampler (40 fishers, on average)
            surveyTag = "5_areolatus_v"+i;
            randomAreolatusSampling(scenario, random, surveyTag, .05, columns);
            //add 10, 10% sampler (80 fishers, on average)
            surveyTag = "10_areolatus_v"+i;
            randomAreolatusSampling(scenario, random, surveyTag, .1, columns);
            //add 10, 20% sampler (160 fishers, on average)
            surveyTag = "20_areolatus_v"+i;
            randomAreolatusSampling(scenario, random, surveyTag, .2, columns);
            //add 10, 40% sampler (160 fishers, on average)
            surveyTag = "40_areolatus_v"+i;
            randomAreolatusSampling(scenario, random, surveyTag, .4, columns);
            //add 10, 80% sampler
            surveyTag = "80_areolatus_v"+i;
            randomAreolatusSampling(scenario, random, surveyTag, .8, columns);

            //multidens
            //add 10, 2% sampler (16 fishers, on average)
            surveyTag = "2.5_multidens_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .025, columns);
            //add 10, 5% sampler (40 fishers, on average)
            surveyTag = "5_multidens_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .05, columns);
            //add 10, 10% sampler (80 fishers, on average)
            surveyTag = "10_multidens_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .1, columns);
            //add 10, 20% sampler (160 fishers, on average)
            surveyTag = "20_multidens_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .2, columns);
            //add 10, 40% sampler (160 fishers, on average)
            surveyTag = "40_multidens_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .4, columns);
            //add 10, 80% sampler
            surveyTag = "80_multidens_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .8, columns);

            //malabaricus
            //add 10, 2% sampler (16 fishers, on average)
            surveyTag = "2.5_malabaricus_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .025, columns);
            //add 10, 5% sampler (40 fishers, on average)
            surveyTag = "5_malabaricus_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .05, columns);
            //add 10, 10% sampler (80 fishers, on average)
            surveyTag = "10_malabaricus_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .1, columns);
            //add 10, 20% sampler (160 fishers, on average)
            surveyTag = "20_malabaricus_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .2, columns);
            //add 10, 40% sampler (160 fishers, on average)
            surveyTag = "40_malabaricus_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .4, columns);
            //add 10, 80% sampler
            surveyTag = "80_malabaricus_v"+i;
            randomMultidensSampling(scenario, random, surveyTag, .8, columns);

        }

        //add a full one
        String surveyTag = "100_areolatus";
        randomAreolatusSampling(scenario, random, surveyTag, 1, columns);
        surveyTag = "100_multidens";
        randomMultidensSampling(scenario, random, surveyTag, 1, columns);
        surveyTag = "100_malabaricus";
        randomMalabaricusSampling(scenario, random, surveyTag, 1, columns);

        //add the oracles
        scenario.getPlugins().add(
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        return new SprOracle(
                                fishState.getBiology().getSpecie("Epinephelus areolatus"),
                                assumedLenghtAtMaturityAreolatus,
                                VIRGIN_SPAWNING_BIOMASS_AREOLATUS
                        );
                    }
                }
        );
        columns.add("SPR Oracle - Epinephelus areolatus");
        scenario.getPlugins().add(
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        return new SprOracle(
                                fishState.getBiology().getSpecie("Pristipomoides multidens"),
                                assumedLenghtAtMaturityMultidens,
                                VIRGIN_SPAWNING_BIOMASS_MULTIDENS
                        );
                    }
                }
        );
        columns.add("SPR Oracle - Pristipomoides multidens");
        scenario.getPlugins().add(
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        return new SprOracle(
                                fishState.getBiology().getSpecie("Lutjanus malabaricus"),
                                assumedLenghtAtMaturityMalabaricus,
                                VIRGIN_SPAWNING_BIOMASS_MALABARICUS
                        );
                    }
                }
        );

        columns.add("SPR Oracle - Lutjanus malabaricus");

        return columns;

    }

    //winning no prizes with this code

    private static void randomAreolatusSampling(
            FlexibleScenario scenario,
            MersenneTwisterFast random,
            String surveyTag,
            double probability,
            List<String> columns) {
        String speciesName = "Epinephelus areolatus";

        scenario.getPlugins().add(
                new SPRAgentBuilder(
                        surveyTag,
                        speciesName,
                        new Predicate<Fisher>() {
                            @Override
                            public boolean test(Fisher fisher) {
                                return random.nextBoolean(probability);
                            }
                        },
                        assumedLinfAreolatus, assumedKParameterAreolatus, assumedNaturalMortalityAreolatus,
                        100, 1000, 5,
                        assumedVarAAreolatus, assumedVarBAreolatus, assumedLenghtAtMaturityAreolatus
                )
        );
        columns.add("SPR " + speciesName + " " + surveyTag);


    }

    private static void randomMultidensSampling(
            FlexibleScenario scenario,
            MersenneTwisterFast random,
            String surveyTag,
            double probability,
            List<String> columns) {
        String speciesName = "Pristipomoides multidens";

        scenario.getPlugins().add(
                new SPRAgentBuilder(
                        surveyTag,
                        speciesName,
                        new Predicate<Fisher>() {
                            @Override
                            public boolean test(Fisher fisher) {
                                return random.nextBoolean(probability);
                            }
                        },
                        assumedLinfMultidens, assumedKParameterMultidens, assumedNaturalMortalityMultidens,
                        100, 1000, 5,
                        assumedVarAMultidens, assumedVarBMultidens, assumedLenghtAtMaturityMultidens
                )
        );
        columns.add("SPR " + speciesName + " " + surveyTag);


    }


    private static void randomMalabaricusSampling(
            FlexibleScenario scenario,
            MersenneTwisterFast random,
            String surveyTag,
            double probability,
            List<String> columns) {
        String speciesName = "Lutjanus malabaricus";

        scenario.getPlugins().add(
                new SPRAgentBuilder(
                        surveyTag,
                        speciesName,
                        new Predicate<Fisher>() {
                            @Override
                            public boolean test(Fisher fisher) {
                                return random.nextBoolean(probability);
                            }
                        },
                        assumedLinfMalabaricus, assumedKParameterMalabaricus, assumedNaturalMortalityMalabaricus,
                        100, 1000, 5,
                        assumedVarAMalabaricus, assumedVarBMalabaricus, assumedLenghtAtMaturityMalabaricus
                )
        );
        columns.add("SPR " + speciesName + " " + surveyTag);


    }


    /**
     * a factory whose only job is to put a given SPRAgent into the model
     */
    private static  class SPRAgentBuilder implements AlgorithmFactory<SPRAgent> {

        private final String surveyTag;

        private final String speciesName;

        private final Predicate<Fisher> samplingSelector;

        private final double assumedLinf;

        private final double assumedKParameter;

        private final double assumedNaturalMortality;

        private final int assumedMaxAge;

        private final double assumedVirginRecruits;

        private final double assumedLengthBinCm;

        private final double assumedVarA;

        private final double assumedVarB;

        private final double assumedLenghtAtMaturity;


        public SPRAgentBuilder(
                String surveyTag, String speciesName,
                Predicate<Fisher> samplingSelector, double assumedLinf, double assumedKParameter,
                double assumedNaturalMortality, int assumedMaxAge, double assumedVirginRecruits,
                double assumedLengthBinCm,
                double assumedVarA, double assumedVarB, double assumedLenghtAtMaturity) {
            this.surveyTag = surveyTag;
            this.speciesName = speciesName;
            this.samplingSelector = samplingSelector;
            this.assumedLinf = assumedLinf;
            this.assumedKParameter = assumedKParameter;
            this.assumedNaturalMortality = assumedNaturalMortality;
            this.assumedMaxAge = assumedMaxAge;
            this.assumedVirginRecruits = assumedVirginRecruits;
            this.assumedLengthBinCm = assumedLengthBinCm;
            this.assumedVarA = assumedVarA;
            this.assumedVarB = assumedVarB;
            this.assumedLenghtAtMaturity = assumedLenghtAtMaturity;
        }

        /**
         * Applies this function to the given argument.
         *
         * @param fishState the function argument
         * @return the function result
         */
        @Override
        public SPRAgent apply(FishState fishState) {
            return new SPRAgent(surveyTag,
                                fishState.getBiology().getSpecie(speciesName),
                                samplingSelector,
                                assumedLinf,
                                assumedKParameter,
                                assumedNaturalMortality,
                                assumedMaxAge,
                                assumedVirginRecruits,
                                assumedLengthBinCm,
                                assumedVarA,
                                assumedVarB,
                                assumedLenghtAtMaturity);
        }
    }

}
