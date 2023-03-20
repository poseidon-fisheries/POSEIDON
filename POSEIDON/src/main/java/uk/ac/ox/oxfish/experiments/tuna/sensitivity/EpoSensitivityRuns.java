package uk.ac.ox.oxfish.experiments.tuna.sensitivity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.experiments.tuna.Policy;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityEnvironmentalAttractorFactory;
import uk.ac.ox.oxfish.maximization.YearlyResultsRowProvider;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineTripLogger;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.*;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.EpoScenarioPathfinding;
import uk.ac.ox.oxfish.model.scenario.StandardIattcRegulationsFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.time.Month.AUGUST;
import static java.time.Month.JUNE;
import static uk.ac.ox.oxfish.experiments.tuna.Policy.makeDelayedRegulationsPolicy;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.dayOfYear;

public class EpoSensitivityRuns {

    private static final int YEARS_BEFORE_POLICIES_KICK_IN = 1;

    public static void main(final String[] args) {
        final Path baseFolder = Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");
        final Path baseScenario = baseFolder.resolve(Paths.get(
            "calibrations",
            //"pathfinding", "calibration_LCWCC_VPS", "cenv0729", "2022-12-06_04.48.10_local"
            "vps_holiday_runs", "without_betavoid_with_temp", "cenv0729", "2022-12-25_20.45.38_local",
            "calibrated_scenario.yaml"
        ));
        final Path baseOutputFolder = baseFolder.resolve(Paths.get("sensitivity"));
        ImmutableMap.of(
//            "temperature", noTemperatureLayerPolicies(),
                "fad_limits", fadLimitPolicies(IntStream.of(100)) //5, 25, 100)),
//                "fad_limits_fine", fadLimitPolicies(
//                    IntStream.rangeClosed(1, 20).map(i -> i * 5)
//                )//,
//            "spatial_closures", spatialClosurePolicies(),
//            "skj_minus_bet", betAvoidancePolicies(),
//            "southern_spatial_closure", southernSpatialClosurePolicies()
            )
            .entrySet()
            .stream()
            .parallel()
            .forEach(entry -> {
                final Path outputFolder = baseOutputFolder.resolve(entry.getKey());
                final Runner<EpoScenarioPathfinding> runner =
                    new Runner<>(EpoScenarioPathfinding.class, baseScenario, outputFolder)
                        .setPolicies(entry.getValue())
                        .setParallel(true)
                        .registerRowProvider("yearly_results.csv", YearlyResultsRowProvider::new)
                        .requestFisherYearlyData();
                if (!entry.getKey().equals("fad_limits_fine")) {
                    runner
                        .requestFisherDailyData()
                        .registerRowProvider("sim_trip_events.csv", PurseSeineTripLogger::new)
                        .registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new);
                }
                runner.run(3, 16);
            });
    }

    @NotNull
    private static List<Policy<? super EpoScenarioPathfinding>> betAvoidancePolicies() {
        return makePolicyList(
            new Policy<>(
                "With SKJ-BET layer",
                "SKJ-BET layer turned on",
                scenario -> setLayerThreshold(scenario, "SKJMINUSBET", 0.2258513514917878)
            )
        );
    }

    @SafeVarargs
    private static List<Policy<? super EpoScenarioPathfinding>> makePolicyList(
        final Policy<? super EpoScenarioPathfinding>... policies
    ) {
        return makePolicyList(Arrays.asList(policies));
    }

    private static List<Policy<? super EpoScenarioPathfinding>> makePolicyList(
        final Iterable<Policy<? super EpoScenarioPathfinding>> policies
    ) {
        return ImmutableList.<Policy<? super EpoScenarioPathfinding>>builder()
            .add(Policy.DEFAULT)
            .addAll(policies)
            .build();
    }

    @NotNull
    private static List<Policy<? super EpoScenarioPathfinding>> noTemperatureLayerPolicies() {
        return makePolicyList(
            new Policy<>(
                "No temperature layer",
                "Temperature layer turned off",
                scenario -> setLayerThreshold(scenario, "Temperature", 0)
            )
        );
    }

    private static List<Policy<? super EpoScenarioPathfinding>> fadLimitPolicies(final IntStream limits) {
        return limits
            .mapToDouble(i -> i / 100.0)
            .mapToObj(pctOfRegularLimit -> {
                final String name = String.format(
                    "%d%% of regular active FAD limits",
                    (int) (pctOfRegularLimit * 100)
                );
                return makeDelayedRegulationsPolicy(
                    name,
                    ImmutableList.of(makeFadLimitsFactory(pctOfRegularLimit)),
                    null,
                    YEARS_BEFORE_POLICIES_KICK_IN
                );
            })
            .collect(toImmutableList());
    }

    private static ActiveFadLimitsFactory makeFadLimitsFactory(final double pctOfRegularLimit) {
        final ActiveFadLimitsFactory fadLimitsFactory = new ActiveFadLimitsFactory();
        fadLimitsFactory.setLimitClass6a((int) (fadLimitsFactory.getLimitClass6a() * pctOfRegularLimit));
        fadLimitsFactory.setLimitClass6b((int) (fadLimitsFactory.getLimitClass6b() * pctOfRegularLimit));
        return fadLimitsFactory;
    }

    private static List<Policy<? super EpoScenarioPathfinding>> southernSpatialClosurePolicies() {
        final SpecificProtectedAreaFromCoordinatesFactory spatialClosureFactory =
            new SpecificProtectedAreaFromCoordinatesFactory(
                -2, -135, -10, -85
            );
        return makePolicyList(
            ImmutableList.of(
                new Policy<>(
                    "Southern spatial closure",
                    scenario -> addRegulation(scenario, spatialClosureFactory)
                )
            )
        );
    }

    private static List<Policy<? super EpoScenarioPathfinding>> spatialClosurePolicies() {
        final SpecificProtectedAreaFromCoordinatesFactory spatialClosureFactory =
            new SpecificProtectedAreaFromCoordinatesFactory(
                5, -150, -5, -145
            );
        final TemporaryRegulationFactory q3SpatialClosureFactory =
            new TemporaryRegulationFactory(
                dayOfYear(JUNE, 1), dayOfYear(AUGUST, 31),
                spatialClosureFactory
            );
        return makePolicyList(
            ImmutableList.of(
                makeDelayedRegulationsPolicy(
                    "Annual western spatial closure",
                    null,
                    scenario -> spatialClosureFactory,
                    YEARS_BEFORE_POLICIES_KICK_IN
                ),
                makeDelayedRegulationsPolicy(
                    "Third quarter western spatial closure",
                    null,
                    scenario -> q3SpatialClosureFactory,
                    YEARS_BEFORE_POLICIES_KICK_IN
                )
            )
        );
    }

    private static void addRegulation(
        final EpoScenarioPathfinding scenario,
        final AlgorithmFactory<? extends Regulation> regulationFactory
    ) {
        scenario.setRegulationsFactory(
            new CompositeMultipleRegulationsFactory(
                ImmutableList.of(
                    new StandardIattcRegulationsFactory(
                        new ProtectedAreasFromFolderFactory(
                            scenario.getInputFolder().path("regions"),
                            "region_tags.csv"
                        )
                    ),
                    new MultipleRegulationsFactory(
                        ImmutableMap.of(regulationFactory, TAG_FOR_ALL)
                    )
                )
            )
        );
    }

    private static void setLayerThreshold(
        final EpoScenarioPathfinding scenario,
        final String layerName,
        final double threshold
    ) {
        final WeibullCatchabilitySelectivityEnvironmentalAttractorFactory fadInitializerFactory =
            (WeibullCatchabilitySelectivityEnvironmentalAttractorFactory) scenario.getFadInitializerFactory();
        final int avoidanceMapIndex = fadInitializerFactory
            .getEnvironmentalMaps()
            .stream()
            .map(AdditionalMapFactory::getMapVariableName)
            .collect(Collectors.toList())
            .indexOf(layerName);
        fadInitializerFactory
            .getEnvironmentalThresholds()
            .set(avoidanceMapIndex, new FixedDoubleParameter(threshold));
    }

}
