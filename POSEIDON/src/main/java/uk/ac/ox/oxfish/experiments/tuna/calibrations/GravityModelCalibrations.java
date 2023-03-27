package uk.ac.ox.oxfish.experiments.tuna.calibrations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityEnvironmentalAttractorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.WeibullLinearIntervalEnvironmentalAttractorFactory;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.model.scenario.EpoGravityAbundanceScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;

public class GravityModelCalibrations {
    public static void main(final String[] args) {

        // Values here don't matter, as they are going to get calibrated

        final LinkedHashMap<String, Double> carryingCapacityShapeParameters =
            new LinkedHashMap<>(ImmutableMap.of(
                "Skipjack tuna", 22276.61357257331,
                "Bigeye tuna", 3150.079023390662,
                "Yellowfin tuna", 9393.492049525945
            ));

        final LinkedHashMap<String, Double> carryingCapacityScaleParameters =
            new LinkedHashMap<>(ImmutableMap.of(
                "Skipjack tuna", 0.7550522737541978,
                "Bigeye tuna", 0.38856703429600276,
                "Yellowfin tuna", 1.570847805276818
            ));

        final LinkedList<AdditionalMapFactory> environmentalMaps =
            new LinkedList<>(ImmutableList.of(
                new AdditionalMapFactory("Clorophill", "inputs/tests/clorophill.csv"),
                new AdditionalMapFactory("Temperature", "inputs/tests/temperature.csv"),
                new AdditionalMapFactory("FrontalIndex", "inputs/tests/frontalindex.csv"),
                new AdditionalMapFactory("SKJMINUSBET", "inputs/tests/skj_minus_bet.csv")
            ));

        final LinkedList<DoubleParameter> environmentalThresholds =
            new LinkedList<>(ImmutableList.of(
                new FixedDoubleParameter(0),
                new FixedDoubleParameter(26.407350615564233),
                new FixedDoubleParameter(0.0),
                new FixedDoubleParameter(0.7964784251739445)
            ));

        final LinkedList<DoubleParameter> environmentalPenalties =
            new LinkedList<>(ImmutableList.of(
                new FixedDoubleParameter(2),
                new FixedDoubleParameter(2),
                new FixedDoubleParameter(2),
                new FixedDoubleParameter(2)
            ));


        final LinkedHashMap<String, Double> catchabilities =
            new LinkedHashMap<>(ImmutableMap.of(
                "Skipjack tuna", 0.010600048411855497,
                "Bigeye tuna", 0.07226185882579747,
                "Yellowfin tuna", 0.02067837393061897
            ));

        final Map<String, Function<AbundanceFiltersFactory, AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>>>> fadInitializerFactories =
            ImmutableMap.of(
                "scenario-gravity_weibull_interval",
                abundanceFiltersFactory ->
                    new WeibullLinearIntervalEnvironmentalAttractorFactory(
                        abundanceFiltersFactory,
                        new FixedDoubleParameter(1E-3),
                        new FixedDoubleParameter(1),
                        carryingCapacityShapeParameters,
                        carryingCapacityScaleParameters,
                        new FixedDoubleParameter(5.722025419605172),
                        new FixedDoubleParameter(21.993000000000002),
                        new FixedDoubleParameter(1.36247134532557),
                        environmentalMaps,
                        environmentalThresholds
                    ),
                "scenario-gravity_weibull_catchability",
                abundanceFiltersFactory ->
                    new WeibullCatchabilitySelectivityEnvironmentalAttractorFactory(
                        abundanceFiltersFactory,
                        carryingCapacityShapeParameters,
                        carryingCapacityScaleParameters,
                        catchabilities,
                        new FixedDoubleParameter(1E-3),
                        new FixedDoubleParameter(5.722025419605172),
                        new FixedDoubleParameter(41.6127216390614),
                        new FixedDoubleParameter(1),
                        environmentalMaps,
                        environmentalThresholds,
                        environmentalPenalties
                )
            );

        fadInitializerFactories.forEach((scenarioName, fadInitializerFactoryMaker) -> {
            final EpoGravityAbundanceScenario scenario = new EpoGravityAbundanceScenario();
            final AbundanceFiltersFactory abundanceFiltersFactory = scenario.getAbundanceFiltersFactory();
            scenario
                .getPurseSeinerFleetFactory()
                .getPurseSeineGearFactory()
                .setFadInitializerFactory(
                    fadInitializerFactoryMaker.apply(abundanceFiltersFactory)
                );
            final File scenarioFile = Paths.get("inputs", "epo_inputs", "tests", scenarioName + ".yaml").toFile();
            try {
                new FishYAML().dump(scenario, new FileWriter(scenarioFile));
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        });

    }
}
