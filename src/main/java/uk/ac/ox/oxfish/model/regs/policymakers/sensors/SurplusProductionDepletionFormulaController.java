package uk.ac.ox.oxfish.model.regs.policymakers.sensors;


import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.policymakers.SurplusProductionResult;
import uk.ac.ox.oxfish.model.regs.policymakers.SurplusProductionStockAssessment;
import uk.ac.ox.oxfish.model.regs.policymakers.TargetToTACController;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

//TAC=D*K*r/2
public class SurplusProductionDepletionFormulaController implements
        AlgorithmFactory<AdditionalStartable>
{

    private DoubleParameter carryingCapacityMinimum = new FixedDoubleParameter( 100000);
    private DoubleParameter carryingCapacityMaximum = new FixedDoubleParameter( 30000000);
    private DoubleParameter logisticGrowthMinimum =new FixedDoubleParameter(  0.1);
    private DoubleParameter logisticGrowthMaximum =new FixedDoubleParameter(  0.8);
    private DoubleParameter catchabilityMinimum = new FixedDoubleParameter( 0);
    private DoubleParameter catchabilityMaximum = new FixedDoubleParameter( 0.0001);

    private String indicatorColumnName =  "Species 0 CPHO";

    private  String catchColumnName = "Species 0 Landings";

    private DoubleParameter minimumTAC = new FixedDoubleParameter(10000);

    private int interval = 1;
    private int startingYear = 10;

    @Override
    public AdditionalStartable apply(FishState fishState) {

        SurplusProductionStockAssessment depletionSensor =
                new SurplusProductionStockAssessment(
                        new double[]{
                                carryingCapacityMinimum.apply(fishState.getRandom()),
                                carryingCapacityMaximum.apply(fishState.getRandom())
                        },
                        new double[]{
                                logisticGrowthMinimum.apply(fishState.getRandom()),
                                logisticGrowthMaximum.apply(fishState.getRandom())
                        },
                        new double[]{
                                catchabilityMinimum.apply(fishState.getRandom()),
                                catchabilityMaximum.apply(fishState.getRandom())
                        },
                        indicatorColumnName,
                        catchColumnName

                        );

        final TargetToTACController controller = new TargetToTACController(
                new Sensor<FishState, Double>() {
                    @Override
                    public Double scan(FishState system) {

                        final SurplusProductionResult assessment = depletionSensor.scan(system);
                        if (assessment == null) {
                            System.out.println("stock assessment has failed!");
                            return minimumTAC.apply(fishState.getRandom());
                        }
                        else {
                            double currentDepletion = assessment.getDepletion()[assessment.getDepletion().length - 1];

                            //TAC=D*K*r/2
                            //formula from here: https://dlmtool.github.io/DLMtool/reference/SPMSY.html
                            final double tac = currentDepletion * assessment.getCarryingCapacity() *
                                    assessment.getLogisticGrowth() / 2d;
                            System.out.println("tac: " + tac);
                            System.out.println("current_depletion: " + currentDepletion);
                            System.out.println("carrying_capacity: " + assessment.getCarryingCapacity());
                            System.out.println("logistic_growth: " + assessment.getLogisticGrowth());
                            if(tac<0)
                                return minimumTAC.apply(fishState.getRandom());

                            return tac;
                        }

                    }
                },
                interval * 365);


        return new AdditionalStartable() {
            @Override
            public void start(FishState model) {

                model.scheduleOnceInXDays(
                        new Steppable() {
                            @Override
                            public void step(SimState simState) {
                                controller.start(model);
                                controller.step(model);
                            }
                        },
                        StepOrder.DAWN,
                        365*startingYear+1
                );


            }
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
