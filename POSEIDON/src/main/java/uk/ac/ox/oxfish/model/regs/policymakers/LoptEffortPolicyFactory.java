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


import com.google.common.base.Preconditions;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Map;

public class LoptEffortPolicyFactory implements AlgorithmFactory<AdditionalStartable> {


    private String meanLengthColumnName = "Mean Length Caught " + "Lutjanus malabaricus" + " " + "spr_agent_total";

    private DoubleParameter targetLength = new FixedDoubleParameter(60);

    private DoubleParameter bufferValue = new FixedDoubleParameter(0.9);

    private DoubleParameter howManyYearsToLookBackTo = new FixedDoubleParameter(5);

    private String effortDefinition = "season";


    private boolean blockEntryWhenSeasonIsNotFull = false;

    private int startingYear = 7;

    @Override
    public AdditionalStartable apply(FishState fishState) {


        final Map<String, Actuator<FishState, Double>> effortActuators = LBSPREffortPolicyFactory.EFFORT_ACTUATORS;
        Preconditions.checkArgument(
            effortActuators.containsKey(effortDefinition),
            "The valid effort actuators are " + effortActuators.keySet()
        );


        return model -> fishState.scheduleOnceInXDays(
            (Steppable) simState -> {
                LoptEffortPolicy lopt = new LoptEffortPolicy(
                    meanLengthColumnName,
                    1d - bufferValue.applyAsDouble(fishState.getRandom()),
                    targetLength.applyAsDouble(fishState.getRandom()),
                    (int) howManyYearsToLookBackTo.applyAsDouble(fishState.getRandom()),
                    effortActuators.get(effortDefinition),
                    blockEntryWhenSeasonIsNotFull
                );
                lopt.start(model);
                lopt.step(model);


                //creaqte also a collector
                fishState.getYearlyDataSet().registerGatherer(
                    "LoptEffortPolicy output",
                    (Gatherer<FishState>) fishState1 -> lopt.getTheoreticalSuggestedEffort(),
                    Double.NaN
                );
            },
            StepOrder.DAWN,
            365 * startingYear + 1
        );
    }


    public String getMeanLengthColumnName() {
        return meanLengthColumnName;
    }

    public void setMeanLengthColumnName(String meanLengthColumnName) {
        this.meanLengthColumnName = meanLengthColumnName;
    }

    public DoubleParameter getTargetLength() {
        return targetLength;
    }

    public void setTargetLength(DoubleParameter targetLength) {
        this.targetLength = targetLength;
    }

    public DoubleParameter getBufferValue() {
        return bufferValue;
    }

    public void setBufferValue(DoubleParameter bufferValue) {
        this.bufferValue = bufferValue;
    }

    public DoubleParameter getHowManyYearsToLookBackTo() {
        return howManyYearsToLookBackTo;
    }

    public void setHowManyYearsToLookBackTo(DoubleParameter howManyYearsToLookBackTo) {
        this.howManyYearsToLookBackTo = howManyYearsToLookBackTo;
    }

    public String getEffortDefinition() {
        return effortDefinition;
    }

    public void setEffortDefinition(String effortDefinition) {
        this.effortDefinition = effortDefinition;
    }

    public boolean isBlockEntryWhenSeasonIsNotFull() {
        return blockEntryWhenSeasonIsNotFull;
    }

    public void setBlockEntryWhenSeasonIsNotFull(boolean blockEntryWhenSeasonIsNotFull) {
        this.blockEntryWhenSeasonIsNotFull = blockEntryWhenSeasonIsNotFull;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }
}
