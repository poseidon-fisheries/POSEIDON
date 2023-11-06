package uk.ac.ox.poseidon.epo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.maximization.YearlyResultsRowProvider;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineTripLogger;
import uk.ac.ox.oxfish.model.scenario.EpoPathPlannerAbundanceScenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PolicyRuns {

    public static void main(final String[] args) {
        final Path baseFolder = Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");
        final Path baseScenario = baseFolder.resolve(Paths.get(
            "calibrations",
            "2023-10-23/cenv0729/2023-10-25_04.14.17_local",
            "calibrated_scenario.yaml"
        ));
        final Path baseOutputFolder = baseFolder.resolve(Paths.get("policy_runs"));
        final List<Integer> applicableYears = ImmutableList.of(2023);
        final ImmutableList<Double> proportions = ImmutableList.of(1.0, 0.75, 0.50, 0.25, 0.10, 0.0);
        ImmutableMap.of(
                "global_object_set_limits", new GlobalObjectSetLimit(
                    applicableYears,
                    // 8729 FAD + 4003 OFS in 2022:
                    proportions.stream().map(p -> (int) (p * (8729 + 4003))).collect(toList())
                ),
                "fad_limits", new ActiveFadLimitsPolicies(
                    applicableYears,
                    2022,
                    proportions
                ),
                "extended_closures", new ExtendedClosurePolicies(
                    applicableYears,
                    ImmutableList.of(5, 15, 30)
                )
            )
            .entrySet()
            .stream()
            .parallel()
            .forEach(entry -> {
                final Path outputFolder = baseOutputFolder.resolve(entry.getKey());
                final Runner<EpoPathPlannerAbundanceScenario> runner =
                    new Runner<>(EpoPathPlannerAbundanceScenario.class, baseScenario, outputFolder)
                        .setPolicies(entry.getValue().get())
                        .setParallel(true)
                        .registerRowProvider("yearly_results.csv", YearlyResultsRowProvider::new)
                        .requestFisherYearlyData();
                if (!entry.getKey().equals("fad_limits_fine")) {
                    runner
                        .requestFisherDailyData()
                        .registerRowProvider("sim_trip_events.csv", PurseSeineTripLogger::new)
                        .registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new);
                }
                runner.run(3, 1);
            });
    }
}
