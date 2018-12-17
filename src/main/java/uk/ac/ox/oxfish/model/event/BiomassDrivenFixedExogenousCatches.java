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

package uk.ac.ox.oxfish.model.event;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.OneSpecieGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.LinkedHashMap;

/**
 * Created by carrknight on 5/25/17.
 */
public class BiomassDrivenFixedExogenousCatches extends AbstractExogenousCatches {
    public BiomassDrivenFixedExogenousCatches(
            LinkedHashMap<Species, Double> exogenousYearlyCatchesInKg) {
        super(exogenousYearlyCatchesInKg, "Exogenous catches of ");
    }

    /**
     * simulate exogenous catch
     *
     * @param model the model
     * @param target   species to kill
     * @param tile     where to kill it
     * @param step     how much at most to kill
     * @return
     */
    @Override
    protected Catch mortalityEvent(
            FishState model, Species target, SeaTile tile, double step) {
        return biomassSimpleMortalityEvent(model,
                                           target,
                                           tile,
                                           step);
    }


    public static Catch biomassSimpleMortalityEvent(
            FishState model, Species target, SeaTile tile, double step) {
        //take it as a fixed proportion catchability (and never more than it is available anyway)
        assert tile.getBiomass(target) > FishStateUtilities.EPSILON;
        double proportionToCatch = Math.min(1,step/tile.getBiomass(target));
        //simulate the catches as a fixed proportion gear
        OneSpecieGear gear = new OneSpecieGear(target,proportionToCatch);
        //catch it
        Catch fish = gear.fish(null, tile,tile , 1, model.getBiology());
        //round to be supersafe
        if(fish.totalCatchWeight()>tile.getBiomass(target)) {
            //should be by VERY little!
            assert tile.getBiomass(target) + FishStateUtilities.EPSILON > fish.getTotalWeight();
            assert proportionToCatch >=1.0;
            //bound it to what is available
            fish = new Catch(target,tile.getBiomass(target),model.getBiology());
            assert (fish.totalCatchWeight()<=tile.getBiomass(target));
        }
        assert (fish.totalCatchWeight()<=tile.getBiomass(target));
        tile.reactToThisAmountOfBiomassBeingFished(fish,fish,model.getBiology());
        return fish;
    }
}
