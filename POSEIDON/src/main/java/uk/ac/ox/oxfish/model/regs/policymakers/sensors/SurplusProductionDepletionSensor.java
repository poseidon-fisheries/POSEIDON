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

package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.policymakers.SurplusProductionResult;
import uk.ac.ox.oxfish.model.regs.policymakers.SurplusProductionStockAssessment;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.function.Function;

/**
 * returns the current depletion level according to a surplus-production model assessment
 */
public class SurplusProductionDepletionSensor implements Sensor<FishState, Double> {

    private static final long serialVersionUID = 3818329073309046080L;
    private final SurplusProductionStockAssessment assessment;

    public SurplusProductionDepletionSensor(
        final double[] carryingCapacityBounds, final double[] logisticGrowthBounds,
        final double[] catchabilityBounds, final String indicatorColumnName,
        final String catchColumnName
    ) {

        assessment = new SurplusProductionStockAssessment(
            carryingCapacityBounds,
            logisticGrowthBounds,
            catchabilityBounds,
            indicatorColumnName,
            catchColumnName
        );
    }


    @Override
    public Double scan(final FishState system) {

        final SurplusProductionResult assessmentResult = assessment.scan(system);
        if (assessmentResult == null)
            return Double.NaN;
        else
            return assessmentResult.getDepletion()[assessmentResult.getDepletion().length];


    }

    public Function<Double, Double> getCatchTransformer() {
        return assessment.getCatchTransformer();
    }

    public void setCatchTransformer(final Function<Double, Double> catchTransformer) {
        assessment.setCatchTransformer(catchTransformer);
    }

    public Function<Double, Double> getIndicatorTransformer() {
        return assessment.getIndicatorTransformer();
    }

    public void setIndicatorTransformer(final Function<Double, Double> indicatorTransformer) {
        assessment.setIndicatorTransformer(indicatorTransformer);
    }


}
