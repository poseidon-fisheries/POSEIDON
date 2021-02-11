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
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimits;

import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class DeploymentLocationsAttractionModulator implements AttractionModulator {

    private final DoubleUnaryOperator pctActiveFadsLimitModulationFunction;

    public DeploymentLocationsAttractionModulator(
        final double pctActiveFadsLimitLogisticMidpoint,
        final double pctActiveFadsLimitLogisticSteepness
    ) {
        this(
            new LogisticFunction(pctActiveFadsLimitLogisticMidpoint, pctActiveFadsLimitLogisticSteepness)
        );
    }

    private DeploymentLocationsAttractionModulator(
        final DoubleUnaryOperator pctActiveFadsLimitModulationFunction
    ) {
        this.pctActiveFadsLimitModulationFunction = pctActiveFadsLimitModulationFunction;
    }

    @Override
    public double modulate(
        final int x, final int y, final int t, final Fisher fisher
    ) {
        return canFishThere(x, y, t, fisher)
            ? pctActiveFadsLimitModulationFunction.applyAsDouble(1 - getPctActiveFads(fisher))
            : 0;
    }

    private double getPctActiveFads(final Fisher fisher) {
        final FadManager fadManager = getFadManager(fisher);
        return fadManager
            .getActionSpecificRegulations()
            .getActiveFadLimits()
            .map(reg -> (double) fadManager.getNumDeployedFads() / reg.getLimit(fisher))
            .orElse(0.0);
    }

}
