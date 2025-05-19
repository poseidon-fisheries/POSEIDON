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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import static java.lang.Math.exp;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class GlobalSetAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctHoldAvailableModulationFunction;
    private final DoubleUnaryOperator pctSetsRemainingModulationFunction;

    public GlobalSetAttractionModulator(
        final DoubleUnaryOperator pctHoldAvailableModulationFunction,
        final DoubleUnaryOperator pctSetsRemainingModulationFunction
    ) {
        this.pctHoldAvailableModulationFunction = pctHoldAvailableModulationFunction;
        this.pctSetsRemainingModulationFunction = pctSetsRemainingModulationFunction;
    }

    @Override
    public double modulate(final Fisher fisher) {
        final double modulatedPctHoldAvailable =
            pctHoldAvailableModulationFunction.applyAsDouble(
                1 - fisher.getHold().getPercentageFilled());
        final double modulatedPctSetsRemaining =
            pctSetsRemainingModulationFunction.applyAsDouble(mapSetsRemainingToZeroOneInterval(fisher));
        return modulatedPctHoldAvailable * modulatedPctSetsRemaining;
    }

    private static double mapSetsRemainingToZeroOneInterval(final Fisher fisher) {

        // The current implementation for this is fairly arbitrary, but I think
        // it could serve as a good base for modulation functions if we decide
        // to revisit the gravity algorithm.
        final FadManager fadManager = getFadManager(fisher);
        final double k = 0.01;
        final double x0 = -2.0;
        final double L = (1 + exp(k * x0)) / exp(k * x0);
        return Arrays
            .stream(ActionClass.values())
            .filter(actionClass -> actionClass != ActionClass.DPL)
            .mapToInt(actionClass ->
                fadManager.numberOfPermissibleActions(actionClass, 100, fisher.grabState().getRegulations())
            )
            .mapToDouble(x ->
                L / (1 + exp(-k * (x - x0))) - L / (1 + exp(k * x0))
            )
            .max()
            .orElse(0);
    }

}
