/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetActionMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.DoubleSupplier;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadSetOpportunityGenerator<
    B extends LocalBiology,
    F extends Fad,
    A extends AbstractFadSetAction>
    extends SetOpportunityGenerator<B, A> {

    private final BiPredicate<Fisher, F> fadPredicate;
    private final FadSetActionMaker<A> actionMaker;

    public FadSetOpportunityGenerator(
        final BiPredicate<Fisher, F> fadPredicate,
        final FadSetActionMaker<A> actionMaker,
        final DoubleSupplier durationSampler
    ) {
        super(durationSampler);
        this.fadPredicate = fadPredicate;
        this.actionMaker = actionMaker;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<A> apply(final Fisher fisher) {
        return getFadManager(fisher)
            .getFadsAt(fisher.getLocation())
            .filter(Fad::isActive)
            .filter(fad -> fadPredicate.test(fisher, (F) fad))
            .map(fad -> actionMaker.make(fad, fisher, getDurationSampler().getAsDouble()))
            .collect(toImmutableList());
    }
}
