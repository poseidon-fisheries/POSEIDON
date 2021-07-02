/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import static java.lang.Math.min;

public class LinearFadBiomassAttractor implements FadBiomassAttractor {

    private final double attractionRate;
    private final double carryingCapacity;

    public LinearFadBiomassAttractor(final double attractionRate, final double carryingCapacity) {
        this.attractionRate = attractionRate;
        this.carryingCapacity = carryingCapacity;
    }

    @Override
    public double getCarryingCapacity() {
        return carryingCapacity;
    }

    @Override
    public boolean shouldAttract(final double cellBiomass, final double totalFadBiomass) {
        return true;
    }

    @Override
    public double biomassAttracted(final double tileBiomass, final double fadBiomass, final double totalFadBiomass) {
        return min(attractionRate * tileBiomass, carryingCapacity - fadBiomass);
    }

}
