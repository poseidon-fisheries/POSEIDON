/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategy;

import java.util.function.DoubleUnaryOperator;

public class PortAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctHoldSpaceLeftModulationFunction;
    private final DoubleUnaryOperator pctTravelTimeLeftModulationFunction;

    public PortAttractionModulator(
        final DoubleUnaryOperator pctHoldSpaceLeftModulationFunction,
        final DoubleUnaryOperator pctTravelTimeLeftModulationFunction
    ) {
        this.pctHoldSpaceLeftModulationFunction = pctHoldSpaceLeftModulationFunction;
        this.pctTravelTimeLeftModulationFunction = pctTravelTimeLeftModulationFunction;
    }

    @Override
    public double modulate(final Fisher fisher) {
        final double pctHoldSpaceLeft = 1.0 - fisher.getHold().getPercentageFilled();
        final double pctTravelTimeLeft = 1.0 - (fisher.getHoursAtSea() / maxTravelTime(fisher));
        return 1.0 -
            pctTravelTimeLeftModulationFunction.applyAsDouble(pctTravelTimeLeft) *
                pctHoldSpaceLeftModulationFunction.applyAsDouble(pctHoldSpaceLeft);
    }

    private static double maxTravelTime(final Fisher fisher) {
        return ((GravityDestinationStrategy) fisher.getDestinationStrategy()).getMaxTravelTime();
    }

}
