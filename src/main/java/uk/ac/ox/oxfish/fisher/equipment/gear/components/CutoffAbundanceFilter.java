package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * filters any fish above/below a given cutoff length
 * Created by carrknight on 3/11/16.
 */
public class CutoffAbundanceFilter extends FormulaAbundanceFilter {


    private final double cutoffLevel;

    private final boolean selectHigherThanCutoff;


    public CutoffAbundanceFilter(double cutoffLevel, boolean selectHigherThanCutoff) {
        super(false);
        this.cutoffLevel = cutoffLevel;
        this.selectHigherThanCutoff = selectHigherThanCutoff;
    }

    /** {@inheritDoc} */
    @Override
    protected double[][] computeSelectivity(Species species) {
        double[][] toReturn = new double[2][species.getMaxAge()+1];
        ImmutableList<Double> maleLength = species.getLengthMaleInCm();
        ImmutableList<Double> femaleLength = species.getLengthFemaleInCm();

        double higherThanCutoff = selectHigherThanCutoff ? 1 : 0;

        for(int age=0; age<species.getMaxAge()+1; age++)
        {
            toReturn[FishStateUtilities.MALE][age] = maleLength.get(age)>=cutoffLevel ? higherThanCutoff : 1-higherThanCutoff;


            toReturn[FishStateUtilities.FEMALE][age] =
                    femaleLength.get(age)>=cutoffLevel ? higherThanCutoff : 1-higherThanCutoff;

        }
        return toReturn;
    }
}
