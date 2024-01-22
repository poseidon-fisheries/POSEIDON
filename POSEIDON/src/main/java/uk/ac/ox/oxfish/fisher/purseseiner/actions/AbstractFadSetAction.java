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
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public abstract class AbstractFadSetAction
    extends AbstractSetAction implements FadRelatedAction {

    private final Fad fad;

    public AbstractFadSetAction(
        final Fad fad,
        final Fisher fisher,
        final double duration
    ) {
        super(
            fad.getBiology(),
            fisher,
            fad.getLocation(),
            duration
        );
        this.fad = fad;
    }

    public Fad getFad() {
        return fad;
    }

    @Override
    public ActionResult act(
        final FishState fishState,
        final Fisher fisher,
        final Regulation regulation,
        final double hoursLeft
    ) {
        assert fisher.getLocation() == fad.getLocation();
        return super.act(fishState, fisher, regulation, hoursLeft);
    }

    @Override
    boolean checkSuccess() {
        final PurseSeineGear purseSeineGear = (PurseSeineGear) getFisher().getGear();
        return getFisher().grabRandomizer().nextDouble()
            < purseSeineGear.getSuccessfulFadSetProbability();
    }

    @Override
    public void reactToSuccessfulSet(
        final FishState fishState,
        final SeaTile locationOfSet
    ) {
        fad.reactToBeingFished(fishState, getFisher(), locationOfSet);
        // Nothing to do here since the biomass has already been removed from the ocean
        fishState.getFadMap().destroyFad(fad);
        getFadManager(getFisher()).putFadBackInStock();
    }

    /**
     * When a FAD set fails, the fish is returned to the underlying sea tile biology.
     */
    @Override
    public void reactToFailedSet(
        final FishState fishState,
        final SeaTile locationOfSet
    ) {
        fad.releaseFishIntoTile(fishState.getBiology().getSpecies(), locationOfSet.getBiology());
        fishState.getFadMap().destroyFad(fad);
        getFadManager(getFisher()).putFadBackInStock();
    }

    @Override
    void notify(final FadManager fadManager) {
        fadManager.reactTo(this);
    }

    // TODO: get rid of that and check on subclass instead
    public abstract boolean isOwnFad();

}
