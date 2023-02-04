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

import java.util.function.DoubleUnaryOperator;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategy;

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
