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


import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.policymakers.SurplusProductionResult;
import uk.ac.ox.oxfish.model.regs.policymakers.SurplusProductionStockAssessment;
import uk.ac.ox.oxfish.model.regs.policymakers.TargetToTACController;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

//TAC=D*K*r/2
public class SurplusProductionDepletionFormulaController implements
    AlgorithmFactory<AdditionalStartable> {

    private DoubleParameter carryingCapacityMinimum = new FixedDoubleParameter(100000);
    private DoubleParameter carryingCapacityMaximum = new FixedDoubleParameter(30000000);
    private DoubleParameter logisticGrowthMinimum = new FixedDoubleParameter(0.1);
    private DoubleParameter logisticGrowthMaximum = new FixedDoubleParameter(0.8);
    private DoubleParameter catchabilityMinimum = new FixedDoubleParameter(0);
    private DoubleParameter catchabilityMaximum = new FixedDoubleParameter(0.0001);

    private String indicatorColumnName = "Species 0 CPHO";

    private String catchColumnName = "Species 0 Landings";

    private DoubleParameter minimumTAC = new FixedDoubleParameter(10000);

    private int interval = 1;
    private int startingYear = 10;

    private double lastAssessedCarryingCapacity = Double.NaN;
    private double lastAssessedCurrentDepletion = Double.NaN;
    private double lastAssessedLogisticGrowth = Double.NaN;

    @Override
    public AdditionalStartable apply(FishState fishState) {

        SurplusProductionStockAssessment depletionSensor =
            new SurplusProductionStockAssessment(
                new double[]{
                    carryingCapacityMinimum.applyAsDouble(fishState.getRandom()),
                    carryingCapacityMaximum.applyAsDouble(fishState.getRandom())
                },
                new double[]{
                    logisticGrowthMinimum.applyAsDouble(fishState.getRandom()),
                    logisticGrowthMaximum.applyAsDouble(fishState.getRandom())
                },
                new double[]{
                    catchabilityMinimum.applyAsDouble(fishState.getRandom()),
                    catchabilityMaximum.applyAsDouble(fishState.getRandom())
                },
                indicatorColumnName,
                catchColumnName

            );

        final TargetToTACController controller = new TargetToTACController(
            (Sensor<FishState, Double>) system -> {

                final SurplusProductionResult assessment = depletionSensor.scan(system);
                if (assessment == null) {
                    System.out.println("stock assessment has failed!");
                    return minimumTAC.applyAsDouble(fishState.getRandom());
                } else {
                    double currentDepletion = assessment.getDepletion()[assessment.getDepletion().length - 1];

                    //TAC=D*K*r/2
                    //formula from here: https://dlmtool.github.io/DLMtool/reference/SPMSY.html
                    final double tac = currentDepletion * assessment.getCarryingCapacity() *
                        assessment.getLogisticGrowth() / 2d;
                    System.out.println("tac: " + tac);
                    System.out.println("current_depletion: " + currentDepletion);
                    System.out.println("carrying_capacity: " + assessment.getCarryingCapacity());
                    System.out.println("logistic_growth: " + assessment.getLogisticGrowth());

                    lastAssessedCarryingCapacity = assessment.getCarryingCapacity();
                    lastAssessedCurrentDepletion = currentDepletion;
                    lastAssessedLogisticGrowth = assessment.getLogisticGrowth();

                    if (tac < 0)
                        return minimumTAC.applyAsDouble(fishState.getRandom());

                    return tac;
                }

            },
            interval * 365
        );


        return model -> {

            model.getYearlyDataSet().registerGatherer("Last Assessed Carrying Capacity",
                (Gatherer<FishState>) fishState1 -> lastAssessedCarryingCapacity, Double.NaN
            );
            model.getYearlyDataSet().registerGatherer("Last Assessed Depletion",
                (Gatherer<FishState>) fishState1 -> lastAssessedCurrentDepletion, Double.NaN
            );

            model.getYearlyDataSet().registerGatherer("Last Assessed Logistic Growth",
                (Gatherer<FishState>) fishState1 -> lastAssessedLogisticGrowth, Double.NaN
            );


            model.scheduleOnceInXDays(
                (Steppable) simState -> {
                    controller.start(model);
                    controller.step(model);
                },
                StepOrder.DAWN,
                365 * startingYear + 1
            );


        };


    }

    public DoubleParameter getCarryingCapacityMinimum() {
        return carryingCapacityMinimum;
    }

    public void setCarryingCapacityMinimum(DoubleParameter carryingCapacityMinimum) {
        this.carryingCapacityMinimum = carryingCapacityMinimum;
    }

    public DoubleParameter getCarryingCapacityMaximum() {
        return carryingCapacityMaximum;
    }

    public void setCarryingCapacityMaximum(DoubleParameter carryingCapacityMaximum) {
        this.carryingCapacityMaximum = carryingCapacityMaximum;
    }

    public DoubleParameter getLogisticGrowthMinimum() {
        return logisticGrowthMinimum;
    }

    public void setLogisticGrowthMinimum(DoubleParameter logisticGrowthMinimum) {
        this.logisticGrowthMinimum = logisticGrowthMinimum;
    }

    public DoubleParameter getLogisticGrowthMaximum() {
        return logisticGrowthMaximum;
    }

    public void setLogisticGrowthMaximum(DoubleParameter logisticGrowthMaximum) {
        this.logisticGrowthMaximum = logisticGrowthMaximum;
    }

    public DoubleParameter getCatchabilityMinimum() {
        return catchabilityMinimum;
    }

    public void setCatchabilityMinimum(DoubleParameter catchabilityMinimum) {
        this.catchabilityMinimum = catchabilityMinimum;
    }

    public DoubleParameter getCatchabilityMaximum() {
        return catchabilityMaximum;
    }

    public void setCatchabilityMaximum(DoubleParameter catchabilityMaximum) {
        this.catchabilityMaximum = catchabilityMaximum;
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

    public DoubleParameter getMinimumTAC() {
        return minimumTAC;
    }

    public void setMinimumTAC(DoubleParameter minimumTAC) {
        this.minimumTAC = minimumTAC;
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
