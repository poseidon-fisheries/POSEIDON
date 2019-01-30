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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.AbundanceFilter;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Arrays;
import java.util.Objects;


/**
 * A gear that works on abundance and applies the same series of filters to all species equally
 * Created by carrknight on 3/10/16.
 */
public class HomogeneousAbundanceGear implements Gear {


    /**
     * the list of all filters, to use sequentially
     */
    private final AbundanceFilter[] filters;


    /**
     * fixed gas cost per hour of effort
     */
    private final double litersOfGasConsumedEachHourFishing;

    /**
     * creates (and fix) the gear given the following abundance filters
     * @param filters
     */
    public HomogeneousAbundanceGear(double litersOfGasConsumedEachHourFishing,
                                    AbundanceFilter... filters) {
        this.filters = Arrays.copyOf(filters,filters.length);
        this.litersOfGasConsumedEachHourFishing=litersOfGasConsumedEachHourFishing;
        Preconditions.checkArgument(filters.length > 0, "no filters provided");
    }


    @Override
    public Catch fish(
            Fisher fisher, LocalBiology localBiology, SeaTile context,
            int hoursSpentFishing, GlobalBiology modelBiology)
    {
        StructuredAbundance[] catches = catchesToArray(localBiology, hoursSpentFishing, modelBiology);


        return new Catch(catches,modelBiology);


    }

    private StructuredAbundance[] catchesToArray(
            LocalBiology where, int hoursSpentFishing, GlobalBiology modelBiology) {
        //create array containing biomass
        StructuredAbundance[] abundances = new StructuredAbundance[modelBiology.getSize()];
        for(Species species : modelBiology.getSpecies())
        {
            abundances[species.getIndex()] = catchesAsAbundanceForThisSpecies(where, hoursSpentFishing, species);
        }
        return abundances;
    }

    /**
     * this is a way to apply the gear to a species only. Useful for heterogeneous abundance gear
     * @param where
     * @param hoursSpentFishing
     * @param species
     * @return
     */
    public StructuredAbundance catchesAsAbundanceForThisSpecies(LocalBiology where, int hoursSpentFishing, Species species) {
        //prepare empty array
        prepTempAbundance(species);

        double[][] catches = tempAbundance;


        //you are going to fish every hour until you are done
        int hoursSpentFishingThisSpecies = hoursSpentFishing;

        while (hoursSpentFishingThisSpecies > 0) {
            double[][] hourlyCatches = fishThisSpecies(where, species);
            for (int cohort = 0; cohort < catches.length; cohort++)
                for (int bin = 0; bin < catches[0].length; bin++)
                    catches[cohort][bin] += hourlyCatches[cohort][bin];

            hoursSpentFishingThisSpecies = hoursSpentFishingThisSpecies - 1;

        }
        return new StructuredAbundance(catches);
    }

    //basically if every hour of fishing we create a matrix we are going to collapse under our own weight
    //recycle the abundance matrix instead. This is dangerous but every time this is called it should be safe to assume
    //that the previous numbers have by now been used
    private void prepTempAbundance(Species species) {
        if(tempAbundance==null)
            tempAbundance = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
        else {
            Preconditions.checkArgument(tempAbundance.length==species.getNumberOfSubdivisions());
            Preconditions.checkArgument(tempAbundance[0].length==species.getNumberOfBins());
            for (double[] row : tempAbundance)
                Arrays.fill(row, 0);
        }
    }

    private double tempAbundance[][];
    private double tempLocationalAbundance[][];

    private double[][] prepTemplocationalAbundance(Species species) {
        if(tempLocationalAbundance==null)
            tempLocationalAbundance = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
        else {
            Preconditions.checkArgument(tempLocationalAbundance.length==species.getNumberOfSubdivisions());
            Preconditions.checkArgument(tempLocationalAbundance[0].length==species.getNumberOfBins());
            for (double[] row : tempLocationalAbundance)
                Arrays.fill(row, 0);
        }
        return tempLocationalAbundance;
    }


    @Override
    public double[] expectedHourlyCatch(
            Fisher fisher, SeaTile where, int hoursSpentFishing, GlobalBiology modelBiology) {
        StructuredAbundance[] abundances = catchesToArray(where, hoursSpentFishing, modelBiology);
        assert modelBiology.getSpecies().size() == abundances.length;

        double[] weights = new double[abundances.length];
        for(Species species : modelBiology.getSpecies())
            weights[species.getIndex()] = abundances[species.getIndex()].computeWeight(species);

        return weights;
    }


    /**
     * fish for one hour targeting one species and returns the abundance caught
     * @param where where the fishing occurs
     * @param species the species considered
     * @return
     */
    protected double[][] fishThisSpecies(
            LocalBiology where, Species species) {
        //get the array of the fish (but perform a safety copy)

        double[][] fish = prepTemplocationalAbundance(species);
        double[][] realValues = where.getAbundance(species).asMatrix();
        boolean isNonZero = false;
        for(int subdivision=0; subdivision<realValues.length; subdivision++)
            for(int bin=0; bin<realValues[0].length; bin++) {
                fish[subdivision][bin] = realValues[subdivision][bin];
                if(realValues[subdivision][bin]>0)
                    isNonZero = true;
            }
        //filter until you get the catch
        if(isNonZero)
            fish = filter(species, fish);

        return fish;
    }

    /**
     * this is just the loop that calls all filters in order used by the gear when fishing.
     * It's visible so one can test that the numbers are right
     * @param species the species being fished
     * @param abundance a matrix Pof # of subdivisions} columns and MAX_AGE rows
     * @return a matrix of 2 columns and MAX_AGE rows corresponding to what was caught
     */
    @VisibleForTesting
    public double[][] filter(Species species, double[][] abundance) {
        for (AbundanceFilter filter : filters)
            abundance = filter.filter(
                    species,abundance );
        return abundance;
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
        return litersOfGasConsumedEachHourFishing;
    }

    @Override
    public Gear makeCopy() {
        return new HomogeneousAbundanceGear(litersOfGasConsumedEachHourFishing,
                                            filters);


    }

    @Override
    public boolean isSame(Gear o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HomogeneousAbundanceGear that = (HomogeneousAbundanceGear) o;
        return Double.compare(that.litersOfGasConsumedEachHourFishing, litersOfGasConsumedEachHourFishing) == 0 &&
                Objects.equals(filters, that.filters);
    }

    public double getLitersOfGasConsumedEachHourFishing() {
        return litersOfGasConsumedEachHourFishing;
    }
}
