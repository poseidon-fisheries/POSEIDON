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

package uk.ac.ox.oxfish.fisher.strategies.discarding;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Discards everything that is not sellable (according to the regulation object)
 * Created by carrknight on 5/2/17.
 */
public class DiscardAllUnsellable implements DiscardingStrategy {

    private double safetyBuffer = FishStateUtilities.EPSILON;

    /**
     * This strategy decides the new "catch" object, that is how much of the fish we are actually going to store
     * given how much we caught!
     *
     * @param where             where did we do the fishing
     * @param who               who did the fishing
     * @param fishCaught        the catch before any discard
     * @param hoursSpentFishing how many hours have we spent fishing
     * @param regulation        the regulation the fisher is subject to
     * @param model
     *@param random
     * @return a catch object holding how much we are actually going to load in the boat. The difference between
     * what is returned and the 'fishCaught' variable is the implicit discard
     */
    @Override
    public Catch chooseWhatToKeep(
            SeaTile where, Fisher who, Catch fishCaught, int hoursSpentFishing, Regulation regulation,
            FishState model, MersenneTwisterFast random) {

        Preconditions.checkArgument(!fishCaught.hasAbundanceInformation(),
                                    "This discard strategy erases abundance information!");

        //go through the regulation object and return everything that isn't kept
        double[] saved  = new double[fishCaught.numberOfSpecies()];
        for(Species species : model.getSpecies())
        {
            //never hold on to more than it is sellable
            saved[species.getIndex()] = Math.max(0,
                                                 Math.min(
                                                         fishCaught.getWeightCaught(species),
                                                         regulation.maximumBiomassSellable(who,species,model)
                                                                 - safetyBuffer
                                                                 - who.getTotalWeightOfCatchInHold(species)

                                                 )
            );
        }

        return new Catch(saved);

    }


    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * Getter for property 'safetyBuffer'.
     *
     * @return Value for property 'safetyBuffer'.
     */
    public double getSafetyBuffer() {
        return safetyBuffer;
    }

    /**
     * Setter for property 'safetyBuffer'.
     *
     * @param safetyBuffer Value to set for property 'safetyBuffer'.
     */
    public void setSafetyBuffer(double safetyBuffer) {
        this.safetyBuffer = safetyBuffer;
    }
}
