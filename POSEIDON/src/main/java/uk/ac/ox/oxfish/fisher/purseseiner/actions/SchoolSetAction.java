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

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Queue;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public abstract class SchoolSetAction<B extends LocalBiology> extends AbstractSetAction {

    private final B targetBiology;
    private final CatchMaker<B> catchMaker;
    private final Queue<B> sourceBiologies;

    SchoolSetAction(
        final B targetBiology,
        final Fisher fisher,
        final double setDuration,
        final Collection<B> sourceBiologies,
        final CatchMaker<B> catchMaker
    ) {
        super(targetBiology, fisher, fisher.getLocation(), setDuration);
        this.targetBiology = targetBiology;
        this.catchMaker = catchMaker;
        this.sourceBiologies = new ArrayDeque<>(sourceBiologies);
    }

    @Override
    boolean checkSuccess() {
        // school sets are always successful since we're sampling from an empirical distribution
        // that includes failed sets with zeros for all species.
        return true;
    }

    @Override
    public void reactToSuccessfulSet(final FishState fishState, final SeaTile locationOfSet) {

        B biology = sourceBiologies.poll();
        B uncaught = targetBiology;
        while (!(biology == null || isEmpty(uncaught, fishState.getSpecies()))) {
            final Entry<Catch, B> caughtAndUncaught = catchMaker.apply(biology, uncaught);
            final Catch caught = caughtAndUncaught.getKey();
            biology.reactToThisAmountOfBiomassBeingFished(caught, caught, fishState.getBiology());
            uncaught = caughtAndUncaught.getValue();
            biology = sourceBiologies.poll();
        }
    }

    private boolean isEmpty(final LocalBiology biology, final Iterable<Species> species) {
        return biology.getTotalBiomass(species) < EPSILON;
    }

    @Override
    public void reactToFailedSet(final FishState fishState, final SeaTile locationOfSet) {
        throw new IllegalStateException("School sets shouldn't 'fail'.");
    }

    @Override
    void notify(final FadManager fadManager) {
        fadManager.reactTo(this);
    }

}
