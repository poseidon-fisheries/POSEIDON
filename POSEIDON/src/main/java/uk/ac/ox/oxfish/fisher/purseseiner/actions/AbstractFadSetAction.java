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

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

public abstract class AbstractFadSetAction<B extends LocalBiology, F extends AbstractFad<B, F>>
    extends AbstractSetAction<B> implements FadRelatedAction {

    private final F fad;

    public AbstractFadSetAction(
        final F fad,
        final Fisher fisher,
        final double duration
    ) {
        super(
            fad.getBiology(),
            fisher,
            duration
        );
        this.fad = fad;
    }

    public F getFad() {
        return fad;
    }

    @Override
    public ActionResult act(
        FishState fishState, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        assert fisher.getLocation() == fad.getLocation();
        return super.act(fishState, fisher, regulation, hoursLeft);
    }

    @Override
    boolean checkSuccess() {
        final PurseSeineGear<?, ?> purseSeineGear = (PurseSeineGear<?, ?>) getFisher().getGear();
        return getFisher().grabRandomizer().nextDouble()
            < purseSeineGear.getSuccessfulFadSetProbability();
    }

    @Override
    public void reactToSuccessfulSet(final FishState fishState, final SeaTile locationOfSet) {
        fad.reactToBeingFished(fishState, getFisher(), locationOfSet);
        // Nothing to do here since the biomass has already been removed from the ocean
        fishState.getFadMap().destroyFad(fad);
        getFadManager(getFisher()).putFadBackInStock();
    }

    /**
     * When a FAD set fails, the fish is returned to the underlying sea tile biology.
     */
    @Override
    public void reactToFailedSet(final FishState fishState, final SeaTile locationOfSet) {
        fad.releaseFish(fishState.getBiology().getSpecies(), locationOfSet.getBiology());
        fishState.getFadMap().destroyFad(fad);
        getFadManager(getFisher()).putFadBackInStock();
    }

    @Override
    void notify(final FadManager<?, ?> fadManager) {
        fadManager.reactTo(this);
    }

    // TODO: get rid of that and check on subclass instead
    public abstract boolean isOwnFad();

}