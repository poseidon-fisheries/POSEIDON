/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;

public class ExpirationDecoratorFadInitializer<
    B extends LocalBiology,
    F extends Fad<B, F>>
    implements FadInitializer<B, F> {

    private final int daysOfActivity;

    private final FadInitializer<B, F> delegate;

    public ExpirationDecoratorFadInitializer(
        final int daysOfActivity, final FadInitializer<B, F> delegate
    ) {
        this.daysOfActivity = daysOfActivity;
        this.delegate = delegate;
    }

    @Override
    public F makeFad(
        final FadManager<B, F> fadManager,
        final Fisher owner,
        final SeaTile initialLocation,
        final MersenneTwisterFast rng
    ) {
        final F fad = delegate.makeFad(fadManager, owner, initialLocation, rng);
        fad.setDaysBeforeTurningOff(daysOfActivity);
        return fad;
    }
}
