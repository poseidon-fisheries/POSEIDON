/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;

/**
 * weighs each fad by an abstract function and always picks the discretized area that maximizes the SUM
 * of weights
 */
public abstract class PickBestPilePlanningModule extends DiscretizedOwnFadPlanningModule {
    public PickBestPilePlanningModule(final OwnFadSetDiscretizedActionGenerator optionsGenerator) {
        super(optionsGenerator);
    }

    @Override
    protected PlannedAction chooseFadSet(
        final Plan currentPlanSoFar, final Fisher fisher,
        final FishState model, final NauticalMap map,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {

        final int now = model.getStep();

        final List<Entry<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer>> options =
            optionsGenerator.peekAllFads();

        //if there are no options, don't bother
        if (options == null || options.isEmpty())
            return null;
        //if there is only one option, also don't bother
        if (options.size() == 1) {
            if (options.get(0).getValue() > 0)
                return optionsGenerator.chooseFad(options.get(0).getValue());
            else return null;
        }


        //go through every valid discretized list of fads
        double bestWeight = -1;
        int fadGroupChosen = -1;
        for (final Entry<PriorityQueue<OwnFadSetDiscretizedActionGenerator.ValuedFad>, Integer> option : options) {
            final double weightHere =
                option.getKey().stream().mapToDouble(
                    valuedFad -> {

                        return weighFad(now, valuedFad);
                    }
                ).sum();
            if (weightHere > bestWeight) {
                bestWeight = weightHere;
                fadGroupChosen = option.getValue();
            }
        }


        //all fads are empty, don't bother setting on any!
        if (fadGroupChosen < 0 ||
            fadGroupChosen >= optionsGenerator.getNumberOfGroups())
            return null;
        return optionsGenerator.chooseFad(fadGroupChosen);
    }

    protected abstract double weighFad(
        int currentModelStep,
        OwnFadSetDiscretizedActionGenerator.ValuedFad valuedFad
    );
}
