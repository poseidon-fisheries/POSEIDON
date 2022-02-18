package uk.ac.ox.oxfish.maximization;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.primitives.ImmutableIntArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.stream;

public class TunaCalibrationConsole  {



    @Parameter(
            names = "--nickname",
            description = "A nickname for the folder containing the outputs",
            required = false,
            arity = 1
    )
    private String runNickName = null;

    @Parameter(
            names = "--path",
            description = "Path to calibration.yaml",
            required = false,
            arity = 1
    )
    private String pathToCalibrationYaml = null;

    @Parameter(
            names = "--pop",
            description = "Size of optimizer population size"
    )
    private int populationSize = TunaCalibrator.DEFAULT_POPULATION_SIZE;

    @Parameter(
            names = "--maxProcs",
            description = "maximum number of processors"
    )
    private int maxProcessorsToUse = TunaCalibrator.MAX_PROCESSORS_TO_USE;

    @Parameter(
            names = "--runs",
            description = "maximum number of fitness calls"
    )
    private int maxFitnessCalls = TunaCalibrator.MAX_FITNESS_CALLS;

    @Parameter(
            names = "--range",
            description = "EVA range of parameters, by default this is 10"
    )
    private int parameterRange=  TunaCalibrator.DEFAULT_RANGE;

    @Parameter(
            names ="--local",
            description = "when true this is a Nelder-Mead search"

    )
    private boolean isLocalSearch = false;

    @Parameter(
            names ="--initialGuessesTextFile",
            description = "path to text file where each line is an individual (comma separated double array) we need to add in the first population"

    )
    private String bestGuessesTextFile = null;

    @Parameter(
            names ="--runsPerSetting",
            description = "overrides the number of simulations per parameter specified by the yaml file with argument"

    )
    private int numberOfRunsPerSettingOverride = -1;

    public static void main(String[] args) throws IOException {
        //parse all arguments
        TunaCalibrationConsole arguments = new TunaCalibrationConsole();
        JCommander commander = JCommander.newBuilder().addObject(arguments).build();
        commander.parse(args);


        //build calibrator and feed it the arguments
        final TunaCalibrator tunaCalibrator = new TunaCalibrator();
        tunaCalibrator.setMaxProcessorsToUse(arguments.getMaxFitnessCalls());
        tunaCalibrator.setPopulationSize(arguments.getPopulationSize());
        if(arguments.pathToCalibrationYaml != null && !arguments.pathToCalibrationYaml.trim().isEmpty())
            tunaCalibrator.setOriginalCalibrationFilePath(Paths.get(arguments.getPathToCalibrationYaml()));

        tunaCalibrator.setRunNickName(arguments.getRunNickName());
        tunaCalibrator.setParameterRange(arguments.getParameterRange());
        tunaCalibrator.setLocalSearch(arguments.isLocalSearch());
        tunaCalibrator.setMaxProcessorsToUse(arguments.getMaxProcessorsToUse());
        tunaCalibrator.setNumberOfRunsPerSettingOverride(arguments.getNumberOfRunsPerSettingOverride());


        //add initial guesses if provided
        tunaCalibrator.getBestGuess().clear();
        if(arguments.getBestGuessesTextFile() != null && !arguments.getBestGuessesTextFile().trim().isEmpty()){
            List<String> allLines = Files.readAllLines(Paths.get(arguments.getBestGuessesTextFile()));
            List<double[]> individuals = new LinkedList<>();
            for (String readLine : allLines) {
                if(readLine.trim().isEmpty())
                    continue;
                double[] individual = stream(readLine.trim().split(","))
                        .mapToDouble(Double::parseDouble)
                        .toArray();
                individuals.add(individual);

            }
            tunaCalibrator.setBestGuess(individuals);
            
        }
        System.out.println(tunaCalibrator.isLocalSearch());
        System.out.println(tunaCalibrator.getBestGuess());
        //run the bastard
        tunaCalibrator.run();


    }


    public String getRunNickName() {
        return runNickName;
    }

    public String getPathToCalibrationYaml() {
        return pathToCalibrationYaml;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getMaxFitnessCalls() {
        return maxFitnessCalls;
    }

    public int getParameterRange() {
        return parameterRange;
    }

    public boolean isLocalSearch() {
        return isLocalSearch;
    }

    public void setRunNickName(String runNickName) {
        this.runNickName = runNickName;
    }

    public void setPathToCalibrationYaml(String pathToCalibrationYaml) {
        this.pathToCalibrationYaml = pathToCalibrationYaml;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public void setMaxFitnessCalls(int maxFitnessCalls) {
        this.maxFitnessCalls = maxFitnessCalls;
    }

    public void setParameterRange(int parameterRange) {
        this.parameterRange = parameterRange;
    }

    public void setLocalSearch(boolean localSearch) {
        isLocalSearch = localSearch;
    }

    public String getBestGuessesTextFile() {
        return bestGuessesTextFile;
    }

    public void setBestGuessesTextFile(String bestGuessesTextFile) {
        this.bestGuessesTextFile = bestGuessesTextFile;
    }

    public int getMaxProcessorsToUse() {
        return maxProcessorsToUse;
    }

    public void setMaxProcessorsToUse(int maxProcessorsToUse) {
        this.maxProcessorsToUse = maxProcessorsToUse;
    }

    public int getNumberOfRunsPerSettingOverride() {
        return numberOfRunsPerSettingOverride;
    }

    public void setNumberOfRunsPerSettingOverride(int numberOfRunsPerSettingOverride) {
        this.numberOfRunsPerSettingOverride = numberOfRunsPerSettingOverride;
    }
}
