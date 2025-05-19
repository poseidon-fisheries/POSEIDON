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

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgent;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.ITarget;

/**
 * changes M/K in SPR computation to avoid large discrepancies between what the SPR
 * rule says and what ITarget would say.
 */
public class LBSPRPolicyUpdater implements Steppable, AdditionalStartable {


    private static final long serialVersionUID = 5201719448928049885L;
    /**
     * keep the SPR internal so we can change its parameters on the spot
     */
    private final SPRAgent internalSPRAgent;

    /**
     * the object actually making decisions  and policies
     */
    private final LBSPREffortPolicy controller;


    /**
     * the object used to transform CPUE into effort (used to adjust M/K, not directly into policy)
     */
    private final ITarget cpueToEffort;


    private final double upperDiscrepancyThreshold;

    private final double lowerDiscrepancyThreshold;

    private final double minimumMK;

    private final double maximumMK;

    /**
     * do not update M/K before this year
     */
    private final int startUpdatingAfterYear;

    public LBSPRPolicyUpdater(
        final SPRAgent internalSPRAgent, final LBSPREffortPolicy controller,
        final double upperDiscrepancyThreshold, final double lowerDiscrepancyThreshold,
        final int cpueHalfPeriod, final double minimumMK, final double maximumMK, final int startUpdatingAfterYear
    ) {
        this.internalSPRAgent = internalSPRAgent;
        this.controller = controller;
        this.upperDiscrepancyThreshold = upperDiscrepancyThreshold;
        this.lowerDiscrepancyThreshold = lowerDiscrepancyThreshold;
        this.minimumMK = minimumMK;
        this.maximumMK = maximumMK;
        this.startUpdatingAfterYear = startUpdatingAfterYear;

        cpueToEffort = new ITarget(
            "not used",
            "CPUE " + internalSPRAgent.getSpecies() + " " + internalSPRAgent.getSurveyTag(),
            1.0,
            1.5,
            cpueHalfPeriod,
            -1 //not used!
        );
    }

    /**
     * here we update the SPR M/K
     *
     * @param simState
     */
    @Override
    public void step(final SimState simState) {

        final FishState model = (FishState) simState;

        //first apply the controller as usual
        controller.step(simState);


        //if there are not enough observations, don't bother updating
        if (model.getYear() < startUpdatingAfterYear ||
            model.getYearlyDataSet().getColumn(cpueToEffort.getIndicatorColumnName()).size() <
                cpueToEffort.getTimeInterval() * 2

        )
            return;

        final double targetSPR = 0.4;
        final double currentSPR = model.getLatestYearlyObservation("SPR " + internalSPRAgent.getSpecies() + " " + internalSPRAgent.getSurveyTag());
        final double effortChangeSPR = LBSPREffortPolicy.lbsprPolicyEffortProportion(
            controller.getLinearParameter(),
            controller.getCubicParameter(),
            currentSPR,
            targetSPR
        );
        double effortChangeCPUE = cpueToEffort.getPercentageChangeToTACDueToIndicator(model);
        effortChangeCPUE = effortChangeCPUE - 1;

        final double discrepancy = effortChangeCPUE - effortChangeSPR;
        if (discrepancy > upperDiscrepancyThreshold) {
            //increase M/K
            final double currentMK = internalSPRAgent.getAssumedNaturalMortality() / internalSPRAgent.getAssumedKParameter();
            final double newMortality = internalSPRAgent.getAssumedKParameter() * Math.min(currentMK + .1, maximumMK);
            internalSPRAgent.setAssumedNaturalMortality(newMortality);
        }
        if (discrepancy < lowerDiscrepancyThreshold) {
            //decrease M/K
            final double currentMK = internalSPRAgent.getAssumedNaturalMortality() / internalSPRAgent.getAssumedKParameter();
            final double newMortality = internalSPRAgent.getAssumedKParameter() * Math.max(currentMK - .1, minimumMK);
            internalSPRAgent.setAssumedNaturalMortality(newMortality);
        }
        System.out.println("M/K is now " + (internalSPRAgent.getAssumedNaturalMortality() / internalSPRAgent.getAssumedKParameter()));


    }


    public double getAccumulatedDelta() {
        return controller.getAccumulatedDelta();
    }

    @Override
    public void start(final FishState model) {
        //we are going to intercept the start for the controller, because we want to step it always
        //before we update
        model.scheduleEveryXDay(this, StepOrder.POLICY_UPDATE, controller.getIntervalInDays());


        model.getYearlyDataSet().registerGatherer(
            "M/K ratio " + internalSPRAgent.getSpecies() + " " +
                internalSPRAgent.getSurveyTag(),
            (Gatherer<FishState>) fishState -> (internalSPRAgent.getAssumedNaturalMortality() / internalSPRAgent.getAssumedKParameter()),
            Double.NaN
        );

    }


}
