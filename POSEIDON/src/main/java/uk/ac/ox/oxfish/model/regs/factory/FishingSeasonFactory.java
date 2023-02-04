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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates fishing seasons of any amount
 */
public class FishingSeasonFactory implements AlgorithmFactory<FishingSeason>
{

    /**
     * number of days one is allowed to fish. It gets rounded
     */
    private DoubleParameter seasonLength = new FixedDoubleParameter(200);


    public FishingSeasonFactory() {
    }

    public FishingSeasonFactory(double seasonLength, boolean respectMPA) {
        this.seasonLength =new FixedDoubleParameter(seasonLength);
        this.respectMPA = respectMPA;
    }

    /**
     * is the mpa to be respected?
     */
    private boolean respectMPA = true;


    /**
     * creates a fishing season regulation for this agent
     */
    @Override
    public FishingSeason apply(FishState state) {
        final int length = seasonLength.apply(state.random).intValue();
        return new FishingSeason(respectMPA,length);
    }

    public DoubleParameter getSeasonLength() {
        return seasonLength;
    }

    public void setSeasonLength(DoubleParameter seasonLength) {
        this.seasonLength = seasonLength;
    }

    public boolean isRespectMPA() {
        return respectMPA;
    }

    public void setRespectMPA(boolean respectMPA) {
        this.respectMPA = respectMPA;
    }
}
