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

package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.boxcars.EquallySpacedBertalanffyFactory;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;


/**
 * Single species Boxcar factory that assumes bins are all equally spaced (5cm, 10cm, etc...)
 */
public class SingleSpeciesRegularBoxcarFactory extends SingleSpeciesBoxcarAbstractFactory {


    private double cmPerBin = 5;

    @Override
    protected GrowthBinByList generateBins(FishState state) {
        EquallySpacedBertalanffyFactory  meristic = new EquallySpacedBertalanffyFactory();
        meristic.setCmPerBin(getCmPerBin());
        meristic.setNumberOfBins(getNumberOfBins());
        meristic.setAllometricAlpha(getAllometricAlpha());
        meristic.setAllometricBeta(getAllometricBeta());
        meristic.setRecruitLengthInCm(new FixedDoubleParameter(0));
        meristic.setMaxLengthInCm(getLInfinity());
        meristic.setkYearlyParameter(getK());
        GrowthBinByList meristicsInstance = meristic.apply(state);
        return meristicsInstance;
    }


    /**
     * Getter for property 'cmPerBin'.
     *
     * @return Value for property 'cmPerBin'.
     */
    public double getCmPerBin() {
        return cmPerBin;
    }

    /**
     * Setter for property 'cmPerBin'.
     *
     * @param cmPerBin Value to set for property 'cmPerBin'.
     */
    public void setCmPerBin(double cmPerBin) {
        this.cmPerBin = cmPerBin;
    }
}
