/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments.noisespike;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * basically we throw a whole lot of runs at a set of acceptable ranges and we hope some pass, which we store
 */
public class HailMaryRuns {


    public static final int MAX_YEARS_TO_RUN = 50;
    /**
     * what changes
     */
    private static final List<SimpleOptimizationParameter> parameters = new LinkedList<>();
    private final static Path scenarioFile = Paths.get("docs", "20190129 spr_project", "three_years_to_quit.yaml");
    /**
     * what tells us if the result is good or crap
     */
    private static final List<AcceptableRangePredicate> predicates = new LinkedList<>();
    /**
     * here we store each sweep and the year it was first successfull
     */
    private final static Path outputFile = Paths.get("docs", "20190129 spr_project", "fiftyyears11.csv");
    private final static int NUMBER_OF_TRIES = 10000;

    static {
        //those I should really switch to optimization
        parameters.add(
            new SimpleOptimizationParameter("fisherDefinitions$0.gear.averageCatchability",
                0.00001, 0.001
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("fisherDefinitions$0.gear.selectivityAParameter",
                10, 40
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("fisherDefinitions$0.gear.selectivityBParameter",
                3, 15
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("plugins$0.profitRatioToEntrantsMultiplier",
                .1, 2
            )
        );
        //those that probably will remain noise
        parameters.add(
            new SimpleOptimizationParameter("fisherDefinitions$0.hourlyVariableCost",
                30000, 100000
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("market.marketPrice",
                30000, 60000
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("biologyInitializer.k",
                0.120, 0.5
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("biologyInitializer.LInfinity",
                70, 120
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("biologyInitializer.allometricAlpha",
                0.006824, 0.010236
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("biologyInitializer.allometricBeta",
                2.5096, 3.76
            )
        );
        //something like 0.8 to 1.8 of K
        parameters.add(
            new SimpleOptimizationParameter("biologyInitializer.yearlyMortality",
                0.248, 0.558
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("biologyInitializer.lengthAtMaturity",
                45, 70
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("biologyInitializer.virginRecruits",
                4000000, 12000000
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("biologyInitializer.cumulativePhi",
                2, 10
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("biologyInitializer.steepness",
                0.8, 0.95
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("fisherDefinitions$0.holdSize",
                5000, 10000
            )
        );
        parameters.add(
            new SimpleOptimizationParameter("fisherDefinitions$0.departingStrategy.decorated.maxHoursOut",
                180 * 24, 240 * 24
            )
        );


    }

    static {
        predicates.add(new AcceptableRangePredicate(
            0.05, 0.35, "Bt/K Red Fish"
        ));
        predicates.add(new AcceptableRangePredicate(
            2000000, 5000000, "Red Fish Landings"
        ));

    }

    public static void mainSweep(final String[] args) throws IOException {
        sweep(outputFile, parameters, scenarioFile, System.currentTimeMillis(),
            MAX_YEARS_TO_RUN, NUMBER_OF_TRIES
        );
    }

    public static void sweep(
        final Path outputFile,
        final List<SimpleOptimizationParameter> parameters,
        final Path scenarioFile,
        final long originalSeed, final int maxYearsToRun, final int numberOfTries
    ) throws IOException {

        final FileWriter writer = new FileWriter(outputFile.toFile());
        writer.write("seed");
        for (final SimpleOptimizationParameter parameter : HailMaryRuns.parameters) {
            writer.write(",");
            writer.write(parameter.getAddressToModify());
        }
        writer.write(",validyear");
        writer.write("\n");
        writer.flush();

        final MersenneTwisterFast fast = new MersenneTwisterFast(originalSeed);

        for (int i = 0; i < numberOfTries; i++) {

            final Map.Entry<Scenario, double[]> scenarioPair = setupScenario(fast, parameters, scenarioFile);
            final long seed = fast.nextLong();
            ((FlexibleScenario) scenarioPair.getKey()).setMapMakerDedicatedRandomSeed(seed);


            //run the model
            final FishState model = new FishState(seed);
            model.setScenario(scenarioPair.getKey());
            model.start();
            while (model.getYear() <= maxYearsToRun) {
                model.schedule.step(model);
            }
            model.schedule.step(model);

            int validYear;
            for (validYear = maxYearsToRun; validYear > 0; validYear--) {

                boolean valid = true;
                for (final AcceptableRangePredicate predicate : predicates) {
                    valid = valid & predicate.test(model, validYear);
                }
                System.out.println(validYear + " -- " + valid);


                if (valid)
                    break;


            }
            writer.write(String.valueOf(seed));
            for (final double value : scenarioPair.getValue()) {
                writer.write(",");
                writer.write(String.valueOf(value));
            }
            writer.write(",");
            writer.write(String.valueOf(validYear));
            writer.write("\n");

            writer.flush();

            final FileWriter scenarioSave = new FileWriter(outputFile.getParent()
                .resolve("dump")
                .resolve("test" + i + ".yaml")
                .toFile());
            final FishYAML yaml = new FishYAML();
            scenarioSave.write(yaml.dump(scenarioPair.getKey()));
            scenarioSave.flush();
            scenarioSave.close();

        }


        writer.flush();
        writer.close();


    }

    /**
     * reads scenario, builds random vector and passes it along
     *
     * @param random       randomizer needed
     * @param parameters   list of all parameters that will modify the scenario
     * @param scenarioFile scenario file to modify
     * @return a pair with the scenario object and the real values applied to its parameters
     * @throws FileNotFoundException
     */
    public static Map.Entry<Scenario, double[]> setupScenario(
        final MersenneTwisterFast random,
        final List<SimpleOptimizationParameter> parameters,
        final Path scenarioFile
    ) throws FileNotFoundException {

        final double[] randomValues = new double[parameters.size()];
        for (int i = 0; i < randomValues.length; i++) {
            randomValues[i] = random.nextDouble() * 20 - 10;
        }


        final FishYAML yaml = new FishYAML();
        final Scenario scenario = yaml.loadAs(new FileReader(scenarioFile.toFile()), Scenario.class);

        return setupScenario(scenario, randomValues, parameters, false);
    }


    public static Map.Entry<Scenario, double[]> setupScenario(
        final Scenario scenario,
        final double[] randomValues,
        final List<SimpleOptimizationParameter> parameters,
        //if this flag is true, you are passing real values
        final boolean realValues
    ) {

        Preconditions.checkState(parameters.size() == randomValues.length);
        final double[] values = new double[randomValues.length];
        for (int i = 0; i < randomValues.length; i++) {
            final double real;
            if (realValues) {
                real =
                    parameters.get(i).parametrizeRealValue(
                        scenario,
                        randomValues[i]
                    );
            } else {
                real = Double.parseDouble(parameters.get(i).parametrize(
                    scenario,
                    new double[]{randomValues[i]}
                ));
            }
            values[i] = real;
        }


        return entry(scenario, values);
    }

    public static void main(final String[] args) throws IOException {


        final List<String> columnsToPrint = Lists.newArrayList(
            "Bt/K Red Fish",
            "Red Fish Landings",
            "Average Cash-Flow",
            "Average Variable Costs",
            "Average Number of Trips",
            "Average Distance From Port",
            "Average Trip Duration",
            "SPR " + "Red Fish" + " " + "spr_agent",
            "Biomass " + "Red Fish",
            "Total Effort",
            "Actual Average Cash-Flow",
            "Number Of Active Fishers",
            "Average Hours Out"


        );
//
//        HailMaryRuns.runPolicyAnalysis(
//                Paths.get("docs", "20190129 spr_project", "policy", "fiftyyearsinputs.csv"),
//                0, 2, 1,
//                new Consumer<FishState>() {
//                    @Override
//                    public void accept(FishState state) {
//
//                    }
//                },
//                parameters,
//                "fiftyyears_no_change",
//                scenarioFile,
//                Paths.get("docs", "20190129 spr_project", "policy"),
//                columnsToPrint,
//                MAX_YEARS_TO_RUN+10
//        );

        HailMaryRuns.runPolicyAnalysis(
            Paths.get("docs", "20190129 spr_project", "policy", "fiftyyearsinputs.csv"),
            0, 2, 1,
            state -> {


                final FishingSeasonFactory season = new FishingSeasonFactory(120, true);
                //new fishers are also only allowed 5 days at sea
                for (final Map.Entry<String, FisherFactory> fisherFactory : state.getFisherFactories()) {
                    fisherFactory.getValue().setRegulations(new FishingSeasonFactory(0, false));
                }

                for (final Fisher fisher : state.getFishers()) {
                    fisher.setRegulation(season.apply(state));
                }
            },
            parameters,
            "fiftyyears_120_days_noentry",
            scenarioFile,
            Paths.get("docs", "20190129 spr_project", "policy"),
            columnsToPrint,
            MAX_YEARS_TO_RUN + 10
        );


//
//        HailMaryRuns.runPolicyAnalysis(
//                Paths.get("docs", "20190129 spr_project", "policy", "fiftyyearsinputs.csv"),
//                0, 2, 1,
//                new Consumer<FishState>() {
//                    @Override
//                    public void accept(FishState state) {
//
//                        for (Port port : state.getPorts()) {
//                            ((FixedPriceMarket) port.getDefaultMarketMap().
//                                    getMarket(state.getBiology().getSpecie("Red Fish"))).setPrice(20000);
//                        }
//
//                    }
//                },
//                parameters,
//                "fiftyyears_tax",
//                scenarioFile,
//                Paths.get("docs", "20190129 spr_project", "policy"),
//                columnsToPrint,
//                MAX_YEARS_TO_RUN+10
//        );
//
//                HailMaryRuns.runPolicyAnalysis(
//                Paths.get("docs", "20190129 spr_project", "policy", "inputs.csv"),
//                0, 2, 1,
//                new Consumer<FishState>() {
//                    @Override
//                    public void accept(FishState state) {
//
//                        //new fishers aren't allowed out!
//                        for (Map.Entry<String, FisherFactory> fisherFactory : state.getFisherFactories()) {
//                            fisherFactory.getValue().setRegulations(new FishingSeasonFactory(0,true));
//                        }
//
//                        //all the other fishers are only allowed 100 days out
//                        for (Fisher fisher : state.getFishers()) {
//                            fisher.setRegulation(new FishingSeason(true,
//                                                                   100));
//                        }
//
//                    }
//                },
//                parameters,
//                "noentry_hundred_days",
//                scenarioFile,
//                Paths.get("docs", "20190129 spr_project", "policy"),
//                columnsToPrint,
//                MAX_YEARS_TO_RUN+10
//        );
//
//    }


//        HailMaryRuns.runPolicyAnalysis(
//                Paths.get("docs", "20190129 spr_project", "policy", "inputs.csv"),
//                0, 2, 1,
//                new Consumer<FishState>() {
//                    @Override
//                    public void accept(FishState state) {
//
//                        //new fishers aren't allowed out!
//                        for (Map.Entry<String, FisherFactory> fisherFactory : state.getFisherFactories()) {
//                            fisherFactory.getValue().setRegulations(new FishingSeasonFactory(0,true));
//                        }
//
//                        //all the other fishers are only allowed 100 days out
//                        for (Fisher fisher : state.getFishers()) {
//                            fisher.setRegulation(new FishingSeason(true,
//                                                                   150));
//                        }
//
//                    }
//                },
//                parameters,
//                "noentry_150_days",
//                scenarioFile,
//                Paths.get("docs", "20190129 spr_project", "policy"),
//                columnsToPrint,
//                MAX_YEARS_TO_RUN+10
//        );
//
//        HailMaryRuns.runPolicyAnalysis(
//                Paths.get("docs", "20190129 spr_project", "policy", "inputs.csv"),
//                0, 2, 1,
//                new Consumer<FishState>() {
//                    @Override
//                    public void accept(FishState state) {
//
//                        //new fishers aren't allowed out!
//                        for (Map.Entry<String, FisherFactory> fisherFactory : state.getFisherFactories()) {
//                            fisherFactory.getValue().setRegulations(new FishingSeasonFactory(0,true));
//                        }
//
//                        //all the other fishers are only allowed 100 days out
//                        for (Fisher fisher : state.getFishers()) {
//                            fisher.setRegulation(new FishingSeason(true,
//                                    180));
//                        }
//
//                    }
//                },
//                parameters,
//                "noentry_180_days",
//                scenarioFile,
//                Paths.get("docs", "20190129 spr_project", "policy"),
//                columnsToPrint,
//                MAX_YEARS_TO_RUN+10
//        );


    }

    static public void runPolicyAnalysis(
        final Path fileWithAcceptableParameters,
        final int groupColumnIndex,
        final int randomSeedColumnIndex,
        final int yearsToRunColumn,
        final Consumer<FishState> policyToImplement,
        final List<SimpleOptimizationParameter> parameterObjects,
        final String simulationTitle,
        final Path scenarioFile,
        final Path outputFolder,
        final List<String> columnsToPrint,
        final int maxYearsToRun
    ) throws IOException {


        System.out.println("Starting: " + simulationTitle);
        final FileWriter fileWriter = new FileWriter(outputFolder.resolve(simulationTitle + ".csv").toFile());
        fileWriter.write("run,year,group,seed,shock_year,variable,value\n");
        fileWriter.flush();

        //open the file with acceptable parameters
        final List<String> parameterFile = Files.readAllLines(fileWithAcceptableParameters);
        //first column I assume it's a header
        final String header = parameterFile.get(0);
        final String[] columns = header.split(",");
        //one column is the group, one column is the years to run, one column is the random seed;
        //the rest are all parameters
        Preconditions.checkArgument(columns.length > 3);

        //here we store the real parameters
        final double[][] parameters = new double[parameterFile.size() - 1][columns.length - 3];
        //here we store the group associated with each column
        final int[] groups = new int[parameterFile.size() - 1];
        final long[] randomSeeds = new long[parameterFile.size() - 1]; //random seeds here
        final int[] yearsToPolicy = new int[parameterFile.size() - 1];

        //fill up the parameters now
        parameterFile.remove(0); //pop the header out
        for (int row = 0; row < parameterFile.size(); row++) {

            final String[] splitRow = parameterFile.get(row).split(",");
            Preconditions.checkArgument(splitRow.length == parameters[0].length + 3);

            int columnsAlreadyFilled = 0;
            for (int column = 0; column < splitRow.length; column++)
                if (column == groupColumnIndex) {
                    groups[row] = Integer.parseInt(splitRow[column]);

                } else if (column == randomSeedColumnIndex) {
                    randomSeeds[row] = Long.parseLong(splitRow[column]);
                } else if (column == yearsToRunColumn) {
                    yearsToPolicy[row] = Integer.parseInt(splitRow[column]);
                } else {
                    parameters[row][columnsAlreadyFilled] = Double.parseDouble(splitRow[column]);
                    columnsAlreadyFilled++;
                }

            Preconditions.checkState(
                columnsAlreadyFilled == parameters[0].length,
                "failed to fill all columns in row " + row
            );

        }


        final BatchRunner runner = new BatchRunner(
            scenarioFile,
            maxYearsToRun,
            columnsToPrint,
            outputFolder,
            null,
            System.currentTimeMillis(),
            -1

        );
        runner.setColumnModifier((writer, model, year) -> writer.append(groups[runner.getRunsDone()]).append(",").
            append(randomSeeds[runner.getRunsDone()]).append(",").
            append(yearsToPolicy[runner.getRunsDone()]).append(","));

        final Consumer<Scenario> scenarioConsumer = scenario -> {
            //first, let's make sure we set up the right seed
            final int currentRow = runner.getRunsDone();
            ((FlexibleScenario) scenario).setMapMakerDedicatedRandomSeed(randomSeeds[currentRow]);
            //then let's update all the parameters
            setupScenario(scenario, parameters[currentRow], parameterObjects, true);
            //then schedule yourself to change policy at the right year
            ((FlexibleScenario) scenario).getPlugins().add(state -> new AdditionalStartable() {
                @Override
                public void start(final FishState model) {
                    state.scheduleOnceAtTheBeginningOfYear(
                        (Steppable) simState -> policyToImplement.accept(((FishState) simState)),
                        StepOrder.DAWN,
                        yearsToPolicy[currentRow] + 1
                    );
                }

                @Override
                public void turnOff() {
                    //YOU FOOLS I CANNOT EVER BE STOPPED
                }
            });


        };

        runner.setScenarioSetup(scenarioConsumer);

        //ugh done

        for (int runs = 0; runs < parameters.length; runs++) {
            final StringBuffer tidy = new StringBuffer();
            runner.run(tidy);
            fileWriter.write(tidy.toString());
            fileWriter.flush();
        }

        fileWriter.close();


    }

}
