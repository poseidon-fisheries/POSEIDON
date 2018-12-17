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
import uk.ac.ox.oxfish.fisher.equipment.gear.HeterogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.LinkedHashMap;

/**
 * Basically you are given a number of fish to kill each year and you do that
 * on the "abundance" side of catches
 * Created by carrknight on 3/23/17.
 */
public class AbundanceDrivenFixedExogenousCatches extends AbstractExogenousCatches {


    public AbundanceDrivenFixedExogenousCatches(
            LinkedHashMap<Species, Double> exogenousYearlyCatchesInKg) {
        super(exogenousYearlyCatchesInKg, "Exogenous catches of ");
    }

    /**
     * simulate exogenous catch
     * @param model the model
     * @param target species to kill
     * @param tile where to kill it
     * @param step how much at most to kill
     * @return
     */
    protected Catch mortalityEvent(FishState model, Species target, SeaTile tile, double step) {
       return abundanceSimpleMortalityEvent(model, target, tile, step, true);
    }

    /**
     * simulate exogenous catch
     * @param model the model
     * @param target species to kill
     * @param tile where to kill it
     * @param step how much at most to kill
     * @param rounding
     * @return
     */
    public static Catch abundanceSimpleMortalityEvent(
            FishState model, Species target, SeaTile tile, double step, final boolean rounding) {
        //take it as a fixed proportion catchability (and never more than it is available anyway)
        assert tile.getBiomass(target) > FishStateUtilities.EPSILON;
        double proportionToCatch = Math.min(1,step/tile.getBiomass(target));
        //simulate the catches as a fixed proportion gear
        HomogeneousAbundanceGear simulatedGear = new HomogeneousAbundanceGear(0,
                                                                              new FixedProportionFilter(
                                                                                      proportionToCatch, rounding));
        //hide it in an heterogeneous abundance gear so that only one species at a time gets aught!
        HeterogeneousAbundanceGear gear = new HeterogeneousAbundanceGear(
                new Pair<>(target, simulatedGear)
        );
        //catch it
        Catch fish = gear.fish(null, tile,tile , 1, model.getBiology());
        tile.reactToThisAmountOfBiomassBeingFished(fish,fish,model.getBiology());
        return fish;
    }
}
