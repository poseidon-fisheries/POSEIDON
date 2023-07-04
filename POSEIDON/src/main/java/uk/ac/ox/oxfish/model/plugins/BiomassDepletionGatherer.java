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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * creates depletion and MSY data collectors by mere division
 */
public class BiomassDepletionGatherer implements AdditionalStartable {


    public static final String DEPLETION_COLUMN_NAME = "Depletion";

    public static final String MSY = "Landings/MSY";


    /**
     * optionally you can get also msy collected
     */
    private final HashMap<String, Double> msy;


    public BiomassDepletionGatherer(HashMap<String, Double> msy) {
        this.msy = msy;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        for (Species species : model.getBiology().getSpecies()) {

            model.getYearlyDataSet().registerGatherer(
                DEPLETION_COLUMN_NAME + " " + species.getName(),
                (Gatherer<FishState>) state -> {

                    double bt = state.getTotalBiomass(species);
                    double k = state.getMap().getAllSeaTilesExcludingLandAsList().stream().mapToDouble(
                        value -> {
                            if (!value.isFishingEvenPossibleHere())
                                return 0d;

                            return ((BiomassLocalBiology) value.getBiology()).getCarryingCapacity(species);
                        }
                    ).sum();
                    return bt / k;


                },
                Double.NaN
            );

        }

        for (Map.Entry<String, Double> msyEntry : msy.entrySet()) {

            Species species = model.getBiology().getSpecie(msyEntry.getKey());
            Preconditions.checkState(species != null);
            model.getYearlyDataSet().registerGatherer(
                MSY + " " + species.getName(),
                (Gatherer<FishState>) state -> {

                    Double landings = FishStateUtilities.generateYearlySum(
                        state.getDailyDataSet().getColumn(
                            species.getName() + " " + AbstractMarket.LANDINGS_COLUMN_NAME
                        )).apply(
                        state);

                    return landings / msyEntry.getValue();


                },
                Double.NaN
            );

        }


    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }
}
