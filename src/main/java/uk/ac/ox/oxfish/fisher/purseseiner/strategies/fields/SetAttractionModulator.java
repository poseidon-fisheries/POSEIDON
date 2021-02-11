/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;

import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear.getPurseSeineGear;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class SetAttractionModulator implements AttractionModulator {

    private final Class<? extends AbstractSetAction> actionClass;
    private final DoubleUnaryOperator pctHoldAvailableModulationFunction;
    private final DoubleUnaryOperator pctSetsRemainingModulationFunction;
    private final DoubleUnaryOperator timeSinceLastVisitModulationFunction;

    public SetAttractionModulator(
        final Class<? extends AbstractSetAction> actionClass,
        final double pctHoldAvailableLogisticMidpoint,
        final double pctHoldAvailableLogisticSteepness,
        final double pctSetsRemainingLogisticMidpoint,
        final double pctSetsRemainingLogisticSteepness
    ) {
        this(
            actionClass,
            new LogisticFunction(pctHoldAvailableLogisticMidpoint, pctHoldAvailableLogisticSteepness),
            new LogisticFunction(pctSetsRemainingLogisticMidpoint, pctSetsRemainingLogisticSteepness),
            __ -> 1.0 // for when time since last visit doesn't matter (i.e., for sets on own FADs)
        );
    }

    public SetAttractionModulator(
        final Class<? extends AbstractSetAction> actionClass,
        final DoubleUnaryOperator pctHoldAvailableModulationFunction,
        final DoubleUnaryOperator pctSetsRemainingModulationFunction,
        final DoubleUnaryOperator timeSinceLastVisitModulationFunction
    ) {
        this.actionClass = actionClass;
        this.pctHoldAvailableModulationFunction = pctHoldAvailableModulationFunction;
        this.pctSetsRemainingModulationFunction = pctSetsRemainingModulationFunction;
        this.timeSinceLastVisitModulationFunction = timeSinceLastVisitModulationFunction;
    }

    public SetAttractionModulator(
        final Class<? extends AbstractSetAction> actionClass,
        final double pctHoldAvailableLogisticMidpoint,
        final double pctHoldAvailableLogisticSteepness,
        final double pctSetsRemainingLogisticMidpoint,
        final double pctSetsRemainingLogisticSteepness,
        final double timeSinceLastVisitLogisticMidpoint,
        final double timeSinceLastVisitLogisticSteepness
    ) {
        this(
            actionClass,
            new LogisticFunction(pctHoldAvailableLogisticMidpoint, pctHoldAvailableLogisticSteepness),
            new LogisticFunction(pctSetsRemainingLogisticMidpoint, pctSetsRemainingLogisticSteepness),
            new LogisticFunction(timeSinceLastVisitLogisticMidpoint, timeSinceLastVisitLogisticSteepness)
        );
    }

    @Override
    public double modulate(
        final int x,
        final int y,
        final int t,
        final Fisher fisher
    ) {
        if (!canFishThere(x, y, t, fisher))
            return 0;
        else {
            final double modulatedPctHoldAvailable =
                pctHoldAvailableModulationFunction.applyAsDouble(1 - fisher.getHold().getPercentageFilled());
            final double modulatedTimeSinceLastVisit = getPurseSeineGear(fisher)
                .getLastVisit(fisher.getLocation().getGridLocation())
                .map(lastVisit -> timeSinceLastVisitModulationFunction.applyAsDouble(t - lastVisit))
                .orElse(1.0);
            final double modulatedPctSetsRemaining =
                pctSetsRemainingModulationFunction.applyAsDouble(pctSetsRemaining(fisher));
            return modulatedPctHoldAvailable * modulatedTimeSinceLastVisit * modulatedPctSetsRemaining;
        }
    }

    private double pctSetsRemaining(final Fisher fisher) {
        return getFadManager(fisher)
            .getActionSpecificRegulations()
            .getSetLimits()
            .map(reg -> reg.getPctLimitRemaining(fisher))
            .orElse(1.0);
    }

}
