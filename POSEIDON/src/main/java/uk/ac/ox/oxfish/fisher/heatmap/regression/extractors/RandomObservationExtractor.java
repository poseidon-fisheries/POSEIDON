/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * extracts for each observation a random number.
 * Nasty and used only to add noise and make prediction worse
 * Created by carrknight on 7/7/16.
 */
public class RandomObservationExtractor implements ObservationExtractor {


    final private MersenneTwisterFast randomizer;
    private double maxNoise;


    public RandomObservationExtractor(double maxNoise, MersenneTwisterFast randomizer) {
        this.maxNoise = maxNoise;
        this.randomizer = randomizer;
    }


    @Override
    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        return randomizer.nextDouble() * maxNoise;
    }

    /**
     * Getter for property 'maxNoise'.
     *
     * @return Value for property 'maxNoise'.
     */
    public double getMaxNoise() {
        return maxNoise;
    }

    /**
     * Setter for property 'maxNoise'.
     *
     * @param maxNoise Value to set for property 'maxNoise'.
     */
    public void setMaxNoise(double maxNoise) {
        this.maxNoise = maxNoise;
    }
}
