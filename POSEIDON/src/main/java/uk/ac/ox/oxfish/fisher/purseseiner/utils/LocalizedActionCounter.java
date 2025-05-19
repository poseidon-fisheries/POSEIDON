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

package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.poseidon.common.api.Observer;

import java.util.function.Predicate;

/**
 * counts for certain actions (and their total catch) when they pass a predicate. Useful for studying only actions that happen
 * in a specific area.
 * <p>
 * Following convention here assuming that we start ourselves to register gatherers but somebody else has already registered
 * us as observers in the fad manager!
 */
public class LocalizedActionCounter implements Observer<AbstractFadSetAction>, AdditionalStartable {

    private final Counter validActions;

    private final Predicate<AbstractFadSetAction> passThisIfYouWantToBeCounted;


    private final String counterName;

    public LocalizedActionCounter(
        final Predicate<AbstractFadSetAction> passThisIfYouWantToBeCounted,
        final String counterName
    ) {
        this.passThisIfYouWantToBeCounted = passThisIfYouWantToBeCounted;
        this.counterName = counterName;
        validActions = new Counter(IntervalPolicy.EVERY_YEAR);
        validActions.addColumn("Number of Actions");
        validActions.addColumn("Total Catch");
    }


    @Override
    public void observe(final AbstractFadSetAction observable) {
        if (passThisIfYouWantToBeCounted.test(observable)) {
            validActions.count("Number of Actions", 1.0);
            if (observable.getCatchesKept().isPresent())
                validActions.count(
                    "Total Catch",
                    observable.getCatchesKept().get().getTotalWeight()
                );

        }
    }

    @Override
    public void start(final FishState model) {

        validActions.start(model);
        model.getYearlyDataSet().registerGatherer(counterName + ": Number of Actions",
            (Gatherer<FishState>) fishState -> getNumberOfActionsThisYearSoFar(), Double.NaN
        );
        model.getYearlyDataSet().registerGatherer(counterName + ": Total Catch",
            (Gatherer<FishState>) fishState -> getTotalCatchThisYearSoFar(), Double.NaN
        );

    }

    public double getNumberOfActionsThisYearSoFar() {
        return validActions.getColumn("Number of Actions");
    }

    public double getTotalCatchThisYearSoFar() {
        return validActions.getColumn("Total Catch");
    }

    @Override
    public void turnOff() {
        validActions.turnOff();
    }
}
