/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Function;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;

public class RecruitmentBySpawningJackKnifeMaturity implements AlgorithmFactory<RecruitmentBySpawningBiomass> {

    /**
     * the number of recruits you'd get in a "virgin" state.
     */
    private DoubleParameter virginRecruits = new FixedDoubleParameter(40741397);



    /**
     * logistic growth parameter
     */
    private DoubleParameter steepness = new FixedDoubleParameter(0.6);


    private DoubleParameter cumulativePhi = new FixedDoubleParameter(14.2444066771724);


    private double lengthAtMaturity = 50;

    /**
     * if you have multiple subdivisions (most likely because you are accounting for male and female separately) then
     * this points out to the subdivision that matters in terms of maturity
     */
    private int subdivisionThatSpawns = 0;


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public RecruitmentBySpawningBiomass apply(FishState fishState) {

        return new RecruitmentBySpawningBiomass(
                virginRecruits.apply(fishState.getRandom()).intValue(),
                steepness.apply(fishState.getRandom()),
                cumulativePhi.apply(fishState.getRandom()),
                false,
                new Function<Species, double[]>() {
                    @Override
                    public double[] apply(Species species) {
                        double[] actualMaturity = new double[species.getNumberOfBins()];
                        for(int bin=0; bin<species.getNumberOfBins(); bin++)
                            actualMaturity[bin] = species.getLength(subdivisionThatSpawns,bin) >= lengthAtMaturity ? 1.0 : 0;
                        return actualMaturity;

                    }
                },
                null,
                subdivisionThatSpawns

        );

    }

    /**
     * Getter for property 'virginRecruits'.
     *
     * @return Value for property 'virginRecruits'.
     */
    public DoubleParameter getVirginRecruits() {
        return virginRecruits;
    }

    /**
     * Setter for property 'virginRecruits'.
     *
     * @param virginRecruits Value to set for property 'virginRecruits'.
     */
    public void setVirginRecruits(DoubleParameter virginRecruits) {
        this.virginRecruits = virginRecruits;
    }

    /**
     * Getter for property 'steepness'.
     *
     * @return Value for property 'steepness'.
     */
    public DoubleParameter getSteepness() {
        return steepness;
    }

    /**
     * Setter for property 'steepness'.
     *
     * @param steepness Value to set for property 'steepness'.
     */
    public void setSteepness(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    /**
     * Getter for property 'cumulativePhi'.
     *
     * @return Value for property 'cumulativePhi'.
     */
    public DoubleParameter getCumulativePhi() {
        return cumulativePhi;
    }

    /**
     * Setter for property 'cumulativePhi'.
     *
     * @param cumulativePhi Value to set for property 'cumulativePhi'.
     */
    public void setCumulativePhi(DoubleParameter cumulativePhi) {
        this.cumulativePhi = cumulativePhi;
    }

    /**
     * Getter for property 'lengthAtMaturity'.
     *
     * @return Value for property 'lengthAtMaturity'.
     */
    public double getLengthAtMaturity() {
        return lengthAtMaturity;
    }

    /**
     * Setter for property 'lengthAtMaturity'.
     *
     * @param lengthAtMaturity Value to set for property 'lengthAtMaturity'.
     */
    public void setLengthAtMaturity(double lengthAtMaturity) {
        this.lengthAtMaturity = lengthAtMaturity;
    }

    /**
     * Getter for property 'subdivisionThatSpawns'.
     *
     * @return Value for property 'subdivisionThatSpawns'.
     */
    public int getSubdivisionThatSpawns() {
        return subdivisionThatSpawns;
    }

    /**
     * Setter for property 'subdivisionThatSpawns'.
     *
     * @param subdivisionThatSpawns Value to set for property 'subdivisionThatSpawns'.
     */
    public void setSubdivisionThatSpawns(int subdivisionThatSpawns) {
        this.subdivisionThatSpawns = subdivisionThatSpawns;
    }


}
