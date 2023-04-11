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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetActionMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadSetOpportunityGenerator<
    B extends LocalBiology,
    F extends Fad<B, F>,
    A extends AbstractFadSetAction<B>>
    extends SetOpportunityGenerator<B, A> {

    private final BiPredicate<Fisher, F> fadPredicate;
    private final FadSetActionMaker<B, A> actionMaker;

    public FadSetOpportunityGenerator(
        final BiPredicate<Fisher, F> fadPredicate,
        final FadSetActionMaker<B, A> actionMaker,
        final DoubleSupplier durationSampler
    ) {
        super(durationSampler);
        this.fadPredicate = fadPredicate;
        this.actionMaker = actionMaker;
    }

    @Override
    public Collection<A> apply(final Fisher fisher) {
        return getFadManager(fisher)
            .getFadsAt(fisher.getLocation())
            .filter((Predicate<Fad<? extends LocalBiology, ? extends Fad<?, ?>>>) Fad::isActive)
            .filter(fad -> fadPredicate.test(fisher, (F) fad))
            .map(fad -> actionMaker.make(fad, fisher, getDurationSampler().getAsDouble()))
            .collect(toImmutableList());
    }
}
