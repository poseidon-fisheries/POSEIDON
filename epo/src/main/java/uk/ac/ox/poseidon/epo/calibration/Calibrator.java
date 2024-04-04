/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024-2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.epo.calibration;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.google.common.collect.ImmutableList;
import eva2.OptimizerFactory;
import eva2.OptimizerRunnable;
import eva2.optimization.OptimizationParameters;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.terminators.EvaluationTerminator;
import eva2.optimization.population.Population;
import eva2.optimization.statistics.InterfaceStatisticsParameters;
import eva2.optimization.strategies.AbstractOptimizer;
import eva2.optimization.strategies.ClusterBasedNichingEA;
import eva2.optimization.strategies.NelderMeadSimplex;
import eva2.optimization.strategies.ParticleSwarmOptimizationGCPSO;
import eva2.problems.SimpleProblemWrapper;
import uk.ac.ox.oxfish.maximization.FileAndScreenWriter;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.LastStepFixedDataTarget;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.IntFunction;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Runtime.getRuntime;
import static java.nio.file.Files.createDirectories;
import static uk.ac.ox.oxfish.maximization.BoundsWriter.writeBounds;

public class Calibrator implements JCommanderRunnable {
    public static final String CALIBRATION_FILENAME = "calibration.yaml";
    public static final String CALIBRATED_SCENARIO_FILENAME = "calibrated_scenario.yaml";
    public static final String BOUNDS_FILENAME = "bounds.csv";
    private static final Logger logger = Logger.getLogger(Calibrator.class.getName());
    private static final String LOG_FILENAME = "calibration_log.md";
    @Parameter(names = "--prefix")
    private String runFolderPrefix = null;
    @Parameter(names = {"-n", "--population_size"})
    private int populationSize = 100;
    @Parameter(names = {"-c", "--max_fitness_calls"})
    private int maxFitnessCalls = 2000;
    @Parameter(names = {"-r", "--parameter_range"})
    private int parameterRange = 10;
    @Parameter(names = {"-p", "--parallel_threads"})
    private int parallelThreads = getRuntime().availableProcessors();
    @Parameter(names = {"-v", "--verbose"})
    private boolean verbose = false;
    @Parameter(converter = PathConverter.class)
    private Path rootCalibrationFolder;
    @Parameter(names = {"-f", "--calibration_file"}, converter = PathConverter.class)
    private Path calibrationFile = Paths.get(CALIBRATION_FILENAME);
    @Parameter(names = {"-s", "--seed_scenarios"}, converter = PathConverter.class)
    private List<Path> seedScenarios = ImmutableList.of();
    @Parameter(names = {"-o", "--optimizer"}, converter = OptimizerInitializerConverter.class)
    private OptimizerInitializer optimizerInitializer = new ClusterBasedNichingEAInitializer();

    public Calibrator() {
    }

    public Calibrator(
        final String runFolderPrefix,
        final int populationSize,
        final int maxFitnessCalls,
        final int parameterRange,
        final int parallelThreads,
        final boolean verbose,
        final Path rootCalibrationFolder,
        final Path calibrationFile,
        final List<Path> seedScenarios,
        final OptimizerInitializer optimizerInitializer
    ) {
        this.runFolderPrefix = runFolderPrefix;
        this.populationSize = populationSize;
        this.maxFitnessCalls = maxFitnessCalls;
        this.parameterRange = parameterRange;
        this.parallelThreads = parallelThreads;
        this.verbose = verbose;
        this.rootCalibrationFolder = rootCalibrationFolder;
        this.calibrationFile = calibrationFile;
        this.seedScenarios = seedScenarios;
        this.optimizerInitializer = optimizerInitializer;
    }

    public static void main(final String[] args) {
        new Calibrator().run(args);
    }

