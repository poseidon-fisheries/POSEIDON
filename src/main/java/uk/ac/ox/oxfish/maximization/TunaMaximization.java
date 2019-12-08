package uk.ac.ox.oxfish.maximization;

import com.google.common.collect.ImmutableList;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getLast;
import static java.util.Arrays.stream;

public class TunaMaximization {

    static final String BASE_CALIBRATION_FOLDER_NAME = "/home/nicolas/workspace/tuna/np/calibrations/";
    static final String logFileName = "log_calibration.log";
    static final String optimizationFileName = "calibration.yaml";
    static final String calibratedScenarioFileName = "tuna_calibrated.yaml";
    static final String resultsFileName = "results.txt";
    static boolean outputToFile = true;

    public static void main(String[] args) {
        final ImmutableList<String> calibrationFolderNames = ImmutableList.of(
//            "2019-12-06_1-normal_biomass-normal_costs",
//            "2019-12-06_2-normal_biomass-zero_costs",
//            "2019-12-06_3-virgin_biomass_normal_costs",
//            "2019-12-06_4-virgin_biomass_zero_costs",
//            "2019-12-06_5-free_skj",
            "2019-12-06_6-free_skj_landings_only"
        );

        calibrationFolderNames.forEach(TunaMaximization::evaluateCalibration);
    }

    static double[] optimalParamsFromFile(Path logFile) {
        try {
            final List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
            final Matcher matcher = Pattern.compile("\\{(.*)}").matcher(getLast(lines));
            checkState(matcher.find());
            final String[] strings = matcher.group(1).split(",");
            return stream(strings).mapToDouble(Double::parseDouble).toArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void evaluateCalibration(String calibrationFolderName) {

        Path calibrationFolder = Paths.get(BASE_CALIBRATION_FOLDER_NAME, calibrationFolderName);
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
            System.out.println("POSEIDON commit: " + new FileRepository("./.git").resolve(Constants.HEAD).getName());
            optimization.evaluate(optimalParameters);
            System.setOut(stdOut);
            System.out.println("Done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
