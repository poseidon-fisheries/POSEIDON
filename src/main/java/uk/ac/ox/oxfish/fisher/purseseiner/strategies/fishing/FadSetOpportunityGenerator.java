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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.DoubleSupplier;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetActionMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;

public class FadSetOpportunityGenerator<
    B extends LocalBiology,
    F extends Fad<B, F>,
    A extends AbstractFadSetAction<B, F>>
    extends SetOpportunityGenerator<B, A> {

    private final Class<F> fadClass;
    private final BiPredicate<Fisher, F> fadPredicate;
    private final FadSetActionMaker<B, F, A> actionMaker;

    public FadSetOpportunityGenerator(
        final Class<F> fadClass,
        final BiPredicate<Fisher, F> fadPredicate,
        final FadSetActionMaker<B, F, A> actionMaker,
        final DoubleSupplier durationSampler
    ) {
        super(durationSampler);
        this.fadClass = fadClass;
        this.fadPredicate = fadPredicate;
        this.actionMaker = actionMaker;
    }

    @Override
    public Collection<A> apply(final Fisher fisher) {
        return getFadManager(fisher)
            .getFadsAt(fisher.getLocation())
            .filter(fadClass::isInstance)
            .map(fadClass::cast)
            .filter(fad -> fadPredicate.test(fisher, fad))
            .map(fad -> actionMaker.make(fad, fisher, getDurationSampler().getAsDouble()))
            .collect(toImmutableList());
    }
}
