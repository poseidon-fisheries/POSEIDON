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

package uk.ac.ox.oxfish.model.regs.policymakers.factory;

import com.google.common.base.Preconditions;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.policymakers.LBSPREffortPolicyFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.LTargetEffortPolicy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Map;

public class LTargetEffortPolicyFactory implements AlgorithmFactory<AdditionalStartable> {


    private String meanLengthColumnName = "Mean Length Caught " + "Lutjanus malabaricus" + " " + "spr_agent_total";

    private DoubleParameter proportionAverageToTarget = new FixedDoubleParameter(60);


    private DoubleParameter yearsBackToAverage = new FixedDoubleParameter(5);


    private DoubleParameter updateEffortPeriodInYears = new FixedDoubleParameter(5);


    private String effortDefinition = "season";


    private boolean blockEntryWhenSeasonIsNotFull = true;

    private int startingYear = 11;


    @Override
    public AdditionalStartable apply(FishState fishState) {

        final Map<String, Actuator<FishState, Double>> effortActuators = LBSPREffortPolicyFactory.EFFORT_ACTUATORS;
        Preconditions.checkArgument(
            effortActuators.containsKey(effortDefinition),
            "The valid effort actuators are " + effortActuators.keySet()
        );


        return model -> fishState.scheduleOnceInXDays(
            (Steppable) simState -> {
                LTargetEffortPolicy ltargetE = new LTargetEffortPolicy(
                    meanLengthColumnName,
                    proportionAverageToTarget.applyAsDouble(model.getRandom()),
                    (int) yearsBackToAverage.applyAsDouble(fishState.getRandom()),
                    effortActuators.get(effortDefinition),
                    blockEntryWhenSeasonIsNotFull,
                    (int) updateEffortPeriodInYears.applyAsDouble(fishState.getRandom())
                );
                ltargetE.start(model);
                ltargetE.step(model);


                //creaqte also a collector
                fishState.getYearlyDataSet().registerGatherer(
                    "LTargetE output",
                    (Gatherer<FishState>) fishState1 -> ltargetE.getSuggestedEffort(),
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

    public DoubleParameter getProportionAverageToTarget() {
        return proportionAverageToTarget;
    }

    public void setProportionAverageToTarget(DoubleParameter proportionAverageToTarget) {
        this.proportionAverageToTarget = proportionAverageToTarget;
    }

    public DoubleParameter getYearsBackToAverage() {
        return yearsBackToAverage;
    }

    public void setYearsBackToAverage(DoubleParameter yearsBackToAverage) {
        this.yearsBackToAverage = yearsBackToAverage;
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

    public DoubleParameter getUpdateEffortPeriodInYears() {
        return updateEffortPeriodInYears;
    }

    public void setUpdateEffortPeriodInYears(DoubleParameter updateEffortPeriodInYears) {
        this.updateEffortPeriodInYears = updateEffortPeriodInYears;
    }
}
