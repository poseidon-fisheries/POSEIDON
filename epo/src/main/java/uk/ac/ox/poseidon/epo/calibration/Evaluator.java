/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFadAttractionEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.maximization.BoundsWriter;
import uk.ac.ox.oxfish.maximization.YearlyResultsRowProvider;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.AbundanceFadAttractionEventObserver;
import uk.ac.ox.oxfish.model.data.monitors.loggers.GlobalBiomassLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineTripLogger;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Files.getFileExtension;
import static java.lang.Runtime.getRuntime;
import static uk.ac.ox.poseidon.epo.calibration.Calibrator.BOUNDS_FILENAME;
import static uk.ac.ox.poseidon.epo.calibration.Calibrator.CALIBRATED_SCENARIO_FILENAME;

public class Evaluator implements JCommanderRunnable {

    // I identified the vessels to follow using (in R):
    // obs_action_events |>
    //  filter(year == 2017, action_type %in% c("DEL", "FAD", "NOA")) |>
    //  count(ves_no, action_type) |>
    //  group_by(action_type) |>
    //  slice_max(n, with_ties = FALSE)
    @Parameter(names = "--track_fads_of_vessels")
    private Set<String> vesselsWhoseFadsToTrack = ImmutableSet.of(); //"1779", "453", "1552");
    @Parameter(names = {"-r", "--num-runs"})
    private int numRuns = Math.min(8, getRuntime().availableProcessors());
    @Parameter(names = {"-y", "--years"})
    private int numYearsToRuns = 3;
    @Parameter(names = "--parallel")
    private boolean parallel = true;
    @Parameter(converter = PathConverter.class)
    private Path calibrationFolder;

    @Parameter(
        names = {"-s", "--scenario_source"},
        description = "Either a YAML scenario file or a markdown calibration log " +
            "from which a scenario can be extracted.",
        converter = PathConverter.class
    )
    private Path scenarioSource = Paths.get(CALIBRATED_SCENARIO_FILENAME);

    public static void main(final String[] args) {
        new Evaluator().run(args);
    }

    @SuppressWarnings("unused")
    public Path getScenarioSource() {
        return scenarioSource;
    }

    @SuppressWarnings("unused")
    public void setScenarioSource(final Path scenarioSource) {
        this.scenarioSource = checkNotNull(scenarioSource);
    }

    @SuppressWarnings("unused")
    public Set<String> getVesselsWhoseFadsToTrack() {
        return vesselsWhoseFadsToTrack;
    }

    @SuppressWarnings("unused")
    public void setVesselsWhoseFadsToTrack(final Collection<String> vesselsWhoseFadsToTrack) {
        this.vesselsWhoseFadsToTrack = checkNotNull(ImmutableSet.copyOf(vesselsWhoseFadsToTrack));
    }

    @SuppressWarnings("unused")
    public Path getCalibrationFolder() {
        return calibrationFolder;
    }

    @SuppressWarnings("unused")
    public void setCalibrationFolder(final Path calibrationFolder) {
        this.calibrationFolder = checkNotNull(calibrationFolder);
    }

    @Override
    public void run() {
        final Scenario scenario;
        switch (getFileExtension(scenarioSource.toString())) {
            case "yaml":
                scenario = loadScenario();
                break;
            case "md":
                scenario = new ScenarioExtractor(calibrationFolder, calibrationFolder.resolve(scenarioSource))
                    .getAndWriteToFile(CALIBRATED_SCENARIO_FILENAME);
                break;
            default:
                throw new IllegalArgumentException(
                    "Scenario source needs to be .yaml or .md file but was: " + scenarioSource
                );
        }

        if (!java.nio.file.Files.exists(calibrationFolder.resolve(BOUNDS_FILENAME))) {
            final BoundsWriter boundsWriter = new BoundsWriter();
            boundsWriter.setCalibrationFolder(calibrationFolder);
            boundsWriter.run();
        }

        final Runner<Scenario> runner =
            new Runner<>(() -> scenario, calibrationFolder)
                .setParallel(parallel)
                .registerRowProvider("yearly_results.csv", YearlyResultsRowProvider::new)
                .requestFisherYearlyData();

        final AtomicInteger runCounter = new AtomicInteger(1);
        runner.run(numYearsToRuns, numRuns - 1, runCounter);

        if (!vesselsWhoseFadsToTrack.isEmpty()) {
            registerFadAttractionEventProviders(runner);
        }

        runner
            .registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new)
            .registerRowProvider("sim_trip_events.csv", PurseSeineTripLogger::new)
            .registerRowProvider("sim_global_biomass.csv", GlobalBiomassLogger::new)
            // turn the following line on or off as needed:
            // .registerRowProvider("death_events.csv", DeathEventsRowProvider::new)
            .run(numYearsToRuns, 1, runCounter);
    }

    private Scenario loadScenario() {
        checkNotNull(this.calibrationFolder);
        checkNotNull(this.scenarioSource);
        final File scenarioFile = calibrationFolder.resolve(this.scenarioSource).toFile();
        try (final FileReader fileReader = new FileReader(scenarioFile)) {
            final FishYAML fishYAML = new FishYAML();
            return fishYAML.loadAs(fileReader, Scenario.class);
        } catch (final FileNotFoundException e) {
            throw new IllegalArgumentException("Can't find scenario file: " + scenarioFile, e);
        } catch (final IOException e) {
            throw new IllegalStateException("Error while reading file: " + scenarioFile, e);
        }
    }

    private void registerFadAttractionEventProviders(final Runner<Scenario> runner) {
        runner.setAfterStartConsumer(state -> {
            final FishState fishState = state.getModel();
            final AbundanceFadAttractionEventObserver observer =
                new AbundanceFadAttractionEventObserver(fishState);
            fishState.getFishers().stream()
                .filter(fisher -> vesselsWhoseFadsToTrack.contains(fisher.getTagsList().get(0)))
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
