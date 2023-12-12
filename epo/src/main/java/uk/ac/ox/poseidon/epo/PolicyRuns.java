package uk.ac.ox.poseidon.epo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.maximization.YearlyResultsRowProvider;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.scenario.EpoPathPlannerAbundanceScenario;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;

public class PolicyRuns {

    private static final Logger logger = Logger.getLogger(PolicyRuns.class.getName());

    public static void main(final String[] args) {
        final Path baseFolder = Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");
        final Path baseScenario = baseFolder.resolve(Paths.get(
            "calibrations",
            "2023-12-01/cenv0729/2023-12-03_04.29.43_local",
            "calibrated_scenario.yaml"
        ));
        final Path baseOutputFolder = baseFolder.resolve(Paths.get("policy_runs"));
        final List<Integer> yearsActive = ImmutableList.of(2023);
        final ImmutableList<Double> proportions = ImmutableList.of(0.75, 0.50, 0.25, 0.10, 0.0);
        final ImmutableMap<String, List<Policy<EpoScenario<?>>>> policies = ImmutableMap.of(
                "global_object_set_limits", new GlobalObjectSetLimit(
                    yearsActive,
                    // 8729 FAD + 4003 OFS in 2022:
                    proportions.stream().map(p -> (int) (p * (8729 + 4003))).collect(toList())
                ),
                "fad_limits", new ActiveFadLimitsPolicies(
                    yearsActive,
                    2022,
                    proportions
                ),
                "fad_limits_fine", new ActiveFadLimitsPolicies(
                    yearsActive,
                    2022,
                    IntStream.rangeClosed(1, 19).mapToObj(i -> i * 0.05).collect(toImmutableList())
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

        final int numberOfRunsPerPolicy = 10;
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
                final Path outputFolder = baseOutputFolder.resolve(policyName);
                final Runner<EpoPathPlannerAbundanceScenario> runner =
                    new Runner<>(EpoPathPlannerAbundanceScenario.class, baseScenario, outputFolder)
                        .setPolicies(entry.getValue())
                        .setParallel(true)
                        .setWriteScenarioToFile(true)
                        .registerRowProvider("yearly_results.csv", YearlyResultsRowProvider::new);
                if (!policyName.equals("fad_limits_fine")) {
                    runner
                        .registerRowProvider("spatial_closures.csv", RectangularAreaExtractor::new)
                        .registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new);
                }
                runner.run(3, numberOfRunsPerPolicy);
            });
    }
}
