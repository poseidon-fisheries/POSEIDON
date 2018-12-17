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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;
import java.util.List;

/**
 * Like random catchability thrawl but the catchability depends on it.
 * Created by carrknight on 9/29/15.
 */
public class HabitatAwareRandomCatchability implements Gear {


    private final double[] sandyCatchabilityMeanPerSpecie;

    private final double[] sandyCatchabilityDeviationPerSpecie;

    private final double[] rockCatchabilityMeanPerSpecie;

    private final double[] rockCatchabilityDeviationPerSpecie;


    /**
     * speed (used for fuel consumption) of thrawling
     */
    private  final double trawlSpeed;


    public HabitatAwareRandomCatchability(
            double[] sandyCatchabilityMeanPerSpecie, double[] sandyCatchabilityDeviationPerSpecie,
            double[] rockCatchabilityMeanPerSpecie, double[] rockCatchabilityDeviationPerSpecie,
            double trawlSpeed) {
        this.sandyCatchabilityMeanPerSpecie = sandyCatchabilityMeanPerSpecie;
        this.sandyCatchabilityDeviationPerSpecie = sandyCatchabilityDeviationPerSpecie;
        this.rockCatchabilityMeanPerSpecie = rockCatchabilityMeanPerSpecie;
        this.rockCatchabilityDeviationPerSpecie = rockCatchabilityDeviationPerSpecie;
        this.trawlSpeed = trawlSpeed;
    }

    @Override
    public Catch fish(
            Fisher fisher, LocalBiology localBiology, SeaTile context,
            int hoursSpentFishing, GlobalBiology modelBiology) {
        List<Species> species = modelBiology.getSpecies();
        double[] totalCatch = catchesAsArray(fisher, context, hoursSpentFishing, modelBiology, species);
        return new Catch(totalCatch);


    }

    private double[] catchesAsArray(
            Fisher fisher, SeaTile where, int hoursSpentFishing,
            GlobalBiology modelBiology,
            List<Species> species) {
        double[] totalCatch = new double[modelBiology.getSize()];
        for(Species specie : species)
        {
            double sandyQ = fisher.grabRandomizer().nextGaussian()* sandyCatchabilityDeviationPerSpecie[specie.getIndex()]
                    + sandyCatchabilityMeanPerSpecie[specie.getIndex()];
            double rockyQ = fisher.grabRandomizer().nextGaussian()* rockCatchabilityDeviationPerSpecie[specie.getIndex()]
                    + rockCatchabilityMeanPerSpecie[specie.getIndex()];

            double q = sandyQ * (1d-where.getRockyPercentage()) + rockyQ * where.getRockyPercentage();

            totalCatch[specie.getIndex()] =
                    FishStateUtilities.catchSpecieGivenCatchability(where, hoursSpentFishing, specie, q);
        }
        return totalCatch;
    }

    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        return catchesAsArray(fisher, where, hoursSpentFishing, modelBiology, modelBiology.getSpecies());
    }

    /**
     * get how much gas is consumed by fishing a spot with this gear
     *
     * @param fisher the dude fishing
     * @param boat
     * @param where  the location being fished  @return liters of gas consumed for every hour spent fishing
     */
    @Override
    public double getFuelConsumptionPerHourOfFishing(
            Fisher fisher, Boat boat, SeaTile where) {
        return boat.expectedFuelConsumption(trawlSpeed);
    }



    @Override
    public Gear makeCopy() {

        return new HabitatAwareRandomCatchability(
                Arrays.copyOf(sandyCatchabilityMeanPerSpecie,sandyCatchabilityMeanPerSpecie.length),
                Arrays.copyOf(sandyCatchabilityDeviationPerSpecie,sandyCatchabilityDeviationPerSpecie.length),
                Arrays.copyOf(rockCatchabilityMeanPerSpecie,rockCatchabilityMeanPerSpecie.length),
                Arrays.copyOf(rockCatchabilityDeviationPerSpecie,rockCatchabilityDeviationPerSpecie.length),
                trawlSpeed);


    }

    @Override
    public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabitatAwareRandomCatchability that = (HabitatAwareRandomCatchability) o;
        return Double.compare(that.trawlSpeed, trawlSpeed) == 0 &&
                Arrays.equals(sandyCatchabilityMeanPerSpecie, that.sandyCatchabilityMeanPerSpecie) &&
                Arrays.equals(sandyCatchabilityDeviationPerSpecie, that.sandyCatchabilityDeviationPerSpecie) &&
                Arrays.equals(rockCatchabilityMeanPerSpecie, that.rockCatchabilityMeanPerSpecie) &&
                Arrays.equals(rockCatchabilityDeviationPerSpecie, that.rockCatchabilityDeviationPerSpecie);
    }



}
