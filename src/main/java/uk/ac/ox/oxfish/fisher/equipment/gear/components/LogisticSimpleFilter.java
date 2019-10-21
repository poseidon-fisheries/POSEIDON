package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import uk.ac.ox.oxfish.biology.Species;

/**
 * logistic selectivity with formula:
 * 1/(1+exp(selex_1-selex_2*L))
 * which hasn't the log10 correction from Stock assessments
 */
public class LogisticSimpleFilter extends FormulaAbundanceFilter {

    private final double selex1;

    private final double selex2;


    public LogisticSimpleFilter(boolean memoization, boolean rounding, double selex1, double selex2) {
        super(memoization, rounding);
        this.selex1 = selex1;
        this.selex2 = selex2;
    }

    @Override
    protected double[][] computeSelectivity(Species species) {
        double[][] toReturn = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        for(int subdivision = 0; subdivision<species.getNumberOfSubdivisions(); subdivision++)
            for(int bin=0; bin<species.getNumberOfBins(); bin++)
            {
                toReturn[subdivision][bin] = 1/(1+Math.exp(selex1-selex2*species.getLength(subdivision,bin)));


            }
        return toReturn;
    }

    public double getSelex1() {
        return selex1;
    }

    public double getSelex2() {
        return selex2;
    }
}
