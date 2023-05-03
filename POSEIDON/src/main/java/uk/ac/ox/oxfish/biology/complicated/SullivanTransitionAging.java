/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.boxcars.SullivanTransitionProbability;
import uk.ac.ox.oxfish.model.FishState;

/**
 * creates and manages transition probability as in Sullivan; usese LocalSullivanTransitionAging as a delegate
 */
public class SullivanTransitionAging extends LocalAgingProcess {


    private final double k;

    private final double LInfinity;

    private final double gammaScaleParameter;

    private final int agingPeriodInDays;

    private LocalSullivanTransitionAging delegate;


    public SullivanTransitionAging(double k, double LInfinity, double gammaScaleParameter, int agingPeriodInDays) {
        this.k = k;
        this.LInfinity = LInfinity;
        this.gammaScaleParameter = gammaScaleParameter;
        this.agingPeriodInDays = agingPeriodInDays;
    }

    @Override
    public void ageLocally(
            AbundanceLocalBiology localBiology, Species species, FishState model, boolean rounding,
            int daysToSimulate) {

        Preconditions.checkState(delegate!=null, "not started!");
        delegate.ageLocally(localBiology, species, model, rounding, daysToSimulate);

    }

    private Species speciesConnected = null;

    /**
     * called after the aging process has been initialized but before it is run.
     *
     * @param species
     */
    @Override
    public void start(Species species) {
        Preconditions.checkState(speciesConnected==null);
        speciesConnected = species; //you don't want to re-use this for multiple species!!
        initializeTransitionMatrix();
    }

    private void initializeTransitionMatrix(){

        assert delegate==null;
        assert speciesConnected!=null;
        SullivanTransitionProbability[] probabilities = new SullivanTransitionProbability[speciesConnected.getNumberOfSubdivisions()];
        for (int subdivision = 0; subdivision < speciesConnected.getNumberOfSubdivisions(); subdivision++) {
            probabilities[subdivision] = new SullivanTransitionProbability(
                    gammaScaleParameter,
                    LInfinity,
                    k,
                    agingPeriodInDays/365d,
                    subdivision,
                    speciesConnected

            );


        }
        delegate = new LocalSullivanTransitionAging(probabilities,agingPeriodInDays);
        delegate.start(speciesConnected);

    }


    /**
     * Getter for property 'k'.
     *
     * @return Value for property 'k'.
     */
    public double getK() {
        return k;
    }

    /**
     * Getter for property 'LInfinity'.
     *
     * @return Value for property 'LInfinity'.
     */
    public double getLInfinity() {
        return LInfinity;
    }

    /**
     * Getter for property 'gammaScaleParameter'.
     *
     * @return Value for property 'gammaScaleParameter'.
     */
    public double getGammaScaleParameter() {
        return gammaScaleParameter;
    }

    /**
     * Getter for property 'agingPeriodInDays'.
     *
     * @return Value for property 'agingPeriodInDays'.
     */
    public int getAgingPeriodInDays() {
        return agingPeriodInDays;
    }
}
