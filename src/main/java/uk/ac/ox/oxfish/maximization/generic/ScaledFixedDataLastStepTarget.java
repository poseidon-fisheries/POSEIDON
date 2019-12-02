package uk.ac.ox.oxfish.maximization.generic;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;

public class ScaledFixedDataLastStepTarget implements DataTarget {

    private double fixedTarget;
    private String columnName = "";

    private double exponent = 1;

    public static boolean VERBOSE = false;

    public ScaledFixedDataLastStepTarget() { }

    /**
     * computes distance from target (0 best, the higher the number the further away from optimum we are)
     *
     * @param model model after it has been run
     * @return distance from target (0 best, the higher the number the further away from optimum we are)
     */
    @Override
    public double computeError(FishState model) {
        DataColumn simulationOutput = model.getYearlyDataSet().getColumn(columnName);
        if (VERBOSE) {
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            System.out.println("column: " + columnName);
            System.out.println("output: " + simulationOutput.getLatest());
            System.out.println("target : " + fixedTarget);
            System.out.println("error : " + abs((simulationOutput.getLatest() - fixedTarget) / fixedTarget));
        }
        return abs(
            Math.pow((simulationOutput.getLatest() - fixedTarget), exponent)
                / fixedTarget
        );
    }

    public double getFixedTarget() { return fixedTarget; }

    public void setFixedTarget(double fixedTarget) {
        checkArgument(fixedTarget != 0);
        this.fixedTarget = fixedTarget;
    }

    public String getColumnName() { return columnName; }

    public void setColumnName(String columnName) { this.columnName = columnName; }

    /**
     * Getter for property 'exponent'.
     *
     * @return Value for property 'exponent'.
     */
    public double getExponent() {
        return exponent;
    }

    /**
     * Setter for property 'exponent'.
     *
     * @param exponent Value to set for property 'exponent'.
     */
    public void setExponent(double exponent) {
        this.exponent = exponent;
    }
}
