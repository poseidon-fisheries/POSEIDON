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

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetActionMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.DurationSampler;

import java.util.function.BiPredicate;

public class BiomassFadSetOpportunityGenerator<
    A extends FadSetAction<BiomassLocalBiology>
    > extends FadSetOpportunityGenerator<BiomassLocalBiology, BiomassAggregatingFad, A> {

    public BiomassFadSetOpportunityGenerator(
        final BiPredicate<Fisher, BiomassAggregatingFad> fadPredicate,
        final FadSetActionMaker<BiomassLocalBiology, A> actionMaker,
        final DurationSampler durationSampler
    ) {
        super(fadPredicate, actionMaker, durationSampler);
    }
}
