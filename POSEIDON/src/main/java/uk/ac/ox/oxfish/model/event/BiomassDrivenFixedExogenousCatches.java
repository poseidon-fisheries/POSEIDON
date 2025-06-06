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

package uk.ac.ox.oxfish.model.event;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.tuna.Extractor;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.OneSpecieGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by carrknight on 5/25/17.
 */
public class BiomassDrivenFixedExogenousCatches extends AbstractYearlyTargetExogenousCatches {


    private static final long serialVersionUID = 2876862531037871169L;
    private final boolean allowMortalityOnFads;


    public BiomassDrivenFixedExogenousCatches(
        final LinkedHashMap<Species, Double> exogenousYearlyCatchesInKg, final boolean allowMortalityOnFads
    ) {
        super(exogenousYearlyCatchesInKg, "Exogenous catches of ");
        this.allowMortalityOnFads = allowMortalityOnFads;
    }


    @Override
    protected List<? extends LocalBiology> getAllCatchableBiologies(final FishState model) {
        return new Extractor<>(LocalBiology.class, allowMortalityOnFads, true)
            .apply(model);
    }

    /**
     * simulate exogenous catch
     *
     * @param model  the model
     * @param target species to kill
     * @param tile   where to kill it
     * @param step   how much at most to kill
     * @return
     */
    @Override
    protected Catch mortalityEvent(
        final FishState model, final Species target, final LocalBiology tile, final double step
    ) {
        return biomassSimpleMortalityEvent(
            model,
            target,
            tile,
            step
        );
    }


    public static Catch biomassSimpleMortalityEvent(
        final FishState model, final Species target, final LocalBiology tile, final double step
    ) {
        //take it as a fixed proportion catchability (and never more than it is available anyway)
        assert tile.getBiomass(target) > FishStateUtilities.EPSILON;
        final double proportionToCatch = Math.min(1, step / tile.getBiomass(target));
        //simulate the catches as a fixed proportion gear
        final OneSpecieGear gear = new OneSpecieGear(target, proportionToCatch);
        //catch it
        Catch fish = gear.fish(null, tile, null, 1, model.getBiology());
        //round to be supersafe
        if (fish.totalCatchWeight() > tile.getBiomass(target)) {
            //should be by VERY little!
            assert tile.getBiomass(target) + FishStateUtilities.EPSILON > fish.getTotalWeight();
            assert proportionToCatch >= 1.0;
            //bound it to what is available
            fish = new Catch(target, tile.getBiomass(target), model.getBiology());
            assert (fish.totalCatchWeight() <= tile.getBiomass(target));
        }
        assert (fish.totalCatchWeight() <= tile.getBiomass(target));
        tile.reactToThisAmountOfBiomassBeingFished(fish, fish, model.getBiology());
        return fish;
    }
}
