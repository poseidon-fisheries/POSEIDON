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

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

import java.util.ArrayDeque;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.stream.Collectors;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.MasonUtils;

public abstract class SchoolSetAction<B extends LocalBiology> extends AbstractSetAction<B> {

    private final CatchMaker<B> catchMaker;

    SchoolSetAction(
        final B targetBiology,
        final Fisher fisher,
        final double setDuration,
        final CatchMaker<B> catchMaker
    ) {
        super(targetBiology, fisher, setDuration);
        this.catchMaker = catchMaker;
    }

    @Override
    boolean checkSuccess() {
        // school sets are always successful since we're sampling from an empirical distribution
        // that includes failed sets with zeros for all species.
        return true;
    }

    @Override
    public void reactToSuccessfulSet(final FishState fishState, final SeaTile locationOfSet) {

        final Bag fads = fishState.getFadMap().fadsAt(locationOfSet);
        fads.shuffle(fishState.getRandom());
        final Queue<B> fadBiologies =
            MasonUtils.<Fad<B, ?>>bagToStream(fads)
                .map(Fad::getBiology)
                .collect(Collectors.toCollection(ArrayDeque::new));

        @SuppressWarnings("unchecked") // shouldn't happen, but let it crash if it does
        B biology = (B) locationOfSet.getBiology();
        B uncaught = getTargetBiology();
        while (!(biology == null || isEmpty(uncaught, fishState.getSpecies()))) {
            final Entry<Catch, B> caughtAndUncaught = catchMaker.apply(biology, uncaught);
            final Catch caught = caughtAndUncaught.getKey();
            biology.reactToThisAmountOfBiomassBeingFished(caught, caught, fishState.getBiology());
            uncaught = caughtAndUncaught.getValue();
            biology = fadBiologies.poll();
        }
    }

    @Override
    public void reactToFailedSet(final FishState fishState, final SeaTile locationOfSet) {
        throw new IllegalStateException("School sets shouldn't 'fail'.");
    }

    @Override
    void notify(final FadManager<?, ?> fadManager) {
        fadManager.reactTo(this);
    }

    private boolean isEmpty(final B biology, final Iterable<Species> species) {
        return biology.getTotalBiomass(species) < EPSILON;
    }

    private Catch makeCatch(final B biology) {
        if (biology instanceof VariableBiomassBasedBiology) {
            return new Catch((VariableBiomassBasedBiology) biology);
        } else if (biology instanceof AbundanceLocalBiology) {
            return new Catch(
                getFisher().grabState().getBiology(),
                (AbundanceLocalBiology) biology
            );
        } else {
            throw new IllegalArgumentException();
        }
    }

}
