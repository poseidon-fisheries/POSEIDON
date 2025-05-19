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

package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * Any market that does not care about size of the fish
 * but only its weight
 * Created by carrknight on 7/4/17.
 */
public abstract class AbstractBiomassMarket extends AbstractMarket {


    @Override
    protected TradeInfo sellFishImplementation(
        Hold hold, Fisher fisher, Regulation regulation, FishState state, Species species
    ) {
        return sellFishImplementation(
            hold.getWeightOfCatchInHold(species),
            fisher,
            regulation,
            state,
            species
        );
    }

    /**
     * the only method to implement for subclasses. Needs to actually do the trading and return the result
     *
     * @param biomass    the biomass caught by the seller
     * @param fisher     the seller
     * @param regulation the rules the seller abides to
     * @param state      the model
     * @return TradeInfo  results
     */
    protected abstract TradeInfo sellFishImplementation(
        double biomass, Fisher fisher,
        Regulation regulation, FishState state,
        Species species
    );


}
