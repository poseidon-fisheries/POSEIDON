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

import java.util.function.BiPredicate;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetActionMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.DurationSampler;

public class AbundanceFadSetOpportunityGenerator<
    A extends AbstractFadSetAction<AbundanceLocalBiology, AbundanceFad>
    > extends FadSetOpportunityGenerator<AbundanceLocalBiology, AbundanceFad, A> {

    public AbundanceFadSetOpportunityGenerator(
        final BiPredicate<Fisher, AbundanceFad> fadPredicate,
        final FadSetActionMaker<AbundanceLocalBiology, AbundanceFad, A> actionMaker,
        final DurationSampler durationSampler
    ) {
        super(AbundanceFad.class, fadPredicate, actionMaker, durationSampler);
    }
}