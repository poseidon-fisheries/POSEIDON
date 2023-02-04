package uk.ac.ox.oxfish.biology.initializer.factory;

import com.beust.jcommander.internal.Lists;
import uk.ac.ox.oxfish.biology.boxcars.EquallySpacedBertalanffyFactory;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * Boxcar factory where boxes are not necessarilly all of the same dimension.
 */
public class SingleSpeciesIrregularBoxcarFactory extends SingleSpeciesBoxcarAbstractFactory {

    /**
     * these would represent the "mid-point" of lengths that we are going to model. Fish would transition from
     * one to the other
     */
    private List<Double> binnedLengthsInCm = Lists.newArrayList(10d,30d,60d);



    @Override
    protected GrowthBinByList generateBins(FishState state) {
        double[] lengths = new double[binnedLengthsInCm.size()];
        double[] weights = new double[binnedLengthsInCm.size()];

        final Double alpha = getAllometricAlpha().apply(state.getRandom());
        final Double beta = getAllometricBeta().apply(state.getRandom());

        for (int bin = 0; bin < lengths.length; bin++) {
            lengths[bin] = binnedLengthsInCm.get(bin);

            weights[bin] = EquallySpacedBertalanffyFactory.bertnalanffyLengthToWeight(
                    alpha,
                    beta,
                    lengths[bin]
            );
        }
        return new GrowthBinByList(
                1,
                lengths,
                weights,
                EquallySpacedBertalanffyFactory.bertalanffyLengthAtAge(
                        getLInfinity().apply(state.getRandom()),
                        0,
                        getK().apply(state.getRandom()),
                        EquallySpacedBertalanffyFactory.MAXIMUM_AGE_TRACKED
                )


        );
    }


    public List<Double> getBinnedLengthsInCm() {
        return binnedLengthsInCm;
    }

    public void setBinnedLengthsInCm(List<Double> binnedLengthsInCm) {
        this.binnedLengthsInCm = binnedLengthsInCm;
    }
}
