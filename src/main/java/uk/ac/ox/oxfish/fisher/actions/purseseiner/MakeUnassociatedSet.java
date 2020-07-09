/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.actions.purseseiner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.model.FishState;

import static java.util.stream.IntStream.range;

public class MakeUnassociatedSet extends SetAction {

    private static final String ACTION_NAME = "unassociated sets";

    public MakeUnassociatedSet(FishState model, Fisher fisher) {
        super(model, fisher);
    }

    public String getActionName() { return ACTION_NAME; }

    @Override void notifyFadManager() { getFadManager().reactTo(this); }

    @Override boolean isSuccessful(PurseSeineGear purseSeineGear, MersenneTwisterFast rng) {
        // unassociated sets are always successful since we're sampling from an empirical distribution
        // that includes failed sets with zeros for all species.
        return true;
    }

    /**
     * The target biology of an unassociated set has to be created on the fly. Note that, since this is only done
     * in the case of a successful set, there is no need for a separate method to release the fish if it fails.
     */
    @SuppressWarnings("UnstableApiUsage") @Override public LocalBiology targetBiology(
        PurseSeineGear purseSeineGear,
        GlobalBiology globalBiology,
        LocalBiology seaTileBiology,
        MersenneTwisterFast rng
    ) {
        final double[] availableBiomass =
            range(0, globalBiology.getSize())
                .mapToDouble(i -> seaTileBiology.getBiomass(globalBiology.getSpecie(i)))
                .toArray();

        final double[] biomassCaught =
            purseSeineGear
                .getUnassociatedCatchSampler()
                .next(availableBiomass)
                .toArray();

        final VariableBiomassBasedBiology unassociatedSetBiology =
            new BiomassLocalBiology(biomassCaught, biomassCaught);

        // Remove the catches from the underlying biology:
        final Catch catchObject = new Catch(unassociatedSetBiology.getCurrentBiomass());
        seaTileBiology.reactToThisAmountOfBiomassBeingFished(catchObject, catchObject, globalBiology);
        return unassociatedSetBiology;
    }

    @Override public Action actionAfterSet() { return new Arriving(); }

}
