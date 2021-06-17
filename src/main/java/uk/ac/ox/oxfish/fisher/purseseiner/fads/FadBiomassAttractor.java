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

import ec.util.MersenneTwisterFast;

import static java.lang.Math.*;

public class FadBiomassAttractor {

    private final MersenneTwisterFast rng;
    private final double intercept;
    private final double tileBiomassCoefficient;
    private final double fadBiomassCoefficient;
    private final double growthRate;
    private final double carryingCapacity;

    public FadBiomassAttractor(
        final MersenneTwisterFast rng,
        final double intercept,
        final double tileBiomassCoefficient,
        final double fadBiomassCoefficient,
        final double growthRate,
        final double carryingCapacity
    ) {
        this.rng = rng;
        this.intercept = intercept;
        this.tileBiomassCoefficient = tileBiomassCoefficient;
        this.fadBiomassCoefficient = fadBiomassCoefficient;
        this.growthRate = growthRate;
        this.carryingCapacity = carryingCapacity;
    }

    public double getCarryingCapacity() {
        return carryingCapacity;
    }

    boolean shouldAttract(final double cellBiomass, final double totalFadBiomass) {
        return rng.nextDouble() < probabilityOfAttraction(cellBiomass, totalFadBiomass);
    }

    private double probabilityOfAttraction(final double cellBiomass, final double totalFadBiomass) {
        return 1 / (1 + exp(-(intercept + tileBiomassCoefficient * cellBiomass + fadBiomassCoefficient * totalFadBiomass)));
    }

    double biomassAttracted(final double tileBiomass, final double fadBiomass, final double totalFadBiomass) {
        return min(
            tileBiomass,
            max(1, growthRate * tileBiomass * fadBiomass * (1 - totalFadBiomass / carryingCapacity))
        );
    }

}
