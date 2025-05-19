/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.initializer.factory;

import com.google.common.base.Preconditions;
import org.jfree.util.Log;
import uk.ac.ox.oxfish.biology.boxcars.EquallySpacedBertalanffyFactory;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;


/**
 * Single species Boxcar factory that assumes bins are all equally spaced (5cm, 10cm, etc...)
 */
public class SingleSpeciesRegularBoxcarFactory extends SingleSpeciesBoxcarAbstractFactory {


    private double cmPerBin = 5;

    @Override
    protected GrowthBinByList generateBins(final FishState state) {
        if (getCmPerBin() * getNumberOfBins() + getCmPerBin() / 2 <= getLInfinity().applyAsDouble(state.getRandom()))
            Log.warn("The number of bins provided given their width won't reach l-infinity...");
        Preconditions.checkArgument(
            getCmPerBin() * getNumberOfBins() + getCmPerBin() / 2 >= getLInfinity().applyAsDouble(state.getRandom()) / 2,
            "bins do not reach even half of L_infinity. The biology is inconsistent!"
        );
        final EquallySpacedBertalanffyFactory meristic = new EquallySpacedBertalanffyFactory();
        meristic.setCmPerBin(getCmPerBin());
        meristic.setNumberOfBins(getNumberOfBins());
        meristic.setAllometricAlpha(getAllometricAlpha());
        meristic.setAllometricBeta(getAllometricBeta());
        meristic.setRecruitLengthInCm(new FixedDoubleParameter(0));
        meristic.setMaxLengthInCm(getLInfinity());
        meristic.setkYearlyParameter(getK());
        final GrowthBinByList meristicsInstance = meristic.apply(state);
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
    public void setCmPerBin(final double cmPerBin) {
        this.cmPerBin = cmPerBin;
    }
}
