package uk.ac.ox.oxfish.maximization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import uk.ac.ox.oxfish.maximization.generic.FixedDataLastStepTarget;
import uk.ac.ox.oxfish.maximization.generic.ScaledFixedDataLastStepTarget;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.stream;

public class TunaMaximization {

    static final String baseCalibrationFolderName = "/home/nicolas/workspace/tuna/np/calibrations/";
    static final String logFileName = "log_calibration.log";
    static final String optimizationFileName = "calibration.yaml";
    static final String calibratedScenarioFileName = "tuna_calibrated.yaml";
    static final String resultsFileName = "results.txt";
    static boolean outputToFile = true;

    public static void main(String[] args) {
        final ImmutableList<String> calibrationFolderNames = ImmutableList.of(
            "2019-12-09-normal_biomass_normal_costs"
        );

        calibrationFolderNames.forEach(TunaMaximization::evaluateCalibration);
    }

    static void evaluateCalibration(String calibrationFolderName) {

        Path calibrationFolder = Paths.get(baseCalibrationFolderName, calibrationFolderName);
        System.out.println("Evaluating calibration from folder " + calibrationFolder);

        Path logFile = calibrationFolder.resolve(logFileName);
        File optimizationFile = calibrationFolder.resolve(optimizationFileName).toFile();
        File calibratedScenarioFile = calibrationFolder.resolve(calibratedScenarioFileName).toFile();
        File resultsFile = calibrationFolder.resolve(resultsFileName).toFile();

        double[] optimalParameters = optimalParamsFromFile(logFile);
        FishYAML yaml = new FishYAML();

        try {
            GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile), GenericOptimization.class);
            Scenario scenario = optimization.buildScenario(optimalParameters);
            yaml.dump(scenario, new FileWriter(calibratedScenarioFile));
            ScaledFixedDataLastStepTarget.VERBOSE = true;
            FixedDataLastStepTarget.VERBOSE = true;
            PrintStream stdOut = System.out;
            if (outputToFile) System.setOut(new PrintStream(new FileOutputStream(resultsFile)));
            optimization.evaluate(optimalParameters);
            System.setOut(stdOut);
            System.out.println("Done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static double[] optimalParamsFromFile(Path logFile) {
        try {
            final Stream<String> lines = Files
                .readAllLines(logFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.trim().isEmpty());
            final String lastLine = Streams
                .findLast(lines)
                .orElseThrow(() -> new RuntimeException("Empty calibration log file!"));
            final Matcher matcher = Pattern.compile("\\{(.*)}").matcher(lastLine);
            checkState(matcher.find());
            final String[] strings = matcher.group(1).split(",");
            return stream(strings).mapToDouble(Double::parseDouble).toArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
