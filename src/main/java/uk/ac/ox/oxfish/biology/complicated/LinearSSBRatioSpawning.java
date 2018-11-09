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

package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

public class LinearSSBRatioSpawning extends YearlyRecruitmentProcess {


    final private double virginRecruits;

    final private double lenghtAtMaturity;

    final private double virginSpawningBiomass;


    /**
     * Getter for property 'virginRecruits'.
     *
     * @return Value for property 'virginRecruits'.
     */
    public double getVirginRecruits() {
        return virginRecruits;
    }

    /**
     * Getter for property 'lenghtAtMaturity'.
     *
     * @return Value for property 'lenghtAtMaturity'.
     */
    public double getLenghtAtMaturity() {
        return lenghtAtMaturity;
    }

    /**
     * Getter for property 'virginSpawningBiomass'.
     *
     * @return Value for property 'virginSpawningBiomass'.
     */
    public double getVirginSpawningBiomass() {
        return virginSpawningBiomass;
    }

    public LinearSSBRatioSpawning(double virginRecruits, double lenghtAtMaturity, double virginSpawningBiomass) {
        this.virginRecruits = virginRecruits;
        this.lenghtAtMaturity = lenghtAtMaturity;
        this.virginSpawningBiomass = virginSpawningBiomass;
    }

    @Override
    protected double recruitYearly(
            Species species, Meristics meristics, StructuredAbundance abundance) {

        double currentSpawningBiomass = 0;

        for(int i=0; i<abundance.getSubdivisions(); i++)
            for(int j=0; j<abundance.getBins(); j++)
                if(species.getLength(i,j)>=lenghtAtMaturity)
                    currentSpawningBiomass+= FishStateUtilities.weigh(abundance,meristics,i,j);

        double ratio = Math.min(currentSpawningBiomass / virginSpawningBiomass,1);
        return virginRecruits* ratio;
    }

    /**
     * give a function to generate noise as % of recruits this year
     *
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    @Override
    public void addNoise(NoiseMaker noiseMaker) {

    }
}
