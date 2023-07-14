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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EpoPurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionFieldsFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerAbundanceFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogNormalErrorOperatorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.UnreliableFishValueCalculatorFactory;
import uk.ac.ox.oxfish.geography.fads.LinearAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.regulation.EverythingPermitted;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;

import java.util.List;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoGravityAbundanceScenario extends EpoAbundanceScenario {

    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFromFileFactory(
            getInputFolder().path("abundance", "selectivity.csv")
        );

    private PurseSeinerFleetFactory purseSeinerFleetFactory =
        new EpoPurseSeinerFleetFactory(
            getTargetYear(),
            getInputFolder(),
            getSpeciesCodesSupplier(),
            new AbundancePurseSeineGearFactory(
                new EverythingPermitted(),
                new LinearAbundanceFadInitializerFactory(
                    getAbundanceFiltersFactory(),
                    getSpeciesCodesSupplier(),
                    "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
                ),
                new UnreliableFishValueCalculatorFactory(new LogNormalErrorOperatorFactory(
                    new CalibratedParameter(-.2, .2, -.4, .4),
                    new CalibratedParameter(.2, .3, .01, .5)
                ))
            ),
            new GravityDestinationStrategyFactory(
                getTargetYear(),
                getInputFolder().path("action_weights.csv"),
                getInputFolder().path("vessels.csv"),
                new AttractionFieldsFactory(
                    new LocationValuesFactory(
                        getInputFolder().path("location_values.csv"),
                        new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                        new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                        new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                        new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                        getTargetYear()
                    ),
                    getInputFolder().path("max_current_speeds.csv")
                )
            ),
            new PurseSeinerAbundanceFishingStrategyFactory(
                getTargetYear(),
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
            )
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
    List<Fisher> makeFishers(final FishState fishState, final int targetYear) {
        return purseSeinerFleetFactory.makeFishers(fishState, targetYear);
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        purseSeinerFleetFactory.useDummyData(testFolder());
    }

}
