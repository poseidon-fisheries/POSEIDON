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

package uk.ac.ox.oxfish.biology.complicated.factory;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.jfree.util.Log;
import uk.ac.ox.oxfish.biology.complicated.NoiseMaker;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * classic BH recruitment function with lognormal noise (a la DLMtoolkit, i.e. centered at 0); optionally we may want noise to only
 * start after a certain period (for example, after the calibration is over) which requires the noisemaker to keep a link
 * with the fishstate to know what year we are in....
 */
public class RecruitmentBySpawningJackKnifeMaturityWithProcessError implements AlgorithmFactory<RecruitmentBySpawningBiomass> {

    private RecruitmentBySpawningJackKnifeMaturity delegate =
        new RecruitmentBySpawningJackKnifeMaturity();
    private DoubleParameter lognormalStandardDeviation = new FixedDoubleParameter(0.4);
    private DoubleParameter firstYearRecruitmentBecomesNoisy = new FixedDoubleParameter(0);

    @Override
    public RecruitmentBySpawningBiomass apply(final FishState fishState) {
        final RecruitmentBySpawningBiomass original = delegate.apply(fishState);
        final Double standardDeviation = lognormalStandardDeviation.applyAsDouble(fishState.getRandom());
        if (standardDeviation > 0)
            original.addNoise(new YearAwareLogNormalNoiseMaker(
                (int) firstYearRecruitmentBecomesNoisy.applyAsDouble(fishState.getRandom()),
                standardDeviation,
                fishState
            ));
        else
            Log.warn("Negative/0 standard deviation in recruitment means no noise in recruitments...");
        return original;
    }

    public RecruitmentBySpawningJackKnifeMaturity getDelegate() {
        return delegate;
    }

    public void setDelegate(final RecruitmentBySpawningJackKnifeMaturity delegate) {
        this.delegate = delegate;
    }


    public DoubleParameter getLognormalStandardDeviation() {
        return lognormalStandardDeviation;
    }

    public void setLognormalStandardDeviation(final DoubleParameter lognormalStandardDeviation) {
        this.lognormalStandardDeviation = lognormalStandardDeviation;
    }

    public DoubleParameter getFirstYearRecruitmentBecomesNoisy() {
        return firstYearRecruitmentBecomesNoisy;
    }

    public void setFirstYearRecruitmentBecomesNoisy(final DoubleParameter firstYearRecruitmentBecomesNoisy) {
        this.firstYearRecruitmentBecomesNoisy = firstYearRecruitmentBecomesNoisy;
    }


    /**
     * what I am doing here is reproducing the BH recruitment from the DLMtoolkit which has a lognormal that is recentered
     * by substituting away -sd^2/2;
     */
    public static class YearAwareLogNormalNoiseMaker implements NoiseMaker {

        private static final long serialVersionUID = 7421074856003089457L;
        private final int yearToStart;


        private final FishState state;

        private final LogNormalDistribution logNormalDistribution;

        private final double adjustment;

        public YearAwareLogNormalNoiseMaker(
            final int yearToStart,
            final double lognormalStandardDeviation,
            final FishState state
        ) {
            Preconditions.checkArgument(lognormalStandardDeviation > 0);
            Preconditions.checkArgument(Double.isFinite(yearToStart));
            Preconditions.checkArgument(Double.isFinite(lognormalStandardDeviation));
            this.yearToStart = yearToStart;
            this.state = state;
            this.logNormalDistribution = new LogNormalDistribution(0d, lognormalStandardDeviation);
            this.adjustment = 0.5 * Math.pow(logNormalDistribution.getShape(), 2);
        }

        @Override
        public Double get() {

            if (state.getYear() >= yearToStart) {
                this.logNormalDistribution.reseedRandomGenerator(state.getRandom().nextLong());
                return -1 + (logNormalDistribution.sample(1)[0] - adjustment);
            } else
                return 0d;
        }


    }
}
