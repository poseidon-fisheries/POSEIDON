package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Objects;

/**
 * The retention filter formula that appears the most in the spreadhseets
 * (including thornyheads and sablefish)
 * Created by carrknight on 3/9/16.
 */
public class RetentionAbundanceFilter extends FormulaAbundanceFilter {

    private final double inflection;

    private final double slope;

    private final double asymptote;



    public RetentionAbundanceFilter(boolean memoization, double inflection, double slope, double asymptote) {
        super(memoization);
        this.inflection = inflection;
        this.slope = slope;
        this.asymptote = asymptote;
    }

    @Override
    protected double[][] computeSelectivity(Species species)
    {
        double[][] toReturn = new double[2][species.getMaxAge()+1];
        ImmutableList<Double> maleLength = species.getLengthMaleInCm();
        ImmutableList<Double> femaleLength = species.getLengthFemaleInCm();

        for(int age=0; age<species.getMaxAge()+1; age++)
        {
            toReturn[FishStateUtilities.MALE][age] =
                    asymptote/(1+Math.exp(-( maleLength.get(age)-inflection)/slope));

            toReturn[FishStateUtilities.FEMALE][age] =
                    asymptote/(1+Math.exp(-( femaleLength.get(age)-inflection)/slope));

        }
        return toReturn;

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetentionAbundanceFilter that = (RetentionAbundanceFilter) o;
        return Double.compare(that.inflection, inflection) == 0 &&
                Double.compare(that.slope, slope) == 0 &&
                Double.compare(that.asymptote, asymptote) == 0 &&
                isMemoization() == that.isMemoization();
    }

    @Override
    public int hashCode() {
        return Objects.hash(inflection, slope, asymptote);
    }
}
