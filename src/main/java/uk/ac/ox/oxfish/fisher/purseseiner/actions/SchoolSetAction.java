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
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public abstract class SchoolSetAction<B extends LocalBiology> extends AbstractSetAction<B> {

    SchoolSetAction(
        final B targetBiology,
        final Fisher fisher,
        final double setDuration
    ) {
        super(targetBiology, fisher, setDuration);
    }

    @Override
    boolean checkSuccess() {
        // school sets are always successful since we're sampling from an empirical distribution
        // that includes failed sets with zeros for all species.
        return true;
    }

    @Override
    public void reactToSuccessfulSet(final FishState fishState, final SeaTile locationOfSet) {
        // Remove the catches from the underlying biology:
        final Catch catchObject = makeCatch(getTargetBiology());
        // Note that, despite "biomass" in the name, the following method
        // can react to abundance-based catches:
        locationOfSet.reactToThisAmountOfBiomassBeingFished(
            catchObject,
            catchObject,
            fishState.getBiology()
        );
    }

    @Override
    public void reactToFailedSet(final FishState fishState, final SeaTile locationOfSet) {
        throw new IllegalStateException("School sets shouldn't 'fail'.");
    }

    @Override
    void notify(final FadManager<?, ?> fadManager) {
        fadManager.reactTo(this);
    }

    private Catch makeCatch(final B biology) {
        if (biology instanceof VariableBiomassBasedBiology) {
            return new Catch((VariableBiomassBasedBiology) biology);
        } else if (biology instanceof AbundanceLocalBiology) {
            return new Catch((AbundanceLocalBiology) biology);
        } else {
            throw new IllegalArgumentException();
        }
    }

}
