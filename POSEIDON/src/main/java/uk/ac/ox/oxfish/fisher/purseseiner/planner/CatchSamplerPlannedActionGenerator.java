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

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * generates a random dolphin deployment action
 */
public abstract class CatchSamplerPlannedActionGenerator<PA extends PlannedAction, B extends LocalBiology> extends
    DrawFromLocationValuePlannedActionGenerator<PA> {

    private final double additionalWaitTime;

    private final CatchSampler<B> howMuchWeCanFishOutGenerator;

    private final CatchMaker<B> catchMaker;

    private final GlobalBiology biology;
    private final Class<B> localBiologyClass;

    CatchSamplerPlannedActionGenerator(
        final Fisher fisher,
        final LocationValues originalLocationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double additionalWaitTime,
        final CatchSampler<B> howMuchWeCanFishOutGenerator,
        final CatchMaker<B> catchMaker,
        final GlobalBiology biology,
        final Class<B> localBiologyClass
    ) {
        super(fisher, originalLocationValues, map, random);
        this.additionalWaitTime = additionalWaitTime;
        this.howMuchWeCanFishOutGenerator = howMuchWeCanFishOutGenerator;
        this.catchMaker = catchMaker;
        this.biology = biology;
        this.localBiologyClass = localBiologyClass;
    }

    @Override
    protected PA locationToPlannedAction(final SeaTile location) {
        Preconditions.checkState(isReady(), "Did not start the deploy generator yet!");
        return
            turnSeaTilePickedIntoAction(
                howMuchWeCanFishOutGenerator,
                location,
                additionalWaitTime,
                catchMaker,
                biology,
                localBiologyClass
            );
    }

    abstract public PA turnSeaTilePickedIntoAction(
        CatchSampler<B> howMuchWeCanFishOutGenerator,
        SeaTile tile,
        double additionalWaitTime,
        CatchMaker<B> catchMaker,
        GlobalBiology biology,
        Class<B> localBiologyClass
    );

    public static class DolphinActionGenerator<B extends LocalBiology>
        extends CatchSamplerPlannedActionGenerator<PlannedAction.DolphinSet<B>, B> {

        private final int rangeInSeaTiles;

        DolphinActionGenerator(
            final Fisher fisher,
            final LocationValues originalLocationValues,
            final NauticalMap map,
            final MersenneTwisterFast random,
            final double additionalWaitTime,
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final CatchMaker<B> catchMaker,
            final GlobalBiology globalBiology,
            final Class<B> localBiologyClass,
            final int rangeInSeaTiles
        ) {
            super(
                fisher,
                originalLocationValues,
                map,
                random,
                additionalWaitTime,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                globalBiology,
                localBiologyClass
            );
            this.rangeInSeaTiles = rangeInSeaTiles;
        }

        @Override
        public PlannedAction.DolphinSet<B> turnSeaTilePickedIntoAction(
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final SeaTile tile,
            final double additionalWaitTime,
            final CatchMaker<B> catchMaker,
            final GlobalBiology biology,
            final Class<B> localBiologyClass
        ) {
            return new PlannedAction.DolphinSet<>(
                tile,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                additionalWaitTime,
                false,
                rangeInSeaTiles,
                localBiologyClass
            );
        }
    }

    public static class NonAssociatedActionGenerator<B extends LocalBiology>
        extends CatchSamplerPlannedActionGenerator<PlannedAction.NonAssociatedSet<B>, B> {

        private final boolean canPoachFads;

        private final int rangeInSeaTiles;

        NonAssociatedActionGenerator(
            final Fisher fisher,
            final LocationValues originalLocationValues,
            final NauticalMap map,
            final MersenneTwisterFast random,
            final double additionalWaitTime,
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final CatchMaker<B> catchMaker,
            final GlobalBiology globalBiology,
            final Class<B> localBiologyClass,
            final boolean canPoachFads,
            final int rangeInSeaTiles
        ) {
            super(
                fisher,
                originalLocationValues,
                map,
                random,
                additionalWaitTime,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                globalBiology,
                localBiologyClass
            );
            this.canPoachFads = canPoachFads;
            this.rangeInSeaTiles = rangeInSeaTiles;
        }

        @Override
        public PlannedAction.NonAssociatedSet<B> turnSeaTilePickedIntoAction(
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final SeaTile tile,
            final double additionalWaitTime,
            final CatchMaker<B> catchMaker,
            final GlobalBiology biology,
            final Class<B> localBiologyClass
        ) {
            return new PlannedAction.NonAssociatedSet<>(
                tile,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                additionalWaitTime,
                canPoachFads,
                rangeInSeaTiles,
                localBiologyClass
            );
        }
    }
}

