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

import uk.ac.ox.oxfish.geography.currents.BiasedCurrentVectors;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.util.Map;

@SuppressWarnings("unused")
public class BiasedFadMapFactory extends GenericFadMapFactory {

    private DoubleParameter biasY = new FixedDoubleParameter(0);

    private DoubleParameter biasX = new FixedDoubleParameter(0);

    private DoubleParameter gridYMinimum = new FixedDoubleParameter(0);

    private DoubleParameter gridYMaximum = new FixedDoubleParameter(0);

    @SuppressWarnings("unused")
    public BiasedFadMapFactory(Map<CurrentPattern, Path> currentFiles) {
        super(currentFiles);
    }

    @SuppressWarnings("unused")
    public BiasedFadMapFactory() {
    }

    @Override
    protected CurrentVectors buildCurrentVectors(FishState fishState) {
        return
            new BiasedCurrentVectors(
                super.buildCurrentVectors(fishState),
                biasY.apply(fishState.getRandom()),
                biasX.apply(fishState.getRandom()),
                gridYMinimum.apply(fishState.getRandom()).intValue(),
                gridYMaximum.apply(fishState.getRandom()).intValue()
            );

    }

    @SuppressWarnings("unused")
    public DoubleParameter getBiasY() {
        return biasY;
    }

    @SuppressWarnings("unused")
    public void setBiasY(DoubleParameter biasY) {
        this.biasY = biasY;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getBiasX() {
        return biasX;
    }

    @SuppressWarnings("unused")
    public void setBiasX(DoubleParameter biasX) {
        this.biasX = biasX;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getGridYMinimum() {
        return gridYMinimum;
    }

    @SuppressWarnings("unused")
    public void setGridYMinimum(DoubleParameter gridYMinimum) {
        this.gridYMinimum = gridYMinimum;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getGridYMaximum() {
        return gridYMaximum;
    }

    @SuppressWarnings("unused")
    public void setGridYMaximum(DoubleParameter gridYMaximum) {
        this.gridYMaximum = gridYMaximum;
    }
}