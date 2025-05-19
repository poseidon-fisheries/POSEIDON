/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.NOA;

public class NonAssociatedSetFromLocationValuePlanningModule<B extends LocalBiology>
    extends LocationValuePlanningModule {

    NonAssociatedSetFromLocationValuePlanningModule(
        final Fisher fisher,
        final LocationValues locationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double additionalWaitTime,
        final CatchSampler<B> sampler,
        final CatchMaker<B> catchMaker,
        final GlobalBiology globalBiology,
        final Class<B> localBiologyClass,
        final boolean canPoachFads,
        final int rangeInSeaTiles
    ) {
        this(
            locationValues,
            new CatchSamplerPlannedActionGenerator.NonAssociatedActionGenerator<B>(
                fisher,
                locationValues,
                map,
                random,
                additionalWaitTime,
                sampler,
                catchMaker,
                globalBiology,
                localBiologyClass,
                canPoachFads,
                rangeInSeaTiles
            )
        );
    }

    public NonAssociatedSetFromLocationValuePlanningModule(
        final LocationValues locationValues,
        final CatchSamplerPlannedActionGenerator.NonAssociatedActionGenerator<B> generator
    ) {
        super(locationValues, generator);
    }

    @Override
    public ActionClass getActionClass() {
        return NOA;
    }

}
