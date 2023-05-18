package uk.ac.ox.oxfish.maximization;

import eva2.problems.simple.SimpleProblemDouble;

public class SamplePoseidonOptimization extends SimpleProblemDouble {


    private double multiplier = 2;


    @Override
    public double[] evaluate(double[] x) {

        return new double[]{Math.pow(x[0] - multiplier, 2)};


    }

    @Override
    public int getProblemDimension() {
        return 1;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
}
