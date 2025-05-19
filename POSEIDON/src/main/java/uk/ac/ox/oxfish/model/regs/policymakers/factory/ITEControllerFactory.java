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
import uk.ac.ox.oxfish.model.regs.policymakers.CloseReopenOnEffortDecorator;
import uk.ac.ox.oxfish.model.regs.policymakers.IndexTargetController;
import uk.ac.ox.oxfish.model.regs.policymakers.LBSPREffortPolicyFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.PastAverageSensor;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.UnchangingPastSensor;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * The ITE5/ITE10 kind of controller from the DLMtoolkit.
 * Fundamentally increases/decreases effort as a function of the ratio between current indicator and target indicator
 * where target indicator is the initial 5 year average indicator times a multiplier
 */
public class ITEControllerFactory implements AlgorithmFactory<AdditionalStartable> {


    private String indicatorColumnName = "Average Trip Income";

    private DoubleParameter multiplier = new FixedDoubleParameter(1.0);

    //0.05 is ITE5; 0.1 is ITE10
    private DoubleParameter maxChangePerYear = new FixedDoubleParameter(.1);

    /**
     * what kind of effort are we actually modifying!
     * Keys are in the EFFORT_ACTUATOR map
     */
    private String effortDefinition = "season";

    /**
     * when effort is below 1 we will also close down the fishery to new entrants?
     */
    private boolean blockEntryWhenSeasonIsNotFull = true;

    /**
     * how many years after the start is called we should activate the indicator?
     */
    private int yearsBeforeStarting = 10;

    private DoubleParameter yearsToLookBackForTarget = new FixedDoubleParameter(5);

    @Override
    public AdditionalStartable apply(final FishState fishState) {
        Preconditions.checkArgument(
            LBSPREffortPolicyFactory.EFFORT_ACTUATORS.containsKey(effortDefinition),
            "The valid effort actuators are " + LBSPREffortPolicyFactory.EFFORT_ACTUATORS.keySet()
        );

        final Actuator<FishState, Double> effortActuator = LBSPREffortPolicyFactory.EFFORT_ACTUATORS.get(
            effortDefinition);


        return model -> {
            if (yearsBeforeStarting <= 0)
                starterMethod(model, effortActuator).step(model);
            else
                fishState.scheduleOnceInXDays(
                    starterMethod(model, effortActuator),
                    StepOrder.DAWN,
                    365 * yearsBeforeStarting + 1

                );
        };
    }

    private Steppable starterMethod(final FishState model, final Actuator<FishState, Double> effortActuator) {
        return simState -> {
            final IndexTargetController controller =
                new IndexTargetController(
                    new PastAverageSensor(
                        indicatorColumnName,
                        1
                    ),
                    new UnchangingPastSensor(
                        indicatorColumnName,
                        multiplier.applyAsDouble(model.getRandom()),
                        (int) yearsToLookBackForTarget.applyAsDouble(model.getRandom())
                    ),
                    blockEntryWhenSeasonIsNotFull ? new CloseReopenOnEffortDecorator(effortActuator) :
                        effortActuator, 365,
                    maxChangePerYear.applyAsDouble(model.getRandom()),
                    false,


                    false
                );

            controller.start(model);
            controller.step(model);
            model.getYearlyDataSet().registerGatherer("Index Ratio",
                (Gatherer<FishState>) fishState -> controller.getLastPolicy()
                , Double.NaN
            );

        };
    }


    public String getIndicatorColumnName() {
        return indicatorColumnName;
    }

    public void setIndicatorColumnName(final String indicatorColumnName) {
        this.indicatorColumnName = indicatorColumnName;
    }

    public DoubleParameter getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(final DoubleParameter multiplier) {
        this.multiplier = multiplier;
    }

    public DoubleParameter getMaxChangePerYear() {
        return maxChangePerYear;
    }

    public void setMaxChangePerYear(final DoubleParameter maxChangePerYear) {
        this.maxChangePerYear = maxChangePerYear;
    }

    public String getEffortDefinition() {
        return effortDefinition;
    }

    public void setEffortDefinition(final String effortDefinition) {
        this.effortDefinition = effortDefinition;
    }

    public boolean isBlockEntryWhenSeasonIsNotFull() {
        return blockEntryWhenSeasonIsNotFull;
    }

    public void setBlockEntryWhenSeasonIsNotFull(final boolean blockEntryWhenSeasonIsNotFull) {
        this.blockEntryWhenSeasonIsNotFull = blockEntryWhenSeasonIsNotFull;
    }

    public int getYearsBeforeStarting() {
        return yearsBeforeStarting;
    }

    public void setYearsBeforeStarting(final int yearsBeforeStarting) {
        this.yearsBeforeStarting = yearsBeforeStarting;
    }

    public DoubleParameter getYearsToLookBackForTarget() {
        return yearsToLookBackForTarget;
    }

    public void setYearsToLookBackForTarget(final DoubleParameter yearsToLookBackForTarget) {
        this.yearsToLookBackForTarget = yearsToLookBackForTarget;
    }
}
