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

package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.ConstantCurrentVector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class FadMapDummyFactory implements AlgorithmFactory<FadMap> {

    private boolean isBiomassOnly = false;


    private DoubleParameter fixedXCurrent = new FixedDoubleParameter(+1);
    private DoubleParameter fixedYCurrent = new FixedDoubleParameter(-1);

    public DoubleParameter getFixedXCurrent() {
        return fixedXCurrent;
    }

    public void setFixedXCurrent(final DoubleParameter fixedXCurrent) {
        this.fixedXCurrent = fixedXCurrent;
    }

    public DoubleParameter getFixedYCurrent() {
        return fixedYCurrent;
    }

    public void setFixedYCurrent(final DoubleParameter fixedYCurrent) {
        this.fixedYCurrent = fixedYCurrent;
    }

    @Override
    public FadMap apply(final FishState fishState) {


        final NauticalMap map = fishState.getMap();
        return new FadMap(
            map,
            getCurrentVectors(fishState, map),
            fishState.getBiology(),
            isBiomassOnly ? BiomassLocalBiology.class : AbundanceLocalBiology.class
        );


    }

    protected ConstantCurrentVector getCurrentVectors(final FishState fishState, final NauticalMap map) {
        return new ConstantCurrentVector(
            fixedXCurrent.applyAsDouble(fishState.getRandom()),
            fixedYCurrent.applyAsDouble(fishState.getRandom()),
            map.getHeight(),
            map.getWidth()
        );
    }

    public boolean isBiomassOnly() {
        return isBiomassOnly;
    }

    public void setBiomassOnly(final boolean biomassOnly) {
        isBiomassOnly = biomassOnly;
    }
}
