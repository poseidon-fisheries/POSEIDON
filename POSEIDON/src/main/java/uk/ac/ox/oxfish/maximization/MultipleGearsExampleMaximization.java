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

package uk.ac.ox.oxfish.maximization;

import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleGearsExampleMaximization extends SimpleProblemDouble {


    private static final Path DEFAULT_PATH = Paths.get("docs",
                                                       "indonesia_hub",
                                                       "runs","712","slice0","calibration");

    private String scenarioFile =
            DEFAULT_PATH.resolve("712_optimistic_2014_perfect.yaml").toString();


    private String summaryFile =
            DEFAULT_PATH.resolve("summary_2014.csv").toString();

    private String landingData =
            DEFAULT_PATH.resolve("landings4.csv").toString();

    private long seed = 0;


    // an input of +10 means this is the catchability
    private double maxCatchability =  0.005;

    //an input of -10 means this catchability
    private double minCatchability =  0.00001;

    private int populations = 3;

    private int yearsToIgnore = 0;


    private int runsPerSetting = 1;
    private String speciesName = "Snapper";

    @Override
    public double[] evaluate(double[] x) {


        try {

            double error = 0;

            //read landings
            List<String> lines = Files.readAllLines(Paths.get(landingData));
            Double[][] landings = new Double[populations][lines.size()];
            for(int i=0; i<lines.size(); i++)
            {
                String[] split = lines.get(i).split(",");
                for(int population=0; population<populations; population++)
                    landings[population][i] = Double.parseDouble(split[population]);
            }


            for (int i = 0; i < runsPerSetting; i++) {
                FlexibleScenario scenario = buildInput(x);


                FishState model = new FishState(System.currentTimeMillis());
                model.setScenario(scenario);
                model.start();
                System.out.println("starting run");
                while (model.getYear() < yearsToIgnore + lines.size()) {
                    model.schedule.step(model);
                }
                model.schedule.step(model);

                if(populations>1)
                for(int population = 0; population<populations; population++)
                {
                    List<Double> simulatedLandings = model.getYearlyDataSet().getColumn(
                            speciesName + " Landings of population"+population
                    ).stream().collect(Collectors.toList());
                    for(int j=0; j<yearsToIgnore; j++) //remove years to ignore!
                        simulatedLandings.remove(0);

                    error+= FishStateUtilities.timeSeriesDistance(
                            simulatedLandings,
                            Arrays.asList(landings[population]), 1,
                            false);
                }
                else{
                    List<Double> simulatedLandings = model.getYearlyDataSet().getColumn(
                            speciesName + " Landings").stream().collect(Collectors.toList());

                    for(int j=0; j<yearsToIgnore; j++) //remove years to ignore!
                        simulatedLandings.remove(0);

                    error+= FishStateUtilities.timeSeriesDistance(
                            simulatedLandings,
                            Arrays.asList(landings[0]), 1,
                            false);
                }




            }


            double averageError = error / ((double) runsPerSetting * (double) populations);

            //write summary file
            Files.write(
                    Paths.get(summaryFile),
                    (averageError +"," + Arrays.toString(x).
                            replace("[","").
                            replace("]","") +"\n").getBytes(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );

            return new double[]{
                    averageError};
        }
        catch (IOException exception){
            throw new RuntimeException("failed to read or deal with files!");
        }
    }

    public FlexibleScenario buildInput(double[] x) throws FileNotFoundException {
        FishYAML yaml = new FishYAML();
        Path scenarioPath = Paths.get(scenarioFile);


        FlexibleScenario scenario = yaml.loadAs(
                new FileReader(scenarioPath.toFile()),
                FlexibleScenario.class);


        assert x.length==populations;
        for(int k=0; k<x.length; k++)
        {
            double catchability = ((x[k]+10)/20)*(maxCatchability-minCatchability);
            FisherDefinition definition = scenario.getFisherDefinitions().get(k);
            ((RandomCatchabilityTrawlFactory) definition.getGear()).setMeanCatchabilityFirstSpecies(
                    new FixedDoubleParameter(catchability)
            );

        }
        return scenario;
    }


//    public static void main(String[] args) throws IOException {
//        MultipleGearsExampleMaximization maximization = new MultipleGearsExampleMaximization();
//        maximization.setScenarioFile("./docs/indonesia_hub/runs/712/slice0/calibration/712_pessimistic_2014_perfect.yaml");
//        FlexibleScenario flexibleScenario = maximization.buildInput(new double[]{-8.500,-3.312,-5.292});
//        FishYAML yaml = new FishYAML();
//        yaml.dump(flexibleScenario,
//                  new FileWriter(DEFAULT_PATH.resolve("712_pessimistic_perfect_calibrated_2014.yaml").toFile()));
//    }


    @Override
    public int getProblemDimension() {
        return populations;
    }


    public String getScenarioFile() {
        return scenarioFile;
    }

    public void setScenarioFile(String scenarioFile) {
        this.scenarioFile = scenarioFile;
    }

    public String getSummaryFile() {
        return summaryFile;
    }

    public void setSummaryFile(String summaryFile) {
        this.summaryFile = summaryFile;
    }

    public String getLandingData() {
        return landingData;
    }

    public void setLandingData(String landingData) {
        this.landingData = landingData;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public double getMaxCatchability() {
        return maxCatchability;
    }

    public void setMaxCatchability(double maxCatchability) {
        this.maxCatchability = maxCatchability;
    }

    public double getMinCatchability() {
        return minCatchability;
    }

    public void setMinCatchability(double minCatchability) {
        this.minCatchability = minCatchability;
    }



    public int getYearsToIgnore() {
        return yearsToIgnore;
    }

    public void setYearsToIgnore(int yearsToIgnore) {
        this.yearsToIgnore = yearsToIgnore;
    }

    public int getRunsPerSetting() {
        return runsPerSetting;
    }

    public void setRunsPerSetting(int runsPerSetting) {
        this.runsPerSetting = runsPerSetting;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public int getPopulations() {
        return populations;
    }

    public void setPopulations(int populations) {
        this.populations = populations;
    }
}
