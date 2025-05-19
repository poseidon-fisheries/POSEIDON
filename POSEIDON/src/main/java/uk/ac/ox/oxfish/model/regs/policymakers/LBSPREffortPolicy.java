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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EntryPlugin;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

/**
 * multiplies previous effort by a function of the proportion of current SPR to target
 */
public class LBSPREffortPolicy extends Controller {

    private static final long serialVersionUID = -7938732071691208894L;
    private final String columnNameSPR;

    private final double linearParameter;

    private final double cubicParameter;

    private final double sprTarget; //usually this is 30/40%

    private final double maxChangeInPercentage;
    private final boolean blockEntryWhenSeasonIsNotFull;
    /**
     * I am assuming the actuator itself just wants the final "effort" multiplier;
     * here we store 1 * delta(t=1) * delta(t=2) * ... * delta(now)
     */
    private double accumulatedDelta = 1;


    public LBSPREffortPolicy(
        final String columnNameSPR, final double linearParameter,
        final double cubicParameter, final double sprTarget,
        final double maxChangeInPercentage,
        final Actuator<FishState, Double> effortActuator, final boolean blockEntryWhenSeasonIsNotFull
    ) {

        super(
            (Sensor<FishState, Double>) system ->
                system.getLatestYearlyObservation(columnNameSPR),
            (Sensor<FishState, Double>) system ->
                sprTarget,
            effortActuator,
            365
        );
        this.columnNameSPR = columnNameSPR;
        this.linearParameter = linearParameter;
        this.cubicParameter = cubicParameter;
        this.sprTarget = sprTarget;
        this.maxChangeInPercentage = maxChangeInPercentage;
        this.blockEntryWhenSeasonIsNotFull = blockEntryWhenSeasonIsNotFull;
    }

    /**
     * This is the formula in the DLM toolkit in 2019; it was a simpler bang-bang controller
     */
    public static double lbsprPolicyEffortBangBang(
        final double currentSPR,
        final double targetSPR
    ) {

        final double ratio = currentSPR / targetSPR;
        if (ratio > 1.25)
            return 1.1;
        if (ratio < 0.75)
            return 0.9;
        else
            return 1.0;
    }

    @Override
    public double computePolicy(
        final double currentSPR,
        final double targetSPR,
        final FishState model,
        final double oldPolicy
    ) {
        //numerical errors can happen; don't act then
        if (!Double.isFinite(currentSPR) || currentSPR < 0 || currentSPR > 1)
            return accumulatedDelta;

        double deltaToday =
            lbsprPolicyEffortProportion(linearParameter, cubicParameter, currentSPR, targetSPR);
        if (deltaToday < -maxChangeInPercentage)
            deltaToday = -maxChangeInPercentage;
        if (deltaToday > maxChangeInPercentage)
            deltaToday = maxChangeInPercentage;


        System.out.println("effort is now " + accumulatedDelta);
        accumulatedDelta = accumulatedDelta * (1 + deltaToday);

        if (accumulatedDelta >= 1 && blockEntryWhenSeasonIsNotFull)
            for (final EntryPlugin entryPlugin : model.getEntryPlugins()) {
                entryPlugin.setEntryPaused(false);
            }
        if (accumulatedDelta < 1 && blockEntryWhenSeasonIsNotFull)
            for (final EntryPlugin entryPlugin : model.getEntryPlugins()) {
                entryPlugin.setEntryPaused(true);
            }


        return Math.min(accumulatedDelta, 1);
    }

    /**
     * this is the formula in the DLM toolkit as of Nov 10, 2020
     */
    public static double lbsprPolicyEffortProportion(
        final double linearParameter,
        final double cubicParameter,
        final double currentSPR,
        final double targetSPR
    ) {
        //this is in the DLM toolkit:
        //    vt <- theta1 * (ratio^3) + theta2 * ratio

        //IT IS DIFFERENT FROM THE PAPER
        return cubicParameter *
            Math.pow(currentSPR / targetSPR - 1d, 3) +
            linearParameter *
                (currentSPR / (targetSPR) - 1d);

    }

    public double getAccumulatedDelta() {
        return accumulatedDelta;
    }

    public String getColumnNameSPR() {
        return columnNameSPR;
    }

    public double getLinearParameter() {
        return linearParameter;
    }

    public double getCubicParameter() {
        return cubicParameter;
    }

    public double getSprTarget() {
        return sprTarget;
    }
}
