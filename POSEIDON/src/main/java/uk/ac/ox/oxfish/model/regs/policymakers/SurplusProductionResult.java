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

package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.base.MoreObjects;

public class SurplusProductionResult {


    private final double carryingCapacity;

    private final double logisticGrowth;

    private final double catchability;

    private final double[] depletion;

    private final double[] cpue;

    private final double[] landings;

    private final double[] biomass;


    public SurplusProductionResult(
        double carryingCapacity,
        double logisticGrowth,
        double catchability, double[] depletion,
        double[] cpue, double[] landings, double[] biomass
    ) {
        this.carryingCapacity = carryingCapacity;
        this.logisticGrowth = logisticGrowth;
        this.catchability = catchability;
        this.depletion = depletion;
        this.cpue = cpue;
        this.landings = landings;
        this.biomass = biomass;
    }

    public double getCarryingCapacity() {
        return carryingCapacity;
    }

    public double[] getDepletion() {
        return depletion;
    }

    public double[] getCpue() {
        return cpue;
    }

    public double[] getLandings() {
        return landings;
    }

    public double[] getBiomass() {
        return biomass;
    }

    public double getLogisticGrowth() {
        return logisticGrowth;
    }

    public double getCatchability() {
        return catchability;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("carryingCapacity", carryingCapacity)
            .add("logisticGrowth", logisticGrowth)
            .add("catchability", catchability)
            .add("depletion", depletion)
            .add("cpue", cpue)
            .add("landings", landings)
            .add("biomass", biomass)
            .toString();
    }
}
