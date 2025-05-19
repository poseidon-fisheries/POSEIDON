/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedHashMap;

public class MixedExogenousCatches extends AbstractYearlyTargetExogenousCatches {
    private static final long serialVersionUID = 2386335733872357021L;

    public MixedExogenousCatches(
        final LinkedHashMap<Species, Double> exogenousYearlyCatchesInKg
    ) {
        super(exogenousYearlyCatchesInKg, "Exogenous catches of ");
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
        final FishState simState, final Species target, final LocalBiology tile, final double step
    ) {

        if (target.getNumberOfBins() > 1)
            return AbundanceDrivenFixedExogenousCatches.abundanceSimpleMortalityEvent(simState,
                target,
                tile,
                step, false
            );
        else
            return BiomassDrivenFixedExogenousCatches.biomassSimpleMortalityEvent(
                simState,
                target,
                tile,
                step
            );

    }
}
