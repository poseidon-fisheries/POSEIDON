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
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.HeterogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.LinkedHashMap;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Basically you are given a number of fish to kill each year and you do that
 * on the "abundance" side of catches
 * Created by carrknight on 3/23/17.
 */
public class AbundanceDrivenFixedExogenousCatches extends AbstractYearlyTargetExogenousCatches {


    private static final long serialVersionUID = -3491975654187985622L;

    public AbundanceDrivenFixedExogenousCatches(
        final LinkedHashMap<Species, Double> exogenousYearlyCatchesInKg
    ) {
        super(exogenousYearlyCatchesInKg, "Exogenous catches of ");
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
    protected Catch mortalityEvent(
        final FishState model,
        final Species target,
        final LocalBiology tile,
        final double step
    ) {
        return abundanceSimpleMortalityEvent(model, target, tile, step, true);
    }

    /**
     * simulate exogenous catch
     *
     * @param model    the model
     * @param target   species to kill
     * @param tile     where to kill it
     * @param step     how much at most to kill
     * @param rounding
     * @return
     */
    public static Catch abundanceSimpleMortalityEvent(
        final FishState model, final Species target, final LocalBiology tile, final double step, final boolean rounding
    ) {
        //take it as a fixed proportion catchability (and never more than it is available anyway)
        assert tile.getBiomass(target) > FishStateUtilities.EPSILON;
        final double proportionToCatch = Math.min(1, step / tile.getBiomass(target));
        //simulate the catches as a fixed proportion gear
        final HomogeneousAbundanceGear simulatedGear = new HomogeneousAbundanceGear(
            0,
            new FixedProportionFilter(
                proportionToCatch, rounding)
        );
        //hide it in an heterogeneous abundance gear so that only one species at a time gets aught!
        final HeterogeneousAbundanceGear gear = new HeterogeneousAbundanceGear(
            entry(target, simulatedGear)
        );
        //catch it
        final Catch fish = gear.fish(null, tile, null, 1, model.getBiology());
        tile.reactToThisAmountOfBiomassBeingFished(fish, fish, model.getBiology());
        return fish;
    }
}
