/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.equipment;

/**
 * Just a dedicated counter to keep track of the fuel left!
 * Created by carrknight on 7/15/15.
 */
public class FuelTank {


    /**
     * the total amount of fuel that can be loaded in this tank
     */
    final private double fuelCapacityInLiters;

    /**
     * how much fuel left in tank
     */
    private double litersOfFuelInTank;


    public FuelTank(double fuelCapacityInLiters) {
        this.fuelCapacityInLiters = fuelCapacityInLiters;
        litersOfFuelInTank = fuelCapacityInLiters;
    }


    public double getFuelCapacityInLiters() {
        return fuelCapacityInLiters;
    }

    public double getLitersOfFuelInTank() {
        return litersOfFuelInTank;
    }


    /**
     * fill the tank to the brim.
     *
     * @return how much gas had to be put in
     */
    public double refill() {
        double added = fuelCapacityInLiters - litersOfFuelInTank;
        assert added >= 0;
        litersOfFuelInTank = fuelCapacityInLiters;
        return added;
    }

    public void consume(double litersOfGasConsumed) {
        litersOfFuelInTank -= litersOfGasConsumed;
        //  litersOfFuelInTank = FishStateUtilities.round(litersOfFuelInTank);
    }
}
