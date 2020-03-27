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
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class MakeFadSet extends SetAction {

    private static String ACTION_NAME = "FAD sets";
    private Fad targetFad;

    public MakeFadSet(FishState model, Fisher fisher, Fad targetFad) {
        super(model, fisher);
        this.targetFad = targetFad;
    }

    public String getActionName() { return ACTION_NAME; }

    @Override void notifyFadManager() { getFadManager().reactTo(this); }

    @Override boolean isSuccessful(PurseSeineGear purseSeineGear, MersenneTwisterFast rng) {
        return rng.nextDouble() < purseSeineGear.getSuccessfulSetProbability();
    }

    @Override
    public boolean isPossible() {
        return super.isPossible() && isFadHere(targetFad);
    }

    @Override public Action actionAfterSet() { return new PickUpFad(getModel(), getFisher(), targetFad); }

    /**
     * When making a FAD set, the target biology is the biology of the target FAD.
     * Fish has already been removed from the underlying sea tiles while the FAD
     * was drifting so we don't need to do that now.
     */
    @Override public LocalBiology targetBiology(
        PurseSeineGear purseSeineGear, GlobalBiology globalBiology, LocalBiology seaTileBiology, MersenneTwisterFast rng
    ) {
        return targetFad.getBiology();
    }

    /**
     * When a FAD set fails, the fish is returned to the underlying sea tile biology.
     */
    @Override public void reactToFailedSet(FishState model, SeaTile locationOfSet) {
        targetFad.releaseFish(model.getBiology().getSpecies(), locationOfSet.getBiology());
    }

}
