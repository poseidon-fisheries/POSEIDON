/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.Map;

/**
 * The "all fads are created equal" approach to Abundance
 */
public class AbundanceLinearIntervalAttractor extends AbstractAbundanceLinearIntervalAttractor {



    private final int daysItTakesToFillUp;

    final double[] carryingCapacitiesPerSpecies;

    final double[] dailyBiomassAttractedPerSpecies;

    WeightedObject<AbundanceLocalBiology> dailyAttractionStep; //stored as a weighted object for speed

    HashMap<Species,double[][]>  dailyAttractionThreshold; //this will just be dailyAttractionStep times minAbundanceThreshold; but easy t store and skip all computations

    final double minAbundanceThreshold;

    final Map<Species,NonMutatingArrayFilter> globalSelectivityCurves;

    final FishState model;

    public AbundanceLinearIntervalAttractor(
            int daysInWaterBeforeAttraction, int daysItTakesToFillUp, double[] carryingCapacitiesPerSpecies,
            double minAbundanceThreshold,
            Map<Species, NonMutatingArrayFilter> selectivityFilters,
            FishState model
    ) {
        //there is only one rate of attraction, so it should be easy to compute this straight into the
        //
        super(selectivityFilters, model, daysInWaterBeforeAttraction);
        Preconditions.checkArgument(minAbundanceThreshold>=1);
        Preconditions.checkArgument(daysItTakesToFillUp>0);
        this.daysItTakesToFillUp = daysItTakesToFillUp;
        this.carryingCapacitiesPerSpecies = carryingCapacitiesPerSpecies;
        this.dailyBiomassAttractedPerSpecies= new double[carryingCapacitiesPerSpecies.length];
        for (int i = 0; i < carryingCapacitiesPerSpecies.length; i++) {
            this.dailyBiomassAttractedPerSpecies[i] =
                    carryingCapacitiesPerSpecies[i]/(double)daysItTakesToFillUp;

        }
        //turn biomass target into abundance target via selectivity
        assert selectivityFilters.size()==carryingCapacitiesPerSpecies.length;
        globalSelectivityCurves = selectivityFilters;

        this.model= model;
        this.minAbundanceThreshold = minAbundanceThreshold;


    }


    @Override
    public void step(SimState simState) {
        super.step(simState);
        //by construction here every FAD will have to attract the same daily kg, so we can just need to compute
        //one daily catch, save it as an abundance biology and just give it to any fad making contact

        HashMap<Species,double[][]> dailyAbundance =  new HashMap<>();
        dailyAttractionThreshold = new HashMap<>();
        HeterogeneousLinearIntervalAttractor.
                fillUpAttractionAndThresholdAttractionMatrices(dailyBiomassAttractedPerSpecies,
                                                               dailyAbundance,
                                                               dailyAttractionThreshold,
                                                               minAbundanceThreshold,
                                                               super.globalSelectivityCurves,
                                                               super.getAbundancePerDailyKgLanded());
        AbundanceLocalBiology toReturn = new AbundanceLocalBiology(dailyAbundance);
        dailyAttractionStep = new WeightedObject<>(toReturn,toReturn.getTotalBiomass());


    }


    @Override
    protected WeightedObject<AbundanceLocalBiology> attractDaily(
            AbundanceLocalBiology seaTileBiology, AbundanceFad fad) {
        return dailyAttractionStep;
    }


    public int getDaysItTakesToFillUp() {
        return daysItTakesToFillUp;
    }

    public double getCarryingCapacitiesPerSpecies(int speciesID) {
        return carryingCapacitiesPerSpecies[speciesID];
    }

    public double getDailyBiomassAttractedPerSpecies(int speciesID) {
        return dailyBiomassAttractedPerSpecies[speciesID];
    }

    @VisibleForTesting
    public WeightedObject<AbundanceLocalBiology> getDailyAttractionStep() {
        return dailyAttractionStep;
    }

    @VisibleForTesting
    public HashMap<Species, double[][]> getDailyAttractionThreshold() {
        return dailyAttractionThreshold;
    }

    public double getMinAbundanceThreshold() {
        return minAbundanceThreshold;
    }

    public Map<Species, NonMutatingArrayFilter> getGlobalSelectivityCurves() {
        return globalSelectivityCurves;
    }

    /**
     * get carrying capacities per fad in terms of KG per species
     *
     * @param fad
     * @return
     */
    @Override
    public double[] getCarryingCapacities(AbundanceFad fad) {
        return carryingCapacitiesPerSpecies;
    }

    /**
     * get the minimum amount of abundance there needs to be in a cell for this species without which the FAD won't
     * attract!
     *
     * @param fad
     */
    @Override
    public HashMap<Species,double[][]> getDailyAttractionThreshold(AbundanceFad fad) {
        return dailyAttractionThreshold;
    }
}
