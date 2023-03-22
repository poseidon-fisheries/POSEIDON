/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceProcessesFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionFieldsSupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesSupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerAbundanceFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.fads.*;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoAbundanceScenario extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {

    private GravityDestinationStrategyFactory gravityDestinationStrategyFactory =
        new GravityDestinationStrategyFactory(
            getInputFolder().path("action_weights.csv"),
            getVesselsFile(),
            new AttractionFieldsSupplier(
                new LocationValuesSupplier(
                    getInputFolder().path("location_values.csv")
                ),
                getInputFolder().path("max_current_speeds.csv")
            )
        );

    public EpoAbundanceScenario() {
        setFadInitializerFactory(
            new LinearAbundanceFadInitializerFactory(
                getSpeciesCodesSupplier(),
                "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
            )
        );
        setBiologicalProcessesFactory(
            new AbundanceProcessesFactory(getInputFolder().path("abundance"), getSpeciesCodesSupplier())
        );
        setFadMapFactory(new AbundanceFadMapFactory(getCurrentPatternMapSupplier()));
        final InputPath maxCurrentSpeedsFile = getInputFolder().path("max_current_speeds.csv");
        setFishingStrategyFactory(
            new PurseSeinerAbundanceFishingStrategyFactory(
                getSpeciesCodesSupplier(),
                getInputFolder().path("action_weights.csv"),
                new AbundanceCatchSamplersFactory(
                    getSpeciesCodesSupplier(),
                    new AbundanceFiltersFactory(
                        getInputFolder().path("abundance", "selectivity.csv"),
                        getSpeciesCodesSupplier()
                    ),
                    getInputFolder().path("set_samples.csv")
                ),
                new SetDurationSamplersFactory(
                    getInputFolder().path("set_durations.csv")
                ),
                maxCurrentSpeedsFile,
                getInputFolder().path("set_compositions.csv")
            )
        );
        setPurseSeineGearFactory(new AbundancePurseSeineGearFactory());
    }

    public GravityDestinationStrategyFactory getGravityDestinationStrategyFactory() {
        return gravityDestinationStrategyFactory;
    }

    public void setGravityDestinationStrategyFactory(final GravityDestinationStrategyFactory gravityDestinationStrategyFactory) {
        this.gravityDestinationStrategyFactory = gravityDestinationStrategyFactory;
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {

        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);

        ((PluggableSelectivity) getFadInitializerFactory()).setSelectivityFilters(
            ((AbundanceCatchSamplersFactory) ((PurseSeinerAbundanceFishingStrategyFactory)
                getFishingStrategyFactory()).getCatchSamplersFactory())
                .getAbundanceFiltersFactory()
                .apply(fishState)
                .get(FadSetAction.class)
        );

        getPurseSeineGearFactory().setFadInitializerFactory(getFadInitializerFactory());

        final FisherFactory fisherFactory = makeFisherFactory(
            fishState,
            getRegulationsFactory(),
            gravityDestinationStrategyFactory
        );

        final List<Fisher> fishers =
            new PurseSeineVesselReader(
                getVesselsFile().get(),
                TARGET_YEAR,
                fisherFactory,
                buildPorts(fishState)
            ).apply(fishState);

        plugins.forEach(plugin -> fishState.registerStartable(plugin.apply(fishState)));

        scenarioPopulation.getPopulation().addAll(fishers);
        return scenarioPopulation;
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        this.gravityDestinationStrategyFactory
            .getAttractionFieldsSupplier()
            .getLocationValuesSupplier()
            .setLocationValuesFile(testFolder().path("dummy_location_values.csv"));
        this.gravityDestinationStrategyFactory.setActionWeightsFile(
            testFolder().path("dummy_action_weights.csv")
        );
        this.gravityDestinationStrategyFactory.setMaxTripDurationFile(
            testFolder().path("dummy_boats.csv")
        );
    }

}
