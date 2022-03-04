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
import org.jetbrains.annotations.Nullable;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;
import java.util.Map;

/**
 * Inspired somewhat by the IOTC paper: a FAD that "activates" after a certain number of days and then attracts at a fixed rate until full.
 * Whether anything gets attracted depends on there being enough local biomass (for example 100 times more abundance than what would be attracted in a day).
 *
 * This for now uses a form of relatively questionable selectivity that updates daily to be correct in a "global" sense.
 */
public class AbundanceLinearIntervalAttractor implements FishAttractor<AbundanceLocalBiology, AbundanceFad>, Steppable {


    private final int daysInWaterBeforeAttraction;

    private final int daysItTakesToFillUp;

    final double[] carryingCapacitiesPerSpecies;

    final double[] dailyBiomassAttractedPerSpecies;

    WeightedObject<AbundanceLocalBiology> dailyAttractionStep; //stored as a weighted object for speed

    HashMap<Species,double[][]>  dailyAttractionThreshold; //this will just be dailyAttractionStep times minAbundanceThreshold; but easy t store and skip all computations

    final double minAbundanceThreshold;

    final Map<Species,NonMutatingArrayFilter> globalSelectivityCurves;

    public AbundanceLinearIntervalAttractor(
            int daysInWaterBeforeAttraction, int daysItTakesToFillUp, double[] carryingCapacitiesPerSpecies,
            double minAbundanceThreshold,
            Map<Species, NonMutatingArrayFilter> selectivityFilters,
            FishState model
            ) {
        Preconditions.checkArgument(minAbundanceThreshold>=1);
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
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


        this.minAbundanceThreshold = minAbundanceThreshold;

        model.scheduleEveryDay(this, StepOrder.DAWN);
    }


    @Override
    public void step(SimState simState) {
        HashMap<Species,double[][]> dailyAbundance = new HashMap<>();
        dailyAttractionThreshold = new HashMap<>();
        //update your daily attraction given selectivity curves
        for (Map.Entry<Species, NonMutatingArrayFilter> speciesSelectivity : globalSelectivityCurves.entrySet()) {
            Species species = speciesSelectivity.getKey();
            double[][] oceanAbundance = ((FishState) simState).getTotalAbundance(species);
            double[][] selectedAbundance = speciesSelectivity.getValue().filter(species, oceanAbundance);
            //here we store the WEIGHT of the fish that would be selected had we applied the selectivity curve to the whole ocean
            //ignoring (i.e. setting to 1) catchability
            double[][] selectedWeight = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
            double totalSelectedWeight = 0;
            //weigh the abudance of the filtered matrix for all bins
            for (int sub = 0; sub < selectedAbundance.length; sub++) {
                for (int bin = 0; bin < selectedAbundance[0].length; bin++) {
                    selectedWeight[sub][bin] = selectedAbundance[sub][bin] * species.getWeight(sub,bin);
                    totalSelectedWeight += selectedWeight[sub][bin];
                }
            }
            //now given the weight per bin and total weight, find how many we need to take for each bin to perform a daily step
            double[][] dailyStep = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
            double[][] dailyThreshold =  new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
            for (int sub = 0; sub < selectedAbundance.length; sub++) {
                for (int bin = 0; bin < selectedAbundance[0].length; bin++) {
                    //weight that should come from this bin
                    dailyStep[sub][bin] =
                            (dailyBiomassAttractedPerSpecies[species.getIndex()] *  selectedWeight[sub][bin]/totalSelectedWeight)
                                    //and now turn it all into abundance:
                                    / species.getWeight(sub,bin);

                    dailyThreshold[sub][bin] = dailyThreshold[sub][bin] * minAbundanceThreshold;

                }
            }
            dailyAttractionThreshold.put(species,dailyThreshold);
            dailyAbundance.put(species,dailyStep);


        }
        AbundanceLocalBiology toReturn = new AbundanceLocalBiology(dailyAbundance);
        dailyAttractionStep = new WeightedObject<>(toReturn,toReturn.getTotalBiomass());


    }

    @Nullable
    @Override
    public WeightedObject<AbundanceLocalBiology> attractImplementation(
            AbundanceLocalBiology seaTileBiology, AbundanceFad fad) {

        //attract nothing before spending enough steps in
        if(fad.getStepDeployed()<daysInWaterBeforeAttraction)
                return null;
        //start weighing stuff
        //don't bother attracting if full
        double[] currentFadBiomass = fad.getBiology().getCurrentBiomass();
        if(currentFadBiomass[0]>=carryingCapacitiesPerSpecies[0]){
            //by construction it should fill up the same way across all species
            //length >0 IMPLIES second species is also full ===== length==0 OR second species full
            assert currentFadBiomass.length==1 || currentFadBiomass[1] >= carryingCapacitiesPerSpecies[1];
            return null;
        }
        //don't bother attracting if there is less biomass than what is needed to attract in a single day
        //don't bother attracting if any abundance bin is below threshold
        for (Map.Entry<Species, StructuredAbundance> speciesAbundance : seaTileBiology.getStructuredAbundance().entrySet()) {
            double[][] abundanceInTile = speciesAbundance.getValue().asMatrix();
            double[][] threshold = dailyAttractionThreshold.get(speciesAbundance.getKey());
            for (int subdivision = 0; subdivision < threshold.length; subdivision++) {
                for (int bin = 0; bin < threshold[subdivision].length; bin++) {
                    if(threshold[subdivision][bin]> abundanceInTile[subdivision][bin])
                        return null;
                }
            }
        }
        //attract
        return dailyAttractionStep;
    }

    public int getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
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
}
