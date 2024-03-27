/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.epo.policies;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.maximization.YearlyResultsRowProvider;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.poseidon.epo.scenarios.EpoPathPlannerAbundanceScenario;
import uk.ac.ox.poseidon.epo.scenarios.EpoScenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
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
        "2024-02-13", "cenv0729", "2024-02-17_06.26.53_local",
        "calibrated_scenario_updated.yaml"
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

    private static Map<String, List<Policy<EpoScenario<?>>>> makePolicies() {
        final List<Integer> yearsActive = ImmutableList.of(2023);
        final ImmutableList<Double> proportions =
            ImmutableList.of(0.75, 0.50, 0.25, 0.10, 0.0);
        final ImmutableList<Double> fineProportions =
            IntStream.rangeClosed(1, 19)
                .mapToObj(i -> i * 0.05)
                .collect(toImmutableList());
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
                )
            )
            .entrySet()
            .stream()
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> entry.getValue().getWithDefault()
            ));
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
            numberOfYearsToRun,
            numberOfPolicies * numberOfYearsToRun
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
                        .requestFisherDailyData(columnName -> columnName.equals("Number of active FADs"))
                        .requestFisherYearlyData()
                        .registerRowProvider("yearly_results.csv", YearlyResultsRowProvider::new);
                if (!policyName.startsWith("fad_limits_fine")) {
                    runner
                        .registerRowProvider("spatial_closures.csv", RectangularAreaExtractor::new)
                        .registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new);
                }
                runner.run(numberOfYearsToRun, numberOfRunsPerPolicy);
            });
    }
}
