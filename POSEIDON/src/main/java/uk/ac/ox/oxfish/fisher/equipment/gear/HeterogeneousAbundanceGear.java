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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A map species ---> homogeneousAbudanceGear so that each species has a different selectivity and such.
 * Throws an exception if it catches a species for which it has no gear
 * Created by carrknight on 5/17/16.
 */
public class HeterogeneousAbundanceGear implements Gear {

    /**
     * the map holding the gears used
     */
    private final HashMap<Species, HomogeneousAbundanceGear> gears;


    /**
     * this can be set separately: when this is a finite number then hourly gas price is not the average of each gear
     * but rather this number
     */
    private Double hourlyGasPriceOverride = null;
    //a temp keeping a bunch of empty abundances so that when we hit an area with no fish we can just recycle these
    //rather than building more matrices
    private StructuredAbundance[] emptyCatchesCache;


    @SafeVarargs
    public HeterogeneousAbundanceGear(final Entry<Species, HomogeneousAbundanceGear>... gearPairs) {
        gears = new HashMap<>();
        for (final Entry<Species, HomogeneousAbundanceGear> gearPair : gearPairs) {
            gears.put(gearPair.getKey(), gearPair.getValue());
        }

    }

    public HeterogeneousAbundanceGear(
        final HashMap<Species, HomogeneousAbundanceGear> gears
    ) {
        this.gears = gears;
    }

    @Override
    public Catch fish(
        final Fisher fisher, final LocalBiology localBiology, final SeaTile context,
        final int hoursSpentFishing, final GlobalBiology modelBiology
    ) {
        Preconditions.checkArgument(hoursSpentFishing > 0);
        //create array containing biomass
        return new Catch(catchesAsArray(localBiology, hoursSpentFishing, modelBiology), modelBiology);
    }

    private StructuredAbundance[] catchesAsArray(
        final LocalBiology where, final int hoursSpentFishing, final GlobalBiology modelBiology
    ) {


        final StructuredAbundance[] caught = new StructuredAbundance[modelBiology.getSize()];
        for (final Species species : modelBiology.getSpecies()) {
            if (species.isImaginary() || !gears.containsKey(species)) {
                //if it's imaginary or not set or there is no fish, just return empty
                //double[][] abundance = HomogeneousAbundanceGear.emptyAbundance(species);
                caught[species.getIndex()] = getEmptyCatches(modelBiology, species);

            } else {
                caught[species.getIndex()] = gears.get(species)
                    .catchesAsAbundanceForThisSpecies(where, hoursSpentFishing, species);
            }

        }
        return caught;
    }

    private StructuredAbundance getEmptyCatches(final GlobalBiology biology, final Species species) {
        if (emptyCatchesCache == null)
            emptyCatchesCache = new StructuredAbundance[biology.getSize()];
        if (emptyCatchesCache[species.getIndex()] == null)
            emptyCatchesCache[species.getIndex()] = new StructuredAbundance(
                species.getNumberOfSubdivisions(),
                species.getNumberOfBins()
            );
        else
            for (int row = 0; row < species.getNumberOfSubdivisions(); row++)
                Arrays.fill(emptyCatchesCache[species.getIndex()].asMatrix()[row], 0d);
        return emptyCatchesCache[species.getIndex()];
    }

    @Override
    public double[] expectedHourlyCatch(
        final Fisher fisher, final SeaTile where, final int hoursSpentFishing, final GlobalBiology modelBiology
    ) {
        final StructuredAbundance[] abundances = catchesAsArray(where, hoursSpentFishing, modelBiology);
        assert modelBiology.getSpecies().size() == abundances.length;

        final double[] weights = new double[abundances.length];
        for (final Species species : modelBiology.getSpecies())
            weights[species.getIndex()] = abundances[species.getIndex()].computeWeight(species);

        return weights;
    }

    /**
     * Gas consumed is the average of all consumptions
     *
     * @param fisher the dude fishing
     * @param boat
     * @param where  the location being fished  @return liters of gas consumed for every hour spent fishing
     */
    @Override
    public double getFuelConsumptionPerHourOfFishing(
        final Fisher fisher, final Boat boat, final SeaTile where
    ) {
        if (hourlyGasPriceOverride != null && Double.isFinite(hourlyGasPriceOverride))
            return hourlyGasPriceOverride;
        else
            return getComponentsAverage();
    }

    private double getComponentsAverage() {
        double sum = 0;
        for (final Gear gear : gears.values())
            //this is a ugly hack but it's surprising how expensive this averaging gets
            sum += (gear.getFuelConsumptionPerHourOfFishing(null, null, null));
        return sum / gears.size();
    }

    @Override
    public Gear makeCopy() {
        final HeterogeneousAbundanceGear heterogeneousAbundanceGear = new HeterogeneousAbundanceGear(gears);
        heterogeneousAbundanceGear.setHourlyGasPriceOverride(this.hourlyGasPriceOverride);
        return heterogeneousAbundanceGear;
    }


    @Override
    public boolean isSame(final Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final HeterogeneousAbundanceGear that = (HeterogeneousAbundanceGear) o;
        if (!that.gears.keySet().equals(this.gears.keySet()))
            return false;
        for (final Species species : that.gears.keySet()) {
            if (!that.gears.get(species).isSame(this.gears.get(species)))
                return false;

        }

        return true;

    }

    /**
     * Getter for property 'hourlyGasPriceOverride'.
     *
     * @return Value for property 'hourlyGasPriceOverride'.
     */
    public Double getHourlyGasPriceOverride() {
        return hourlyGasPriceOverride;
    }

    /**
     * Setter for property 'hourlyGasPriceOverride'.
     *
     * @param hourlyGasPriceOverride Value to set for property 'hourlyGasPriceOverride'.
     */
    public void setHourlyGasPriceOverride(final Double hourlyGasPriceOverride) {
        this.hourlyGasPriceOverride = hourlyGasPriceOverride;
    }
}
