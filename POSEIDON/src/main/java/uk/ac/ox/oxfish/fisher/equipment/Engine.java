/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.equipment;

/**
 * A simple object/struct holding the weight of the engine and its fuel efficency plus some
 * utility methods to compute gas consumption
 * Created by carrknight on 7/15/15.
 */
public class Engine {

    /**
     * the power of the engine in bhp. Unused.
     */
    private final double powerInBhp;

    /**
     * the efficency of the engine as how many liters of gas are consumed for each kilometer travelled
     */
    private final double efficiencyAsLitersPerKm;


    /**
     * speed of the boat in knots
     */
    private final double speedInKph;


    public Engine(double powerInBhp, double efficiencyAsLitersPerKm, double speedInKph) {
        this.powerInBhp = powerInBhp;
        this.efficiencyAsLitersPerKm = efficiencyAsLitersPerKm;
        this.speedInKph = speedInKph;
    }

    public double getPowerInBhp() {
        return powerInBhp;
    }

    public double getEfficiencyAsLitersPerKm() {
        return efficiencyAsLitersPerKm;
    }


    public double getGasConsumptionPerKm(double KmTravelled) {
        return efficiencyAsLitersPerKm * KmTravelled;
    }

    public double getSpeedInKph() {
        return speedInKph;
    }
}
