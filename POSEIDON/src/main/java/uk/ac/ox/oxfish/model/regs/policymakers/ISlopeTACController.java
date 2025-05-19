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

package uk.ac.ox.oxfish.model.regs.policymakers;

import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ISlope;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class ISlopeTACController implements AlgorithmFactory<AdditionalStartable> {


    private String indicatorColumnName = "Species 0 CPUE";
    private String catchColumnName = "Species 0 Landings";

    private DoubleParameter gainLambdaParameter = new FixedDoubleParameter(0.4);

    private DoubleParameter precautionaryScaling = new FixedDoubleParameter(0.8);

    private int interval = 5;
    private int startingYear = 10;


    @Override
    public AdditionalStartable apply(FishState fishState) {
        return model -> fishState.scheduleOnceInXDays(
            (Steppable) simState -> {
                TargetToTACController controller = new TargetToTACController(
                    new ISlope(
                        catchColumnName,
                        indicatorColumnName,
                        gainLambdaParameter.applyAsDouble(model.getRandom()),
                        precautionaryScaling.applyAsDouble(model.getRandom()),
                        interval
                    )
                );
                controller.start(model);
                controller.step(model);
            },
            StepOrder.DAWN,
            365 * startingYear + 1
        );

    }


    public String getIndicatorColumnName() {
        return indicatorColumnName;
    }

    public void setIndicatorColumnName(String indicatorColumnName) {
        this.indicatorColumnName = indicatorColumnName;
    }

    public String getCatchColumnName() {
        return catchColumnName;
    }

    public void setCatchColumnName(String catchColumnName) {
        this.catchColumnName = catchColumnName;
    }

    public DoubleParameter getGainLambdaParameter() {
        return gainLambdaParameter;
    }

    public void setGainLambdaParameter(DoubleParameter gainLambdaParameter) {
        this.gainLambdaParameter = gainLambdaParameter;
    }

    public DoubleParameter getPrecautionaryScaling() {
        return precautionaryScaling;
    }

    public void setPrecautionaryScaling(DoubleParameter precautionaryScaling) {
        this.precautionaryScaling = precautionaryScaling;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }
}




