package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EpoPurseSeinerFleetFactory;
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
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPenaltyFunctionFactory;
import uk.ac.ox.oxfish.model.plugins.FrontalIndexMapFactory;
import uk.ac.ox.oxfish.model.plugins.TemperatureMapFactory;
import uk.ac.ox.oxfish.regulation.ForbiddenIf;
import uk.ac.ox.oxfish.regulation.NamedRegulations;
import uk.ac.ox.oxfish.regulation.conditions.*;
import uk.ac.ox.oxfish.regulation.quantities.NumberOfActiveFads;
import uk.ac.ox.oxfish.regulation.quantities.SumOf;
import uk.ac.ox.oxfish.regulation.quantities.YearlyActionCount;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static java.time.Month.*;

public class EpoPathPlannerAbundanceScenario extends EpoAbundanceScenario {

    private AbundanceFiltersFactory abundanceFilters =
        new AbundanceFiltersFromFileFactory(
            getInputFolder().path("abundance", "selectivity.csv")
        );

    private AlgorithmFactory<ScenarioPopulation> purseSeinerFleet =
        new EpoPurseSeinerFleetFactory(
            getTargetYear(),
            getInputFolder(),
            new AbundancePurseSeineGearFactory(
                new NamedRegulations(
                    ImmutableMap.of(
                        "DEL licence", new ForbiddenIf(
                            new AllOf(
                                new ActionCodeIs("DEL"),
                                new Not(new AgentHasTag("has_del_license"))
                            )
                        ),
                        "Active-FAD limits", new ForbiddenIf(
                            new AllOf(
                                new ActionCodeIs("DPL"),
                                new AnyOf(
                                    new AllOf(
                                        new AgentHasTag("class 6A"),
                                        new Not(new Below(new NumberOfActiveFads(), 300))
                                    ),
                                    new AllOf(
                                        new AgentHasTag("class 6B"),
                                        new Not(new Below(new NumberOfActiveFads(), 450))
                                    )
                                )
                            )
                        ),
                        // Yearly set limits, set to 99999 as a placeholder
                        "Object-set limits", new ForbiddenIf(
                            new AllOf(
                                new AnyOf(
                                    new ActionCodeIs("FAD"),
                                    new ActionCodeIs("OFS")
                                ),
                                new Not(
                                    new Below(
                                        new SumOf(
                                            new YearlyActionCount("FAD"),
                                            new YearlyActionCount("OFS")
                                        ),
                                        999999
                                    )
                                )
                            )
                        ),
                        // Forbid deployments 15 days before closure
                        "Closure A", new ForbiddenIf(
                            new AnyOf(
                                new AllOf(
                                    // Forbid deployments 15 days before closure
                                    new AgentHasTag("closure A"),
                                    new ActionCodeIs("DPL"),
                                    new BetweenYearlyDates(
                                        JULY, 14,
                                        OCTOBER, 28
                                    )
                                ),
                                new AllOf(
                                    new AgentHasTag("closure A"),
                                    new BetweenYearlyDates(
                                        JULY, 29,
                                        OCTOBER, 8
                                    )
                                )
                            )
                        ),
                        "Closure B", new ForbiddenIf(
                            new AnyOf(
                                // Forbid deployments 15 days before closure
                                new AllOf(
                                    new AgentHasTag("closure B"),
                                    new ActionCodeIs("DPL"),
                                    new BetweenYearlyDates(
                                        OCTOBER, 25,
                                        NOVEMBER, 8
                                    )
                                ),
                                new AllOf(
                                    new AgentHasTag("closure B"),
                                    new BetweenYearlyDates(
                                        NOVEMBER, 9,
                                        JANUARY, 19
                                    )
                                )
                            )
                        ),
                        "El Corralito", new ForbiddenIf(
                            new AllOf(
                                new BetweenYearlyDates(
                                    OCTOBER, 9,
                                    NOVEMBER, 8
                                ),
                                new InRectangularArea(
                                    4.0, -110.0, -3.0, -96.0
                                )
                            )
                        )
                    )
                ),
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
                    getAbundanceFilters(),
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
                    getAbundanceFilters(),
                    getInputFolder().path("set_samples.csv")
                ),
                getInputFolder().path("action_weights.csv"),
                getInputFolder().path("vessels.csv")
            ),
            new DefaultToDestinationStrategyFishingStrategyFactory()
        );

    public AlgorithmFactory<ScenarioPopulation> getPurseSeinerFleet() {
        return purseSeinerFleet;
    }

    @SuppressWarnings("unused")
    public void setPurseSeinerFleet(final AlgorithmFactory<ScenarioPopulation> purseSeinerFleet) {
        this.purseSeinerFleet = purseSeinerFleet;
    }

    public AbundanceFiltersFactory getAbundanceFilters() {
        return abundanceFilters;
    }

    public void setAbundanceFilters(final AbundanceFiltersFactory abundanceFilters) {
        this.abundanceFilters = abundanceFilters;
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        if (purseSeinerFleet instanceof Dummyable)
            ((Dummyable) purseSeinerFleet).useDummyData(testFolder());
    }

}


