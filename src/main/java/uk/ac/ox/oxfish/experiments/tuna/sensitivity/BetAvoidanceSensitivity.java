package uk.ac.ox.oxfish.experiments.tuna.sensitivity;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityEnvironmentalAttractorFactory;
import uk.ac.ox.oxfish.maximization.YearlyResultsRowProvider;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineTripLogger;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.model.scenario.EpoScenarioPathfinding;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class BetAvoidanceSensitivity {

    public static void main(final String[] args) {
        final Path baseFolder = Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");
        final Path baseScenario = baseFolder.resolve(Paths.get(
            "calibrations", "pathfinding", "calibration_LCWCC_VPS",
            "cenv0729", "2022-12-06_04.48.10_local", "calibrated_scenario.yaml"
        ));
        final Path outputFolder = baseFolder.resolve(Paths.get("runs", "bet_avoidance_sensitivity"));

        new Runner<>(EpoScenarioPathfinding.class, baseScenario, outputFolder)
            .setPolicies(ImmutableList.of(
                Policy.DEFAULT,
                noBetAvoidancePolicy()
            ))
            .setParallel(true)
            .registerRowProvider("yearly_results.csv", YearlyResultsRowProvider::new)
            .registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new)
            .registerRowProvider("sim_trip_events.csv", PurseSeineTripLogger::new)
            .run(3, 4);
    }

    @NotNull
    private static Policy<EpoScenarioPathfinding> noBetAvoidancePolicy() {
        return new Policy<>(
            "No BET avoidance",
            "BET avoidance layer turned off",
            scenario -> {
                final WeibullCatchabilitySelectivityEnvironmentalAttractorFactory fadInitializerFactory =
                    (WeibullCatchabilitySelectivityEnvironmentalAttractorFactory) scenario.getFadInitializerFactory();
                final int avoidanceMapIndex = fadInitializerFactory
                    .getEnvironmentalMaps()
                    .stream()
                    .map(AdditionalMapFactory::getMapVariableName)
                    .collect(Collectors.toList())
                    .indexOf("SKJMINUSBET");
                fadInitializerFactory
                    .getEnvironmentalThresholds()
                    .set(avoidanceMapIndex, new FixedDoubleParameter(0));
            }
        );
    }

}
