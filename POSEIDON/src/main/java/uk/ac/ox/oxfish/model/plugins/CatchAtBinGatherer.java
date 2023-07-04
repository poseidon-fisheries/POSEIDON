/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.plugins;


import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;
import uk.ac.ox.oxfish.model.market.FlexibleAbundanceMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * adds time series collecting every year the catch at bin for all species (formally this was in fishstate start!!!)
 */
public class CatchAtBinGatherer implements AdditionalStartable {


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        //add counters for catches if there is any need (aggregate catches are counted by fishers, here we want abundance based)
        for (Species species : model.getSpecies())
            if (species.getNumberOfBins() > 0)
                for (int age = 0; age < species.getNumberOfBins(); age++) {
                    String columnName = species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME + FlexibleAbundanceMarket.AGE_BIN_PREFIX + age;
                    int finalAge = age;
                    DataColumn dailyCatches = model.getDailyDataSet().registerGatherer(
                        columnName,
                        (Gatherer<FishState>) state -> {

                            double sum = 0;
                            for (Fisher fisher : state.getFishers()) {
                                sum += fisher.getCountedLandingsPerBin(species, finalAge);
                            }

                            return sum;

                        }, 0
                    );
                    model.getYearlyDataSet().registerGatherer(
                        columnName,
                        FishStateUtilities.generateYearlySum(dailyCatches),
                        0d
                    );

                }
    }
}
