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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public abstract class AbstractFadSetAction extends AbstractSetAction {

    private final Fad fad;

    public AbstractFadSetAction(
        final Fisher fisher,
        final Fad fad,
        final double exponentialSteepnessCoefficient
    ) {
        super(
            fisher,
            1.0, // TODO
            fad.getBiology(),
            exponentialSteepnessCoefficient,
            true // TODO
        );
        this.fad = fad;
    }

    public Fad getFad() { return fad; }

    @Override boolean checkSuccess() {
        final PurseSeineGear purseSeineGear = (PurseSeineGear) getFisher().getGear();
        return getFisher().grabRandomizer().nextDouble() < purseSeineGear.getSuccessfulFadSetProbability();
    }

    @Override public void reactToSuccessfulSet(final FishState fishState, final SeaTile locationOfSet) {
        // Nothing to do here since the biomass has already been removed from the ocean
    }

    /**
     * When a FAD set fails, the fish is returned to the underlying sea tile biology.
     */
    @Override public void reactToFailedSet(FishState fishState, SeaTile locationOfSet) {
        fad.releaseFish(fishState.getBiology().getSpecies(), locationOfSet.getBiology());
    }

    @Override void notify(FadManager fadManager) { fadManager.reactTo(this); }

    // TODO: get rid of that and check on subclass instead
    public abstract boolean isOwnFad();

}
