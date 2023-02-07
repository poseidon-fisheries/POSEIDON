/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.HabitatAwareRandomCatchability;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates HabitatAwareRandomCatchability gears
 * Created by carrknight on 9/30/15.
 */
public class HabitatAwareGearFactory  implements AlgorithmFactory<HabitatAwareRandomCatchability>
{


    private DoubleParameter meanCatchabilityRocky = new FixedDoubleParameter(.01);

    private DoubleParameter standardDeviationCatchabilityRocky = new FixedDoubleParameter(0);


    private DoubleParameter meanCatchabilitySandy = new FixedDoubleParameter(.01);

    private DoubleParameter standardDeviationCatchabilitySandy = new FixedDoubleParameter(0);

    private DoubleParameter trawlSpeed = new FixedDoubleParameter(5);


    public HabitatAwareGearFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HabitatAwareRandomCatchability apply(FishState state) {


        int species = state.getSpecies().size();
        double[] meansRocky = new double[species];
        double[] meansSandy = new double[species];
        double[] stdRocky = new double[species];
        double[] stdSandy = new double[species];


        for(int i=0; i<meansSandy.length; i++)
        {
            meansRocky[i] = meanCatchabilityRocky.apply(state.getRandom());
            meansSandy[i] = meanCatchabilitySandy.apply(state.getRandom());
            stdSandy[i] = standardDeviationCatchabilitySandy.apply(state.getRandom());
            stdRocky[i] = standardDeviationCatchabilityRocky.apply(state.getRandom());
        }

        return new HabitatAwareRandomCatchability(meansSandy,stdSandy,meansRocky,stdRocky,
                                                  trawlSpeed.apply(state.getRandom()));

    }

    public DoubleParameter getMeanCatchabilityRocky() {
        return meanCatchabilityRocky;
    }

    public void setMeanCatchabilityRocky(DoubleParameter meanCatchabilityRocky) {
        this.meanCatchabilityRocky = meanCatchabilityRocky;
    }

    public DoubleParameter getStandardDeviationCatchabilityRocky() {
        return standardDeviationCatchabilityRocky;
    }

    public void setStandardDeviationCatchabilityRocky(
            DoubleParameter standardDeviationCatchabilityRocky) {
        this.standardDeviationCatchabilityRocky = standardDeviationCatchabilityRocky;
    }

    public DoubleParameter getMeanCatchabilitySandy() {
        return meanCatchabilitySandy;
    }

    public void setMeanCatchabilitySandy(DoubleParameter meanCatchabilitySandy) {
        this.meanCatchabilitySandy = meanCatchabilitySandy;
    }

    public DoubleParameter getStandardDeviationCatchabilitySandy() {
        return standardDeviationCatchabilitySandy;
    }

    public void setStandardDeviationCatchabilitySandy(
            DoubleParameter standardDeviationCatchabilitySandy) {
        this.standardDeviationCatchabilitySandy = standardDeviationCatchabilitySandy;
    }

    public DoubleParameter getTrawlSpeed() {
        return trawlSpeed;
    }

    public void setTrawlSpeed(DoubleParameter trawlSpeed) {
        this.trawlSpeed = trawlSpeed;
    }
}
