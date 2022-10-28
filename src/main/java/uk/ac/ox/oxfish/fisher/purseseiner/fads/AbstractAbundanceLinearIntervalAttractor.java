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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Inspired somewhat by the IOTC paper: a FAD that "activates" after a certain number of days and then attracts at a fixed rate until full.
 * Whether anything gets attracted depends on there being enough local biomass (for example 100 times more abundance than what would be attracted in a day).
 *
 * This for now uses a form of relatively questionable selectivity that updates daily to be correct in a "global" sense:
 * fundamentally all this abstract class does is each time step it looks
 * at the global population and derive what the selectivity curve implies we should catch for each 1kg of landings in terms
 * of # of fish per bin.
 */
public abstract class  AbstractAbundanceLinearIntervalAttractor implements FishAttractor<AbundanceLocalBiology,AbundanceFad>,
        Steppable {

    protected final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves;

    /**
     * each step we compute how many # of fish we need to catch for each age bin to fulfill 1kg of landings (per species);
     * this can then be retrieved for each fad.
     * Basically it is a selectivity-driven transformer from kg caught to abundance caught
     */
    private HashMap<Species,double[][]> abundancePerDailyKgLanded;


    protected final FishState model;

    private final int daysInWaterBeforeAttraction;

    /**
     * if this predicate is not true, there will be no further attraction this day
     */
    private Predicate<SeaTile> additionalAttractionHurdle = seaTile -> true;


    public AbstractAbundanceLinearIntervalAttractor(
            Map<Species, NonMutatingArrayFilter> globalSelectivityCurves, FishState model,
            int daysInWaterBeforeAttraction) {
        this.globalSelectivityCurves = globalSelectivityCurves;
        Preconditions.checkArgument(daysInWaterBeforeAttraction>=0);
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        model.scheduleEveryDay(this, StepOrder.DAWN);
        this.model = model;
    }

    @Nullable
    @Override
    public WeightedObject<AbundanceLocalBiology> attractImplementation(
            AbundanceLocalBiology seaTileBiology, AbundanceFad fad) {

        if (shouldICancelTheAttractionToday(seaTileBiology, fad))
            return null;

        //you passed all checks! attract
        return attractDaily(seaTileBiology,fad);
    }

    protected boolean shouldICancelTheAttractionToday(AbundanceLocalBiology seaTileBiology, AbundanceFad fad) {
        if(!additionalAttractionHurdle.test(fad.getLocation()))
            return true;

        //attract nothing before spending enough steps in
        if(model.getDay()- fad.getStepDeployed()<daysInWaterBeforeAttraction || !fad.isActive())
            return true;
        //start weighing stuff
        //don't bother attracting if full
        double[] currentFadBiomass = fad.getBiology().getCurrentBiomass();
        double[] carryingCapacitiesPerSpecies = getCarryingCapacities(fad);
        for (int i = 0; i < carryingCapacitiesPerSpecies.length; i++) {
            if(carryingCapacitiesPerSpecies[i] == 0) //todo test that they fill up and then move no further!
                continue;
            //if one is full, they are all full
            if(currentFadBiomass[i]>=carryingCapacitiesPerSpecies[i])
                return true;

        }
        //don't bother attracting if there is less abundance than the threshold
        //don't bother attracting if any abundance bin is below threshold
        HashMap<Species,double[][]> thresholds = getDailyAttractionThreshold(fad);
        Preconditions.checkState(thresholds!=null);
        Preconditions.checkState(!thresholds.isEmpty());
        for (Map.Entry<Species, StructuredAbundance> speciesAbundance : seaTileBiology.getStructuredAbundance().entrySet()) {
            double[][] abundanceInTile = speciesAbundance.getValue().asMatrix();
            double[][] threshold = thresholds.get(speciesAbundance.getKey());
            for (int subdivision = 0; subdivision < threshold.length; subdivision++) {
                for (int bin = 0; bin < threshold[subdivision].length; bin++) {
                    if(threshold[subdivision][bin]> abundanceInTile[subdivision][bin])
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * function that returns how much this fad will attract today in abundance given that all preliminary checks have passed
     * @param seaTileBiology seatile underneath
     * @param fad fad object
     * @return
     */
    protected abstract WeightedObject<AbundanceLocalBiology> attractDaily(AbundanceLocalBiology seaTileBiology, AbundanceFad fad);


    @Override
    public void step(SimState simState) {
        abundancePerDailyKgLanded = turnSelectivityIntoBiomassToAbundanceConverter(
                ((FishState) simState),globalSelectivityCurves
        );


    }






    public static HashMap<Species,double[][]> turnSelectivityIntoBiomassToAbundanceConverter(
            FishState state, Map<Species, NonMutatingArrayFilter> globalSelectivityCurves
    ){
        HashMap<Species,double[][]> abundancePerDailyKgLanded = new HashMap<>();
        //   dailyAttractionThreshold = new HashMap<>();
        //update your daily attraction given selectivity curves
        for (Map.Entry<Species, NonMutatingArrayFilter> speciesSelectivity : globalSelectivityCurves.entrySet()) {
            Species species = speciesSelectivity.getKey();
            double[][] oceanAbundance = state.getTotalAbundance(species);
            double[][] selectedAbundance = speciesSelectivity.getValue().filter(species, oceanAbundance);
            //here we store the WEIGHT of the fish that would be selected had we applied the selectivity curve to the whole ocean
            //ignoring (i.e. setting to 1) catchability
            double[][] selectedWeight = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
            double totalSelectedWeight = 0;
            //weigh the abundance of the filtered matrix for all bins
            for (int sub = 0; sub < selectedAbundance.length; sub++) {
                for (int bin = 0; bin < selectedAbundance[0].length; bin++) {
                    selectedWeight[sub][bin] = selectedAbundance[sub][bin] * species.getWeight(sub, bin);
                    totalSelectedWeight += selectedWeight[sub][bin];
                }
            }
            //now given the weight per bin and total weight, find how many we need to take for each bin to perform a daily step
            double[][] dailyStep = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
            for (int sub = 0; sub < selectedAbundance.length; sub++) {
                for (int bin = 0; bin < selectedAbundance[0].length; bin++) {
                    //weight that should come from this bin
                    dailyStep[sub][bin] =
                            (selectedWeight[sub][bin] / totalSelectedWeight)
                                    //and now turn it all into abundance:
                                    / species.getWeight(sub, bin);


                    dailyStep[sub][bin] = Math.max(0,dailyStep[sub][bin]);
                }
            }
            abundancePerDailyKgLanded.put(species, dailyStep);


        }
        return abundancePerDailyKgLanded;

    }

    protected HashMap<Species, double[][]> getAbundancePerDailyKgLanded() {
        return abundancePerDailyKgLanded;
    }


    /**
     * get carrying capacities per fad in terms of KG per species
     * @param fad
     * @return
     */
    public abstract double[] getCarryingCapacities(AbundanceFad fad);

    /**
     * get the minimum amount of abundance there needs to be in a cell for this species without which the FAD won't attract!
     */
    public abstract HashMap<Species,double[][]> getDailyAttractionThreshold(AbundanceFad fad);

    public Predicate<SeaTile> getAdditionalAttractionHurdle() {
        return additionalAttractionHurdle;
    }

    public void setAdditionalAttractionHurdle(
            Predicate<SeaTile> additionalAttractionHurdle) {
        this.additionalAttractionHurdle = additionalAttractionHurdle;
    }
}
