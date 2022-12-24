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
import uk.ac.ox.oxfish.model.regs.factory.CompositeMultipleRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.factory.MultipleRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.factory.SpecificProtectedAreaFromCoordinatesFactory;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;
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
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.time.Month.AUGUST;
import static java.time.Month.JUNE;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.dayOfYear;

public class EpoSensitivityRuns {

    public static void main(final String[] args) {
        final Path baseFolder = Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");
        final Path baseScenario = baseFolder.resolve(Paths.get(
            "calibrations", "pathfinding", "calibration_LCWCC_VPS",
            "cenv0729", "2022-12-06_04.48.10_local", "calibrated_scenario.yaml"
        ));
        final Path baseOutputFolder = baseFolder.resolve(Paths.get("sensitivity"));
        ImmutableMap.of(
//            "bet_avoidance", noBetAvoidancePolicies(),
//            "temperature", noTemperatureLayerPolicies(),
            "fad_limits", fadLimitPolicies()//,
            //"spatial_closures", spatialClosurePolicies()
        )
            .entrySet()
            .stream()
            .parallel()
            .forEach(entry -> {
                final Path outputFolder = baseOutputFolder.resolve(entry.getKey());
                new Runner<>(EpoScenarioPathfinding.class, baseScenario, outputFolder)
                    .setPolicies(entry.getValue())
                    .setParallel(true)
                    .registerRowProvider("yearly_results.csv", YearlyResultsRowProvider::new)
                    .registerRowProvider("sim_action_events.csv", PurseSeineActionsLogger::new)
                    .registerRowProvider("sim_trip_events.csv", PurseSeineTripLogger::new)
                    .run(3, 16);
            });
    }

    @NotNull
    private static List<Policy<? super EpoScenarioPathfinding>> noBetAvoidancePolicies() {
        return makePolicyList(
            new Policy<>(
                "No BET avoidance",
                "BET avoidance layer turned off",
                scenario -> setLayerThreshold(scenario, "SKJMINUSBET", 0)
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

    private static List<Policy<? super EpoScenarioPathfinding>> fadLimitPolicies() {
        return makePolicyList(
            Stream.of(0.50, 0.25, 0.01)
                .map(pctOfRegularLimit -> {
                    final String name = String.format(
                        "%d%% of regular active FAD limits",
                        (int) (pctOfRegularLimit * 100)
                    );
                    return new Policy<EpoScenarioPathfinding>(
                        name,
                        name,
                        scenario -> setActiveFadLimits(scenario, pctOfRegularLimit)
                    );
                })
                .collect(toImmutableList())
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
                new Policy<>(
                    "Annual western spatial closure",
                    scenario -> addRegulation(scenario, spatialClosureFactory)
                ),
                new Policy<>(
                    "Third quarter western spatial closure",
                    scenario -> addRegulation(scenario, q3SpatialClosureFactory)
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
                    new StandardIattcRegulationsFactory(),
                    new MultipleRegulationsFactory(
                        ImmutableMap.of(regulationFactory, TAG_FOR_ALL)
                    )
                )
            )
        );
    }

    private static void setActiveFadLimits(
        final EpoScenarioPathfinding scenario,
        final double pctOfRegularLimit
    ) {
        scenario.getAbundancePurseSeineGearFactory()
            .getActionSpecificRegulations()
            .stream()
            .filter(factory -> factory instanceof ActiveFadLimitsFactory)
            .forEach(algorithmFactory -> {
                final ActiveFadLimitsFactory fadLimits = (ActiveFadLimitsFactory) algorithmFactory;
                fadLimits.setLimitClass6a((int) (fadLimits.getLimitClass6a() * pctOfRegularLimit));
                fadLimits.setLimitClass6b((int) (fadLimits.getLimitClass6b() * pctOfRegularLimit));
            });
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
