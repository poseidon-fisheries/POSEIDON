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
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Arrays;
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
            Fisher fisher, LocalBiology localBiology, SeaTile context,
            int hoursSpentFishing, GlobalBiology modelBiology)
    {
        Preconditions.checkArgument(hoursSpentFishing>0);
        //create array containing biomass
        return new Catch(catchesAsArray(localBiology, hoursSpentFishing, modelBiology), modelBiology);
    }


    //a temp keeping a bunch of empty abundances so that when we hit an area with no fish we can just recycle these
    //rather than building more matrices
    private StructuredAbundance[] emptyCatchesCache;

    private StructuredAbundance getEmptyCatches(GlobalBiology biology, Species species)
    {
        if(emptyCatchesCache==null)
            emptyCatchesCache = new StructuredAbundance[biology.getSize()];
        if(emptyCatchesCache[species.getIndex()]==null)
            emptyCatchesCache[species.getIndex()] = new StructuredAbundance(species.getNumberOfSubdivisions(),species.getNumberOfBins());
        else
            for(int row=0; row<species.getNumberOfSubdivisions(); row++)
                Arrays.fill(emptyCatchesCache[species.getIndex()].asMatrix()[row],0d);
        return emptyCatchesCache[species.getIndex()];
    }

    private StructuredAbundance[] catchesAsArray(
            LocalBiology where, int hoursSpentFishing, GlobalBiology modelBiology) {


        StructuredAbundance[] caught = new  StructuredAbundance[modelBiology.getSize()];
        for(Species species : modelBiology.getSpecies())
        {
            if(species.isImaginary() || !gears.containsKey(species) )
            {
                //if it's imaginary or not set or there is no fish, just return empty
                //double[][] abundance = HomogeneousAbundanceGear.emptyAbundance(species);
                caught[species.getIndex()] = getEmptyCatches(modelBiology,species);

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
        double sum = 0;
        for(Gear gear : gears.values())
            //this is a ugly hack but it's surprising how expensive this averaging gets
            sum+=(gear.getFuelConsumptionPerHourOfFishing(null,null,null));
        return sum/gears.size();
    }

    @Override
    public Gear makeCopy() {
        return new HeterogeneousAbundanceGear(gears);
    }


    @Override
    public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeterogeneousAbundanceGear that = (HeterogeneousAbundanceGear) o;
        return Objects.equals(gears, that.gears);
    }

}
