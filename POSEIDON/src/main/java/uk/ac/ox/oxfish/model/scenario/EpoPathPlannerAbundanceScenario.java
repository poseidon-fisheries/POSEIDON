package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EpoPurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.SelectivityAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullPerSpeciesCarryingCapacitiesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.ValuePerSetPlanningModuleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogNormalErrorOperatorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.UnreliableFishValueCalculatorFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPenaltyFunctionFactory;
import uk.ac.ox.oxfish.model.plugins.FrontalIndexMapFactory;
import uk.ac.ox.oxfish.model.plugins.TemperatureMapFactory;
import uk.ac.ox.oxfish.regulations.factories.EverythingPermittedRegulationFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.List;

public class EpoPathPlannerAbundanceScenario extends EpoAbundanceScenario {

    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFromFileFactory(
            getInputFolder().path("abundance", "selectivity.csv"),
            getSpeciesCodesSupplier()
        );

    private PurseSeinerFleetFactory purseSeinerFleetFactory =
        new EpoPurseSeinerFleetFactory(
            getTargetYear(),
            getInputFolder(),
            getSpeciesCodesSupplier(),
            new AbundancePurseSeineGearFactory(
                new EverythingPermittedRegulationFactory(),
                new SelectivityAbundanceFadInitializerFactory(
                    // see https://github.com/poseidon-fisheries/tuna-issues/issues/141#issuecomment-1545974455
                    // for Weibull parameter values, obtained by fitting the distributions to observer data
                    new WeibullPerSpeciesCarryingCapacitiesFactory(
                        ImmutableMap.of(
                            "Bigeye tuna", new FixedDoubleParameter(0.6346391),
                            "Skipjack tuna", new FixedDoubleParameter(0.7705004),
                            "Yellowfin tuna", new FixedDoubleParameter(0.7026296)
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new FixedDoubleParameter(8.9333883),
                            "Skipjack tuna", new FixedDoubleParameter(18.4077481),
                            "Yellowfin tuna", new FixedDoubleParameter(5.7959415)
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new FixedDoubleParameter(.4879391),
                            "Skipjack tuna", new FixedDoubleParameter(.0949),
                            "Yellowfin tuna", new FixedDoubleParameter(.2552899)
                        ),
                        new CalibratedParameter(
                            1, 1.5, 0, 2, 1
                        )
                    ),
                    getAbundanceFiltersFactory(),
                    ImmutableMap.of(
                        "Bigeye tuna", new CalibratedParameter(0.03, 0.25, 0, 1, 0.16),
                        "Skipjack tuna", new CalibratedParameter(0.005, 0.25, 0, 1, 0.075),
                        "Yellowfin tuna", new CalibratedParameter(0.008, 0.25, 0, 1, 0.02)
                    ),
                    new EnvironmentalPenaltyFunctionFactory(
                        ImmutableMap.of(
                            "Temperature", new TemperatureMapFactory(
                                getInputFolder().path("environmental_maps", "temperature.csv")
                            ),
                            "FrontalIndex", new FrontalIndexMapFactory(
                                getInputFolder().path("environmental_maps", "frontal_index.csv")
                            )
                        )
                    )
                ),
                // ref: https://github.com/poseidon-fisheries/tuna-issues/issues/141#issuecomment-1549923263
                // For fixed parameter values see:
                // https://github.com/poseidon-fisheries/tuna-issues/issues/202#issue-1779551927
                new UnreliableFishValueCalculatorFactory(new LogNormalErrorOperatorFactory(
                    new FixedDoubleParameter(-0.14452),
                    new FixedDoubleParameter(0.14097)
                ))
            ),
            new EPOPlannedStrategyFlexibleFactory(
                getTargetYear(),
                new LocationValuesFactory(
                    getInputFolder().path("location_values.csv"),
                    new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                    new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                    new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                    new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                    getTargetYear()
                ),
                new ValuePerSetPlanningModuleFactory(),
                new AbundanceCatchSamplersFactory(
                    getSpeciesCodesSupplier(),
                    getAbundanceFiltersFactory(),
                    getInputFolder().path("set_samples.csv")
                ),
                getInputFolder().path("action_weights.csv"),
                getInputFolder().path("vessels.csv")
            ),
            new DefaultToDestinationStrategyFishingStrategyFactory()
        );

    public PurseSeinerFleetFactory getPurseSeinerFleetFactory() {
        return purseSeinerFleetFactory;
    }

    @SuppressWarnings("unused")
    public void setPurseSeinerFleetFactory(final PurseSeinerFleetFactory purseSeinerFleetFactory) {
        this.purseSeinerFleetFactory = purseSeinerFleetFactory;
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        purseSeinerFleetFactory.useDummyData(testFolder());
    }

    @Override
    List<Fisher> makeFishers(final FishState fishState, final int targetYear) {
        return purseSeinerFleetFactory.makeFishers(fishState, targetYear);
    }

}


