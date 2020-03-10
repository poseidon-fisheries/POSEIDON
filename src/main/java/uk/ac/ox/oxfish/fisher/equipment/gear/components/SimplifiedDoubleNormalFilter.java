package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import uk.ac.ox.oxfish.biology.Species;

/**
 * double normal selectivity as in https://rdrr.io/github/AdrianHordyk/LeesApprox/src/R/LeesApprox.r
 */
public class SimplifiedDoubleNormalFilter extends FormulaAbundanceFilter {

    private final double lengthFullSelectivity;

    private final double slopeLeft;

    private final double slopeRight;


    public SimplifiedDoubleNormalFilter(boolean memoization, boolean rounding, double lengthFullSelectivity, double slopeLeft, double slopeRight) {
        super(memoization, rounding);

        this.lengthFullSelectivity = lengthFullSelectivity;
        this.slopeLeft = slopeLeft;
        this.slopeRight = slopeRight;
    }

    @Override
    protected double[][] computeSelectivity(Species species) {

        //original:
        //function(lens,lfs,sl,sr){
        //    cond<-lens<=lfs
        //    sel<-rep(NA,length(lens))
        //    sel[cond]<-2.0^-((lens[cond]-lfs)/sl*(lens[cond]-lfs)/sl)
        //    sel[!cond]<-2.0^-((lens[!cond]-lfs)/sr*(lens[!cond]-lfs)/sr)
        //    sel
        //}
        double[][] selex = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
        for(int subdivision =0; subdivision< species.getNumberOfSubdivisions(); subdivision++)
            for(int bin=0;bin<species.getNumberOfBins();bin++)
            {

                final double length = species.getLength(subdivision, bin);
                boolean isLeft = length <= lengthFullSelectivity;
                if(isLeft)
                    selex[subdivision][bin] = Math.pow(2,
                            (-(length-lengthFullSelectivity)/slopeLeft*(length-lengthFullSelectivity)/slopeLeft));
                else{
                    selex[subdivision][bin] = Math.pow(2,
                            (-(length-lengthFullSelectivity)/slopeRight*(length-lengthFullSelectivity)/slopeRight));

                }

            }
        return selex;

    }
}
