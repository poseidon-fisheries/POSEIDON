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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Objects;

/**
 * A map species ---> homogeneousAbudanceGear so that each species has a different selectivity and such.
 * Throws an exception if it catches a species for which it has no gear
 * Created by carrknight on 5/17/16.
 */
public class HeterogeneousAbundanceGear implements Gear
{

    /**
     * the map holding the gears used
     */
    private final HashMap<Species,HomogeneousAbundanceGear> gears;




    @SafeVarargs
    public HeterogeneousAbundanceGear(Pair<Species,HomogeneousAbundanceGear>... gearPairs) {
        gears = new HashMap<>();
        for(Pair<Species,HomogeneousAbundanceGear> gearPair : gearPairs)
        {
            gears.put(gearPair.getFirst(),gearPair.getSecond());
        }
    }

    public HeterogeneousAbundanceGear(
            HashMap<Species, HomogeneousAbundanceGear> gears) {
        this.gears = gears;
    }

    @Override
    public Catch fish(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology)
    {
        Preconditions.checkArgument(hoursSpentFishing>0);
        //create array containing biomass
        return new Catch(catchesAsArray(where, hoursSpentFishing, modelBiology),modelBiology);
    }

    private StructuredAbundance[] catchesAsArray(
            SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        StructuredAbundance[] caught = new  StructuredAbundance[modelBiology.getSize()];
        for(Species species : modelBiology.getSpecies())
        {
            if(species.isImaginary() || !gears.containsKey(species) || where.getBiology().getBiomass(species)<=0)
            {
                //if it's imaginary or not set or there is no fish, just return empty
                double[][] abundance = HomogeneousAbundanceGear.emptyAbundance(species);
                caught[species.getIndex()] = new StructuredAbundance(abundance);

            }
            else {
                caught[species.getIndex()] = gears.get(species).catchesAsAbundanceForThisSpecies(where,hoursSpentFishing,species);
            }

        }
        return caught;
    }

    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        StructuredAbundance[] abundances = catchesAsArray(where, hoursSpentFishing, modelBiology);
        assert modelBiology.getSpecies().size() == abundances.length;

        double[] weights = new double[abundances.length];
        for(Species species : modelBiology.getSpecies())
            weights[species.getIndex()] = abundances[species.getIndex()].computeWeight(species);

        return weights;
    }

    /**
     *  Gas consumed is the average of all consumptions
     *
     * @param fisher the dude fishing
     * @param boat
     * @param where  the location being fished  @return liters of gas consumed for every hour spent fishing
     */
    @Override
    public double getFuelConsumptionPerHourOfFishing(
            Fisher fisher, Boat boat, SeaTile where) {
        DoubleSummaryStatistics averager = new DoubleSummaryStatistics();
        for(Gear gear : gears.values())
            averager.accept(gear.getFuelConsumptionPerHourOfFishing(fisher,boat,where));
        return averager.getAverage();
    }

    @Override
    public Gear makeCopy() {
        return new HeterogeneousAbundanceGear(gears);
    }

    /**
     * Getter for property 'gears'.
     *
     * @return Value for property 'gears'.
     */
    public HashMap<Species, HomogeneousAbundanceGear> getGears() {
        return gears;
    }

    @Override
    public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeterogeneousAbundanceGear that = (HeterogeneousAbundanceGear) o;
        return Objects.equals(getGears(), that.getGears());
    }

}
