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
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionFieldsSupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesSupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerAbundanceFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadMapFactory;
import uk.ac.ox.oxfish.geography.fads.LinearAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.FromSimpleFilePortInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.YearlyMarketMapFromPriceFileFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasFromFolderFactory;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoAbundanceScenario extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {

    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFromFileFactory(
            getInputFolder().path("abundance", "selectivity.csv"),
            getSpeciesCodesSupplier()
        );

    private PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> purseSeinerFleetFactory =
        new PurseSeinerFleetFactory<>(
            getInputFolder().path("boats.csv"),
            getInputFolder().path("costs.csv"),
            new AbundancePurseSeineGearFactory(
                new LinearAbundanceFadInitializerFactory(
                    getAbundanceFiltersFactory(),
                    getSpeciesCodesSupplier(),
                    "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
                )
            ),
            new FadRefillGearStrategyFactory(
                getInputFolder().path("max_deployments.csv")
            ),
            new GravityDestinationStrategyFactory(
                getInputFolder().path("action_weights.csv"),
                getInputFolder().path("boats.csv"),
                new AttractionFieldsSupplier(
                    new LocationValuesSupplier(
                        getInputFolder().path("location_values.csv")
                    ),
                    getInputFolder().path("max_current_speeds.csv")
                )
            ),
            new PurseSeinerAbundanceFishingStrategyFactory(
                getSpeciesCodesSupplier(),
                getInputFolder().path("action_weights.csv"),
                new AbundanceCatchSamplersFactory(
                    getSpeciesCodesSupplier(),
                    getAbundanceFiltersFactory(),
                    getInputFolder().path("set_samples.csv")
                ),
                new SetDurationSamplersFactory(
                    getInputFolder().path("set_durations.csv")
                ),
                getInputFolder().path("max_current_speeds.csv"),
                getInputFolder().path("set_compositions.csv")
            ),
            new StandardIattcRegulationsFactory(
                new ProtectedAreasFromFolderFactory(
                    getInputFolder().path("regions"),
                    "region_tags.csv"
                )
            ),
            new PurseSeinerDepartingStrategyFactory(),
            new YearlyMarketMapFromPriceFileFactory(
                getInputFolder().path("prices.csv"),
                getSpeciesCodesSupplier()
            ),
            new FromSimpleFilePortInitializer(
                TARGET_YEAR,
                getInputFolder().path("ports.csv")
            )
        );

    public PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> getPurseSeinerFleetFactory() {
        return purseSeinerFleetFactory;
    }

    @SuppressWarnings("unused")
    public void setPurseSeinerFleetFactory(final PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> purseSeinerFleetFactory) {
        this.purseSeinerFleetFactory = purseSeinerFleetFactory;
    }

    public EpoAbundanceScenario() {
        setBiologicalProcessesFactory(
            new AbundanceProcessesFactory(getInputFolder().path("abundance"), getSpeciesCodesSupplier())
        );
        setFadMapFactory(new AbundanceFadMapFactory(getCurrentPatternMapSupplier()));
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {
        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);
        scenarioPopulation.getPopulation().addAll(
            purseSeinerFleetFactory.makeFishers(fishState, TARGET_YEAR)
        );
        plugins.forEach(plugin -> fishState.registerStartable(plugin.apply(fishState)));

        return scenarioPopulation;
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        purseSeinerFleetFactory.useDummyData(testFolder());
    }

}
