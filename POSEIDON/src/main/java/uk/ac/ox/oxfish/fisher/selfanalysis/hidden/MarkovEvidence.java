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

package uk.ac.ox.oxfish.fisher.selfanalysis.hidden;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * The markov evidence, relating (at least for now) to a catch (or better a proportion of total biomass available)
 * Created by carrknight on 6/27/16.
 */
public class MarkovEvidence {

    private final SeaTile tile;

    private final double observation;


    public MarkovEvidence(SeaTile tile, double observation)
    {

        Preconditions.checkArgument(observation<=1);
        Preconditions.checkArgument(observation>0);
        this.tile = tile;
        this.observation = observation;
    }

    public MarkovEvidence(SeaTile tile, double biomassObserved, double totalBiomassAvailable)
    {


        this.tile = tile;
        this.observation = biomassObserved/totalBiomassAvailable;
    }

    /**
     * Getter for property 'tile'.
     *
     * @return Value for property 'tile'.
     */
    public SeaTile getTile() {
        return tile;
    }

    /**
     * Getter for property 'observation'.
     *
     * @return Value for property 'observation'.
     */
    public double getObservation() {
        return observation;
    }
}
