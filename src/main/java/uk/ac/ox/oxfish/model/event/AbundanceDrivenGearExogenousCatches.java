/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.event;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class AbundanceDrivenGearExogenousCatches extends AbstractExogenousCatches {

    private final HashMap<Species, HomogeneousAbundanceGear> gears;


    public AbundanceDrivenGearExogenousCatches(
            LinkedHashMap<Species, Double> exogenousYearlyCatchesInKg,
            HashMap<Species, HomogeneousAbundanceGear> gears) {
        super(exogenousYearlyCatchesInKg, "Exogenous landings of ");
        this.gears = gears;
    }


    /**
     * simulate exogenous catch (must call the react to catch function within this)
     *
     * @param simState the model
     * @param target   species to kill
     * @param tile     where to kill it
     * @param step     how much at most to kill
     * @return
     */
    @Override
    protected Catch mortalityEvent(
            FishState simState, Species target, SeaTile tile, double step) {

        HomogeneousAbundanceGear gear = gears.get(target);
        Preconditions.checkArgument(gear!=null, "Exogenous catches impossible without providing gear");


        StructuredAbundance[] structuredAbundances = new StructuredAbundance[simState.getBiology().getSize()];
        for(int i=0; i<structuredAbundances.length; i++)
            structuredAbundances[i] = new StructuredAbundance(simState.getBiology().getSpecie(i).getNumberOfSubdivisions(),
                                                              simState.getBiology().getSpecie(i).getNumberOfBins());
        structuredAbundances[target.getIndex()] = gear.catchesAsAbundanceForThisSpecies(tile, 1, target);
        Catch fish = new Catch(structuredAbundances,
                                 simState.getBiology());
        double totalWeight = fish.getTotalWeight();
        //adjust for excess
        if(totalWeight >step)
            for (double[] row : fish.getAbundance(target.getIndex()).asMatrix()) {
                for (int i=0; i<row.length; i++) {
                    row[i]=row[i]*step/totalWeight;
                }
            }
        tile.reactToThisAmountOfBiomassBeingFished(fish,fish,simState.getBiology());
        return fish;


    }
}
