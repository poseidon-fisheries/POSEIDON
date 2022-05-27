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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishAttractor;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class ExpirationDecoratorFadInitializer<B extends LocalBiology, F extends Fad<B, F>>
        implements FadInitializer<B,F> {

    private final int daysOfActivity;

    private FadInitializer<B,F> delegate;

    public ExpirationDecoratorFadInitializer(
            int daysOfActivity, FadInitializer<B, F> delegate) {
        this.daysOfActivity = daysOfActivity;
        this.delegate = delegate;
    }

    @Override
    public F makeFad(
            @NotNull FadManager<B, F> fadManager, @Nullable Fisher owner, @NotNull SeaTile initialLocation) {
        F fad = delegate.makeFad(fadManager, owner, initialLocation);
        fad.setDaysBeforeTurningOff(daysOfActivity);
        return fad;
    }
}
