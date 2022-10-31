/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.BiasedCurrentVectors;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.util.Map;

public class BiasedFadMapFactory extends GenericFadMapFactory {


    private DoubleParameter biasY = new FixedDoubleParameter(0);

    private DoubleParameter biasX = new FixedDoubleParameter(0);

    private DoubleParameter gridYMinimum = new FixedDoubleParameter(0);

    private DoubleParameter gridYMaximum = new FixedDoubleParameter(0);



    public BiasedFadMapFactory(
            Map<CurrentPattern, Path> currentFiles) {
        super(currentFiles);
    }

    public BiasedFadMapFactory() {
    }

    @Override
    protected CurrentVectors buildCurrentVector(NauticalMap nauticalMap,
                                                FishState fishState) {
        return
                new BiasedCurrentVectors(super.buildCurrentVector(nauticalMap,fishState),
                                         biasY.apply(fishState.getRandom()),
                                         biasX.apply(fishState.getRandom()),
                                         gridYMinimum.apply(fishState.getRandom()).intValue(),
                                         gridYMaximum.apply(fishState.getRandom()).intValue()
                                         );

    }

    public DoubleParameter getBiasY() {
        return biasY;
    }

    public void setBiasY(DoubleParameter biasY) {
        this.biasY = biasY;
    }

    public DoubleParameter getBiasX() {
        return biasX;
    }

    public void setBiasX(DoubleParameter biasX) {
        this.biasX = biasX;
    }

    public DoubleParameter getGridYMinimum() {
        return gridYMinimum;
    }

    public void setGridYMinimum(DoubleParameter gridYMinimum) {
        this.gridYMinimum = gridYMinimum;
    }

    public DoubleParameter getGridYMaximum() {
        return gridYMaximum;
    }

    public void setGridYMaximum(DoubleParameter gridYMaximum) {
        this.gridYMaximum = gridYMaximum;
    }
}
