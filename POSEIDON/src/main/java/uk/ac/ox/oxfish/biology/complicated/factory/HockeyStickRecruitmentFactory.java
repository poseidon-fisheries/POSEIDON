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

import uk.ac.ox.oxfish.biology.complicated.HockeyStickRecruitment;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class HockeyStickRecruitmentFactory implements AlgorithmFactory<HockeyStickRecruitment> {


    private DoubleParameter virginRecruits = new FixedDoubleParameter(6000000);

    private DoubleParameter lengthAtMaturity = new FixedDoubleParameter(50);

    private DoubleParameter virginSpawningBiomass = new FixedDoubleParameter(1000000);

    private DoubleParameter hinge = new FixedDoubleParameter(.3);


    @Override
    public HockeyStickRecruitment apply(final FishState state) {
        return new HockeyStickRecruitment(
            false,
            hinge.applyAsDouble(state.getRandom()),
            virginRecruits.applyAsDouble(state.getRandom()),
            lengthAtMaturity.applyAsDouble(state.getRandom()),
            virginSpawningBiomass.applyAsDouble(state.getRandom())

        );
    }

    public DoubleParameter getVirginRecruits() {
        return virginRecruits;
    }

    public void setVirginRecruits(final DoubleParameter virginRecruits) {
        this.virginRecruits = virginRecruits;
    }

    public DoubleParameter getLengthAtMaturity() {
        return lengthAtMaturity;
    }

    public void setLengthAtMaturity(final DoubleParameter lengthAtMaturity) {
        this.lengthAtMaturity = lengthAtMaturity;
    }

    public DoubleParameter getVirginSpawningBiomass() {
        return virginSpawningBiomass;
    }

    public void setVirginSpawningBiomass(final DoubleParameter virginSpawningBiomass) {
        this.virginSpawningBiomass = virginSpawningBiomass;
    }

    public DoubleParameter getHinge() {
        return hinge;
    }

    public void setHinge(final DoubleParameter hinge) {
        this.hinge = hinge;
    }
}
