/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import java.util.Collection;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;

public class DolphinSetActionMaker<B extends LocalBiology>
    implements SchoolSetActionMaker<B, DolphinSetAction<B>> {

    private final CatchMaker<B> catchMaker;

    public DolphinSetActionMaker(final CatchMaker<B> catchMaker) {
        this.catchMaker = catchMaker;
    }

    @Override
    public DolphinSetAction<B> make(
        final B targetBiology,
        final Fisher fisher,
        final double setDuration,
        final Collection<B> sourceBiologies
    ) {
        return new DolphinSetAction<>(
            targetBiology,
            fisher,
            setDuration,
            sourceBiologies,
            catchMaker
        );
    }
}