    private static void saveCalibrationFile(
        final GenericOptimization optimizationProblem,
        final Path calibrationFile
    ) {
        try (final FileWriter fileWriter = new FileWriter(calibrationFile.toFile())) {
            new FishYAML().dump(optimizationProblem, fileWriter);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static OptimizerRunnable makeOptimizerRunnable(
        final OptimizationParameters optimizationParameters,
        final Path logFilePath
    ) {
        final OptimizerRunnable runnable = new OptimizerRunnable(optimizationParameters, "");
        runnable.setOutputFullStatsToText(true);
        runnable.setVerbosityLevel(InterfaceStatisticsParameters.OutputVerbosity.ALL);
        runnable.setOutputTo(InterfaceStatisticsParameters.OutputTo.WINDOW);

        try (
            final FileAndScreenWriter fileAndScreenWriter = new FileAndScreenWriter(logFilePath)
        ) {
            runnable.setTextListener(fileAndScreenWriter);
            runnable.run();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return runnable;
    }

    @SuppressWarnings("unused")
    public List<Path> getSeedScenarios() {
        return seedScenarios;
    }

    @SuppressWarnings("unused")
    public void setSeedScenarios(final List<Path> seedScenarios) {
        this.seedScenarios = checkNotNull(seedScenarios);
    }

    @SuppressWarnings("unused")
    public int getParallelThreads() {
        return parallelThreads;
    }

    @SuppressWarnings("unused")
    public void setParallelThreads(final int parallelThreads) {
        this.parallelThreads = parallelThreads;
    }

    @Override
    public void run() {
        calibrate();
    }

    public Result calibrate() {
        return calibrate(rootCalibrationFolder.resolve(calibrationFile));
    }

    public Result calibrate(final Path calibrationFile) {
        return calibrate(GenericOptimization.fromFile(calibrationFile));
    }

    public Result calibrate(final GenericOptimization optimizationProblem) {
        final Path runFolder = makeRunFolder();
        final Path calibrationFile = runFolder.resolve(CALIBRATION_FILENAME);
        final Path calibratedScenarioFile = runFolder.resolve(CALIBRATED_SCENARIO_FILENAME);
        final Path boundsFile = runFolder.resolve(BOUNDS_FILENAME);
        final Path logFile = runFolder.resolve(LOG_FILENAME);
        saveCalibrationFile(optimizationProblem, calibrationFile);
        final double[] solution = computeSolution(optimizationProblem, logFile);
        saveCalibratedScenario(optimizationProblem, solution, calibratedScenarioFile);
        writeBounds(optimizationProblem, solution, boundsFile);
        return new Result(
            calibrationFile,
            calibratedScenarioFile,
            logFile,
            boundsFile,
            solution
        );
    }

    private Path makeRunFolder() {
        final StringBuilder runFolderBuilder = new StringBuilder();
        if (runFolderPrefix != null) {
            runFolderBuilder.append(runFolderPrefix.trim()).append("_");
        }
        runFolderBuilder.append(new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()));
        final Path runFolder = rootCalibrationFolder.resolve(runFolderBuilder.toString());
        try {
            createDirectories(runFolder);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return runFolder;
    }

    private double[] computeSolution(
        final GenericOptimization optimizationProblem,
        final Path logFile
    ) {

        optimizationProblem.getTargets().stream()
            .filter(LastStepFixedDataTarget.class::isInstance)
            .forEach(target -> ((LastStepFixedDataTarget) target).setVerbose(verbose));

        final SimpleProblemWrapper problemWrapper = new ProblemWrapper();
        problemWrapper.setSimpleProblem(optimizationProblem);
        problemWrapper.setParallelThreads(parallelThreads);

        final AbstractOptimizer optimizer = optimizerInitializer.apply(populationSize);
        problemWrapper.setDefaultRange(parameterRange);

        final OptimizationParameters optimizationParameters =
            OptimizerFactory.makeParams(
                optimizer,
                populationSize,
                problemWrapper,
                System.currentTimeMillis(),
                new EvaluationTerminator(maxFitnessCalls)
            );
        final OptimizerRunnable runnable =
            makeOptimizerRunnable(
                optimizationParameters,
                logFile
            );

        return runnable.getDoubleSolution();
    }

    private void saveCalibratedScenario(
        final GenericOptimization optimizationProblem,
        final double[] optimalParameters,
        final Path calibratedScenarioFile
    ) {
        try (final FileWriter fileWriter = new FileWriter(calibratedScenarioFile.toFile())) {
            final Scenario scenario = GenericOptimization.buildScenario(
                optimalParameters,
                Paths.get(optimizationProblem.getScenarioFile()).toFile(),
                optimizationProblem.getParameters()
            );
            new FishYAML().dump(scenario, fileWriter);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    public Path getRootCalibrationFolder() {
        return rootCalibrationFolder;
    }

    @SuppressWarnings("unused")
    public void setRootCalibrationFolder(final Path rootCalibrationFolder) {
        this.rootCalibrationFolder = checkNotNull(rootCalibrationFolder);
    }

    @SuppressWarnings("unused")
    public Path getCalibrationFile() {
        return calibrationFile;
    }

    @SuppressWarnings("unused")
    public void setCalibrationFile(final Path calibrationFile) {
        this.calibrationFile = checkNotNull(calibrationFile);
    }

    @SuppressWarnings("unused")
    public OptimizerInitializer getOptimizerInitializer() {
        return optimizerInitializer;
    }

    @SuppressWarnings("unused")
    public void setOptimizerInitializer(final OptimizerInitializer optimizerInitializer) {
        this.optimizerInitializer = checkNotNull(optimizerInitializer);
    }

    @SuppressWarnings("unused")
    public boolean isVerbose() {
        return verbose;
    }

    @SuppressWarnings("unused")
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    @SuppressWarnings("unused")
    public int getPopulationSize() {
        return populationSize;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setPopulationSize(final int populationSize) {
        this.populationSize = populationSize;
    }

    @SuppressWarnings("unused")
    public int getMaxFitnessCalls() {
        return maxFitnessCalls;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public void setMaxFitnessCalls(final int maxFitnessCalls) {
        this.maxFitnessCalls = maxFitnessCalls;
    }

    @SuppressWarnings("unused")
    public String getRunFolderPrefix() {
        return runFolderPrefix;
    }

    @SuppressWarnings("unused")
    void setRunFolderPrefix(final String runFolderPrefix) {
        this.runFolderPrefix = runFolderPrefix;
    }

    @SuppressWarnings("unused")
    public int getParameterRange() {
        return parameterRange;
    }

    @SuppressWarnings("unused")
    void setParameterRange(final int parameterRange) {
        this.parameterRange = parameterRange;
    }

    public interface OptimizerInitializer extends IntFunction<AbstractOptimizer> {}

    static class OptimizerInitializerConverter implements IStringConverter<OptimizerInitializer> {

        @Override
        public OptimizerInitializer convert(final String optimizerName) {
            switch (optimizerName) {
                case "NM":
                    return new NelderMeadSimplexInitializer();
                case "PSO":
                    return new ParticleSwarmOptimizationGCPSOInitializer();
                case "EA":
                    return new ClusterBasedNichingEAInitializer();
                default:
                    throw new IllegalArgumentException("Unknown optimizer: " + optimizerName);
            }
        }
    }

    static class NelderMeadSimplexInitializer implements OptimizerInitializer {
        @Override
        public AbstractOptimizer apply(final int populationSize) {
            return new NelderMeadSimplex(populationSize);
        }
    }

    static class ParticleSwarmOptimizationGCPSOInitializer implements OptimizerInitializer {
        @Override
        public AbstractOptimizer apply(final int populationSize) {
            final ParticleSwarmOptimizationGCPSO particleSwarmOptimizationGCPSO = new ParticleSwarmOptimizationGCPSO();
            particleSwarmOptimizationGCPSO.setCheckRange(false);
            particleSwarmOptimizationGCPSO.setGcpso(true);
            return particleSwarmOptimizationGCPSO;
        }
    }

    static class ClusterBasedNichingEAInitializer implements OptimizerInitializer {
        @Override
        public AbstractOptimizer apply(final int populationSize) {
            final ClusterBasedNichingEA clusterBasedNichingEA = new ClusterBasedNichingEA();
            clusterBasedNichingEA.setPopulationSize(populationSize);
            return clusterBasedNichingEA;
        }
    }

    public static class Result {
        private final Path calibrationFile;
        private final Path calibratedScenarioFile;
        private final Path logFile;
        private final Path boundsFile;
        private final double[] solution;

        public Result(
            final Path calibrationFile,
            final Path calibratedScenarioFile,
            final Path logFile,
            final Path boundsFile,
            final double[] solution
        ) {
            this.calibrationFile = calibrationFile;
            this.calibratedScenarioFile = calibratedScenarioFile;
            this.logFile = logFile;
            this.boundsFile = boundsFile;
            this.solution = solution;
        }

        @SuppressWarnings("unused")
        public Path getCalibrationFile() {
            return calibrationFile;
        }

        public Path getCalibratedScenarioFile() {
            return calibratedScenarioFile;
        }

        @SuppressWarnings("unused")
        public Path getBoundsFile() {
            return boundsFile;
        }

        public double[] getSolution() {
            return solution;
        }

        @SuppressWarnings("unused")
        public Path getLogFile() {
            return logFile;
        }
    }

    private class ProblemWrapper extends SimpleProblemWrapper {

        private static final long serialVersionUID = -3771406229118693099L;

        @Override
        public void initializePopulation(final Population population) {

            initTemplate();
            population.clear();

            final List<SimpleOptimizationParameter> parameters =
                ((GenericOptimization) this.getSimpleProblem())
                    .getParameters()
                    .stream()
                    .map(SimpleOptimizationParameter.class::cast)
                    .collect(toImmutableList());
            final FishYAML fishYAML = new FishYAML();
            final Stream<AbstractEAIndividual> seedIndividuals =
                seedScenarios.stream().map(scenarioFile ->
                    makeIndividualFromScenario(scenarioFile, fishYAML, parameters)
                );

            final Stream<AbstractEAIndividual> defaultIndividuals =
                Stream.generate(() -> {
                    final AbstractEAIndividual individual = (AbstractEAIndividual) template.clone();
                    individual.initialize(this);
                    return individual;
                });

            Stream
                .concat(seedIndividuals, defaultIndividuals)
                .limit(population.getTargetSize())
                .forEach(population::add);
            population.initialize();
        }

        private ESIndividualDoubleData makeIndividualFromScenario(
            final Path scenarioFile,
            final FishYAML fishYAML,
            final List<SimpleOptimizationParameter> parameters
        ) {
            final Scenario scenario;
            try (final FileReader fileReader = new FileReader(scenarioFile.toFile())) {
                scenario = fishYAML.loadAs(fileReader, Scenario.class);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            final double[] solution = parameters
                .stream()
                .mapToDouble(parameter -> parameter.computeMappedValue(parameter.getValue(scenario)))
                .toArray();
            final ESIndividualDoubleData individual =
                (ESIndividualDoubleData) getIndividualTemplate().clone();
            individual.setDoubleGenotype(solution);
            individual.setDoublePhenotype(solution);
            return individual;
        }
    }

}
