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

import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.PluggableSelectivity;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * simple "linear" attractor for abundance fads: as long as it is old enough (but not too old) the fad attracts a fixed
 * proportion (catchability) of the vulnerable population (i.e. the population that can be selected in a cell).
 * Each FAD also have a carrying capacity so that they cannot get any more full than a given amount
 */
public class CatchabilitySelectivityFishAttractor implements FishAttractor<AbundanceLocalBiology,AbundanceFad>, FadRemovalListener{


    /**
     * generates carrying capacities; these are "weak" bounds as in the fad stops filling up when it passes over them
     * but if the step is so large that there is more fish than carrying capacity available, the fish stays on the fad
     */
    private final DoubleParameter[] carryingCapacitiesGenerator;

    private final double[] catchabilityPerSpecies;


    /**
     * as long as the FAD has been in the water less than this, it won't fill up
     */
    private final int daysInWaterBeforeAttraction;


    /**
     * if the fad has been attracting fish (potentially, anyway) for these many days, it stops attracting any more (but doesn't lose them, yet!)
     */
    private final int maximumAttractionDays;

    private final FishState model;

    private final HashMap<AbstractFad,double[]> carryingCapacityPerFad = new HashMap<>();

    private final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves;

    public CatchabilitySelectivityFishAttractor(
            DoubleParameter[] carryingCapacitiesGenerator,
            double[] catchabilityPerSpecies,
            int daysInWaterBeforeAttraction,
            int maximumAttractionDays,
            FishState model,
            Map<Species, NonMutatingArrayFilter> globalSelectivityCurves) {
        this.carryingCapacitiesGenerator = carryingCapacitiesGenerator;
        this.catchabilityPerSpecies = catchabilityPerSpecies;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.maximumAttractionDays = maximumAttractionDays;
        this.model = model;
        this.globalSelectivityCurves = globalSelectivityCurves;
    }

    @Nullable
    @Override
    public WeightedObject<AbundanceLocalBiology> attractImplementation(
            AbundanceLocalBiology seaTileBiology, AbundanceFad fad) {
        //if it's too early or late don't bother
        if(
                model.getDay()- fad.getStepDeployed()/model.getStepsPerDay()<daysInWaterBeforeAttraction ||
                        !fad.isActive() ||
                        model.getDay()- fad.getStepDeployed()/model.getStepsPerDay()>daysInWaterBeforeAttraction +  maximumAttractionDays
        )
            return null;
        SeaTile location = fad.getLocation();
        LocalBiology biology = location.getBiology();
        if(! (biology instanceof AbundanceLocalBiology))
            return null;

        Map<Species, double[][]> abundanceHere = ((AbundanceLocalBiology) biology).getAbundance();
        Map<Species, double[][]> caughtHere = new HashMap<>();
        //get the carrying capacities or generate them if they don't exist
        double[] carryingCapacityHere = getCarryingCapacities(fad);
        for (Species species : model.getSpecies()) {
            NonMutatingArrayFilter selectivity = globalSelectivityCurves.get(species);

            //set up catches
            double[][] abundanceInTile = abundanceHere.get(species);
            double[][] abundanceCaught = new double[abundanceInTile.length][abundanceInTile[0].length];
            caughtHere.put(species,abundanceCaught);

            //if you are full, ignore it!
            if(carryingCapacityHere[species.getIndex()]<=fad.getBiomass()[species.getIndex()])
                continue;

            //start filling them up!
            for (int subdivision = 0; subdivision < abundanceInTile.length; subdivision++) {
                for (int bin = 0; bin < abundanceInTile[subdivision].length; bin++) {
                    abundanceCaught[subdivision][bin] = Math.max(
                            catchabilityPerSpecies[species.getIndex()] *
                            selectivity.getFilterValue(subdivision,bin) *
                            abundanceInTile[subdivision][bin],
                    0);

                }
            }
        }
        AbundanceLocalBiology toReturn = new AbundanceLocalBiology(caughtHere);
        return new WeightedObject<>(toReturn,toReturn.getTotalBiomass());


    }




    @Override
    public void onFadRemoval(AbstractFad fad) {
        carryingCapacityPerFad.remove(fad);
    }

    public double[] getCarryingCapacities(AbundanceFad fad) {
        double[] toReturn = carryingCapacityPerFad.get(fad);
        if(toReturn==null)
        {
            toReturn = computeFadAttractions(fad,model);
            assert carryingCapacityPerFad.get(fad) == toReturn;
        }
        return toReturn;
    }

    private double[] computeFadAttractions(AbundanceFad fad,FishState model) {
        assert fad.getTotalCarryingCapacity() > 0;
        assert !carryingCapacityPerFad.containsKey(fad);

        //compute carrying capacity for fad
        double[] carryingCapacityHere = new double[carryingCapacitiesGenerator.length];
        for (int i = 0; i < carryingCapacitiesGenerator.length; i++) {
            carryingCapacityHere[i] = carryingCapacitiesGenerator[i].apply(model.getRandom());
        }
        carryingCapacityPerFad.put(fad, carryingCapacityHere);
        return carryingCapacityHere;
    }



}
