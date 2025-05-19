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

import java.util.HashMap;
import java.util.Map;

public class LBSPREffortPolicyFactory implements AlgorithmFactory<AdditionalStartable> {


    public final static Map<String, Actuator<FishState, Double>> EFFORT_ACTUATORS = new HashMap<>();

    static {
        EFFORT_ACTUATORS.put(
            "season",
            IndexTargetController.RATIO_TO_SEASONAL_CLOSURE
        );
        EFFORT_ACTUATORS.put(
            "season_hours_out",
            IndexTargetController.RATIO_TO_PERSONAL_SEASONAL_CLOSURE
        );
        EFFORT_ACTUATORS.put(
            "fleet",
            IndexTargetController.RATIO_TO_FLEET_SIZE
        );
        EFFORT_ACTUATORS.put(
            "daysatsea",
            IndexTargetController.RATIO_TO_DAYSATSEA
        );
    }


    private String sprColumnName = "SPR " + "Lutjanus malabaricus" + " " + "spr_agent_total";

    private DoubleParameter linearParameter = new FixedDoubleParameter(0.05);

    private DoubleParameter cubicParameter = new FixedDoubleParameter(0.3);

    private DoubleParameter sprTarget = new FixedDoubleParameter(.4);

    private DoubleParameter maxChangeEachYear = new FixedDoubleParameter(.1);

    private String effortDefinition = "season";


    private boolean blockEntryWhenSeasonIsNotFull = false;

    private int startingYear = 7;

    @Override
    public AdditionalStartable apply(FishState fishState) {


        Preconditions.checkArgument(
            EFFORT_ACTUATORS.containsKey(effortDefinition),
            "The valid effort actuators are " + EFFORT_ACTUATORS.keySet()
        );


        return model -> fishState.scheduleOnceInXDays(
            (Steppable) simState -> {
                LBSPREffortPolicy lbspr = new LBSPREffortPolicy(
                    sprColumnName,
                    linearParameter.applyAsDouble(fishState.getRandom()),
                    cubicParameter.applyAsDouble(fishState.getRandom()),
                    sprTarget.applyAsDouble(fishState.getRandom()),
                    maxChangeEachYear.applyAsDouble(fishState.getRandom()),
                    EFFORT_ACTUATORS.get(effortDefinition),
                    blockEntryWhenSeasonIsNotFull
                );
                lbspr.start(model);
                lbspr.step(model);


                //creaqte also a collector
                fishState.getYearlyDataSet().registerGatherer(
                    "LBSPREffortPolicy output",
                    (Gatherer<FishState>) fishState1 -> lbspr.getAccumulatedDelta(),
                    Double.NaN
                );
            },
            StepOrder.DAWN,
            365 * startingYear + 1
        );
    }

    public String getSprColumnName() {
        return sprColumnName;
    }

    public void setSprColumnName(String sprColumnName) {
        this.sprColumnName = sprColumnName;
    }

    public DoubleParameter getLinearParameter() {
        return linearParameter;
    }

    public void setLinearParameter(DoubleParameter linearParameter) {
        this.linearParameter = linearParameter;
    }

    public DoubleParameter getCubicParameter() {
        return cubicParameter;
    }

    public void setCubicParameter(DoubleParameter cubicParameter) {
        this.cubicParameter = cubicParameter;
    }

    public DoubleParameter getSprTarget() {
        return sprTarget;
    }

    public void setSprTarget(DoubleParameter sprTarget) {
        this.sprTarget = sprTarget;
    }

    public DoubleParameter getMaxChangeEachYear() {
        return maxChangeEachYear;
    }

    public void setMaxChangeEachYear(DoubleParameter maxChangeEachYear) {
        this.maxChangeEachYear = maxChangeEachYear;
    }

    public String getEffortDefinition() {
        return effortDefinition;
    }

    public void setEffortDefinition(String effortDefinition) {
        this.effortDefinition = effortDefinition;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }

    public boolean isBlockEntryWhenSeasonIsNotFull() {
        return blockEntryWhenSeasonIsNotFull;
    }

    public void setBlockEntryWhenSeasonIsNotFull(boolean blockEntryWhenSeasonIsNotFull) {
        this.blockEntryWhenSeasonIsNotFull = blockEntryWhenSeasonIsNotFull;
    }
}
