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

package uk.ac.ox.poseidon.epo.policies;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.maximization.YearlyResultsRowProvider;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineTripLogger;
import uk.ac.ox.poseidon.epo.scenarios.EpoPathPlannerAbundanceScenario;
import uk.ac.ox.poseidon.epo.scenarios.EpoScenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;

public class PolicyRuns implements Runnable {

    private static final Logger logger = Logger.getLogger(PolicyRuns.class.getName());

    @Parameter(names = "--output_folder", converter = PathConverter.class)
    private Path outputFolder = Paths.get(
        System.getProperty("user.home"), "workspace", "epo_policy_runs", "runs"
    );

    @Parameter(names = "--scenario", converter = PathConverter.class)
    private Path scenarioFile = Paths.get(
        System.getProperty("user.home"), "workspace", "epo_calibration_runs", "runs",
        "2024-04-03", "local_2024-04-05_13.30.59",
        "calibrated_scenario_updated.yaml"
    );
    @Parameter(names = {"-p", "--policies"})
    private Set<String> policiesToRun = ImmutableSet.of(
        "global_object_set_limits",
        "fad_limits_fine",
        "fad_limits_fine_with_override",
        "extended_closures",
        "el_corralito",
        "western_closure",
        "southern_closure"
    );
    @Parameter(names = {"-r", "--runs_per_policy"})
    private int numberOfRunsPerPolicy = 3;
    @Parameter(names = {"-y", "--years_to_run"})
    private int numberOfYearsToRun = 3;

    public static void main(final String[] args) {
        final Runnable policyRuns = new PolicyRuns();
        JCommander.newBuilder()
            .addObject(policyRuns)
            .build()
            .parse(args);
        policyRuns.run();
    }

    @SuppressWarnings("unused")
    public Set<String> getPoliciesToRun() {
        return policiesToRun;
    }

    @SuppressWarnings("unused")
    public void setPoliciesToRun(final Set<String> policiesToRun) {
        this.policiesToRun = policiesToRun;
    }

    @SuppressWarnings("unused")
    public Path getOutputFolder() {
        return outputFolder;
    }

    @SuppressWarnings("unused")
    public void setOutputFolder(final Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    @SuppressWarnings("unused")
    public Path getScenarioFile() {
        return scenarioFile;
    }

    @SuppressWarnings("unused")
    public void setScenarioFile(final Path scenarioFile) {
        this.scenarioFile = scenarioFile;
    }

    @SuppressWarnings("unused")
    public int getNumberOfRunsPerPolicy() {
        return numberOfRunsPerPolicy;
    }

    @SuppressWarnings("unused")
    public void setNumberOfRunsPerPolicy(final int numberOfRunsPerPolicy) {
        this.numberOfRunsPerPolicy = numberOfRunsPerPolicy;
    }

    @SuppressWarnings("unused")
    public int getNumberOfYearsToRun() {
        return numberOfYearsToRun;
    }

    @SuppressWarnings("unused")
    public void setNumberOfYearsToRun(final int numberOfYearsToRun) {
        this.numberOfYearsToRun = numberOfYearsToRun;
    }

    @Override
    public void run() {
        final Map<String, List<Policy<EpoScenario<?>>>> policies = makePolicies();

        final int numberOfPolicies = policies.values().stream().mapToInt(List::size).sum();
        logger.info(String.format(
            "About to run %d policies %d times (%d total runs)",
            numberOfPolicies,
            numberOfRunsPerPolicy,
            numberOfPolicies * numberOfRunsPerPolicy
        ));

        policies
            .entrySet()
            .stream()
            .parallel()
            .forEach(entry -> {
                final String policyName = entry.getKey();
                final Path outputFolder = this.outputFolder.resolve(policyName);
                final Runner<EpoPathPlannerAbundanceScenario> runner =
                    new Runner<>(EpoPathPlannerAbundanceScenario.class, scenarioFile, outputFolder)
                        .setPolicies(entry.getValue())
                        .setParallel(true)
                        .setWriteScenarioToFile(true)
                        .requestFisherDailyData(columnName -> columnName.equals(
                            "Number of active FADs"))
                        .requestFisherYearlyData()
                        .registerRowProvider("sim_trip_events.csv", PurseSeineTripLogger::new)
                        .registerRowProvider("yearly_results.csv", YearlyResultsRowProvider::new)
                        .registerRowProvider("spatial_closures.csv", RectangularAreaExtractor::new)
                        .registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new);
                runner.run(numberOfYearsToRun, numberOfRunsPerPolicy);
            });
    }

    private Map<String, List<Policy<EpoScenario<?>>>> makePolicies() {
        final List<Integer> yearsActive = ImmutableList.of(2023);
        final ImmutableList<Double> proportions =
            ImmutableList.of(0.75, 0.50, 0.25, 0.10, 0.0);
        final ImmutableList<Double> fineProportions =
            ImmutableList.of(1.00, 0.80, 0.50, 0.25);
//             IntStream.rangeClosed(1, 20)
//                 .mapToObj(i -> i * 0.05)
//                 .collect(toImmutableList());
        return ImmutableMap.of(
                "global_object_set_limits", new GlobalObjectSetLimit(
                    yearsActive,
                    // 8729 FAD + 4003 OFS in 2022:
                    proportions.stream().map(p -> (int) (p * (8729 + 4003))).collect(toList())
                ),
                "fad_limits_fine", new ActiveFadLimitsPolicies(
                    yearsActive,
                    2023,
                    fineProportions,
                    false
                ),
                "fad_limits_fine_with_override", new ActiveFadLimitsPolicies(
                    yearsActive,
                    2023,
                    fineProportions,
                    true
                ),
                "extended_closures", new ExtendedClosurePolicies(
                    yearsActive,
                    ImmutableList.of(5, 15, 30)
                ),
                "el_corralito", new ExtendedElCorralitoPolicy(
                    yearsActive,
                    -5, 5, -120,
                    ImmutableList.of(5, 15, 30)
                ),
                "western_closure", new WesternClosure(
                    yearsActive,
                    -120,
                    ImmutableList.of(5, 15, 30)
                ),
                "southern_closure", new SouthernClosure(
                    yearsActive,
                    2023,
                    ImmutableList.of(0.2, 0.5, 1.0),
                    // We get 5215 from:
                    // obs_action_events %>% filter(year == 2022, action_type %in% c("FAD", "OFS"),
                    // lon >= -125 & lon <= -80, lat >= -20 & lat <= 0) %>% nrow()
                    ImmutableList.of(0, 5215)
                )
            )
            .entrySet()
            .stream()
            .filter(entry -> policiesToRun.contains(entry.getKey()))
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> entry.getValue().getWithDefault()
            ));
    }
}
