/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.epo.calibration;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFadAttractionEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.SolutionExtractor;
import uk.ac.ox.oxfish.maximization.YearlyResultsRowProvider;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.AbundanceFadAttractionEventObserver;
import uk.ac.ox.oxfish.model.data.monitors.loggers.GlobalBiomassLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineTripLogger;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.io.Files.getFileExtension;
import static java.lang.Runtime.getRuntime;

public class Evaluator implements Runnable {

    // I identified the vessels to follow using (in R):
    // obs_action_events |>
    //  filter(year == 2017, action_type %in% c("DEL", "FAD", "NOA")) |>
    //  count(ves_no, action_type) |>
    //  group_by(action_type) |>
    //  slice_max(n, with_ties = FALSE)
    @Parameter(names = "--track_fads_of_vessels")
    private Set<String> vesselsOfWhichToTrackFads = ImmutableSet.of("1779", "453", "1552");
    @Parameter(names = {"-r", "--num-runs"})
    private int numRuns = Math.min(16, getRuntime().availableProcessors());
    @Parameter(names = {"-y", "--years"})
    private int numYearsToRuns = 3;
    @Parameter(names = "--parallel", arity = 1)
    private boolean parallel = true;
    @Parameter(converter = PathConverter.class)
    private Path calibrationFolder;

    private static Scenario makeScenario(
        final GenericOptimization optimization,
        final double[] optimalParameters
    ) {
        try {
            return GenericOptimization.buildScenario(
                optimalParameters,
                Paths.get(optimization.getScenarioFile()).toFile(),
                optimization.getParameters()
            );
        } catch (final FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(final String[] args) {

        final Runnable evaluator = new Evaluator();
        JCommander.newBuilder()
            .addObject(evaluator)
            .build()
            .parse(args);

        evaluator.run();
    }

    private static Path findCalibrationFile(final Path folder) {
        try (final Stream<Path> paths = Files.list(folder)) {
            final ImmutableList<Path> calibrationFiles = paths
                .filter(path -> getFileExtension(path.toString()).equals("yaml"))
                .filter(Evaluator::isCalibrationFile)
                .collect(toImmutableList());
            checkState(!calibrationFiles.isEmpty(), "No calibration files found in %s", folder);
            checkState(calibrationFiles.size() == 1, "More than one calibration files found in %s", folder);
            return calibrationFiles.get(0);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isCalibrationFile(final Path path) {
        try (final Stream<String> lines = Files.lines(path)) {
            return lines
                .findFirst()
                .filter(line -> line.equals("!!uk.ac.ox.oxfish.maximization.GenericOptimization"))
                .isPresent();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    public Set<String> getVesselsOfWhichToTrackFads() {
        return vesselsOfWhichToTrackFads;
    }

    @SuppressWarnings("unused")
    public void setVesselsOfWhichToTrackFads(final Set<String> vesselsOfWhichToTrackFads) {
        this.vesselsOfWhichToTrackFads = vesselsOfWhichToTrackFads;
    }

    @SuppressWarnings("unused")
    public Path getCalibrationFolder() {
        return calibrationFolder;
    }

    @SuppressWarnings("unused")
    public void setCalibrationFolder(final Path calibrationFolder) {
        this.calibrationFolder = calibrationFolder;
    }

    @Override
    public void run() {

        final Path calibrationFilePath = findCalibrationFile(calibrationFolder);
        final Path logFilePath = calibrationFolder.resolve("calibration_log.md");
        final double[] solution = new SolutionExtractor(logFilePath).bestSolution().getKey();
        final GenericOptimization optimization = GenericOptimization.fromFile(calibrationFilePath);

        final Runner<Scenario> runner =
            new Runner<>(() -> makeScenario(optimization, solution), calibrationFilePath.getParent())
                .writeScenarioToFile("calibrated_scenario.yaml")
                .setParallel(parallel)
                .registerRowProvider("yearly_results.csv", YearlyResultsRowProvider::new)
                .requestFisherYearlyData();

        final AtomicInteger runCounter = new AtomicInteger(1);
        runner.run(numYearsToRuns, numRuns - 1, runCounter);

        if (!vesselsOfWhichToTrackFads.isEmpty()) {
            registerFadAttractionEventProviders(runner);
        }

        runner
            .registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new)
            .registerRowProvider("sim_trip_events.csv", PurseSeineTripLogger::new)
            .registerRowProvider("sim_global_biomass.csv", GlobalBiomassLogger::new)
            .run(numYearsToRuns, 1, runCounter);
    }

    private void registerFadAttractionEventProviders(final Runner<Scenario> runner) {
        runner.setAfterStartConsumer(state -> {
            final FishState fishState = state.getModel();
            final AbundanceFadAttractionEventObserver observer =
                new AbundanceFadAttractionEventObserver(fishState);
            fishState.getFishers().stream()
                .filter(fisher -> vesselsOfWhichToTrackFads.contains(fisher.getTagsList().get(0)))
                .map(FadManager::getFadManager)
                .forEach(fadManager ->
                    fadManager.registerObserver(AbundanceFadAttractionEvent.class, observer)
                );
            ImmutableMap.of(
                "fad_attraction_events.csv", observer.getEventLogger(),
                "tile_abundance_before.csv", observer.getTileAbundanceLogger(),
                "fad_abundance_delta.csv", observer.getFadAbundanceLogger()
            ).forEach((fileName, logger) -> runner.registerRowProvider(fileName, __ -> logger));
        });
    }

    @SuppressWarnings("unused")
    public int getNumYearsToRuns() {
        return numYearsToRuns;
    }

    @SuppressWarnings("unused")
    public void setNumYearsToRuns(final int numYearsToRuns) {
        this.numYearsToRuns = numYearsToRuns;
    }

    @SuppressWarnings("unused")
    public int getNumRuns() {
        return numRuns;
    }

    @SuppressWarnings("unused")
    public void setNumRuns(final int numRuns) {
        this.numRuns = numRuns;
    }

    @SuppressWarnings("unused")
    public boolean isParallel() {
        return parallel;
    }

    @SuppressWarnings("unused")
    public void setParallel(final boolean parallel) {
        this.parallel = parallel;
    }

}
