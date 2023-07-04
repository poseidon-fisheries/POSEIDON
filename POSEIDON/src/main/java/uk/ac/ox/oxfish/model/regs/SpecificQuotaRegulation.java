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

package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Quota for a single specie. If the specie sold is not the right one, then ignore
 * Created by carrknight on 9/22/15.
 */
public class SpecificQuotaRegulation extends MonoQuotaRegulation {


    private static final long serialVersionUID = 7276006184488363070L;
    /**
     * specie to protect by quota
     */
    private final Species protectedSpecies;

    /**
     * when created it sets itself to step every year to reset the quota
     *
     * @param yearlyQuota the yearly quota
     * @param state       the model link to schedule on
     */
    public SpecificQuotaRegulation(final double yearlyQuota, final FishState state, final Species species) {
        super(yearlyQuota);
        this.protectedSpecies = species;
    }


    /**
     * Can this fisher be at sea?
     */
    @Override
    public boolean allowedAtSea(final Fisher fisher, final FishState model) {
        return true;
    }


    /**
     * You are allowed to fish, just never to sell the protected quota if you are wrong
     */
    @Override
    public boolean canFishHere(final Fisher agent, final SeaTile tile, final FishState model) {
        return true;
    }

    /**
     * ignore if wrong species.
     */
    @Override
    public void reactToSale(
        final Species species,
        final Fisher seller,
        final double biomass,
        final double revenue,
        final FishState model,
        final int timeStep
    ) {
        if (species == protectedSpecies)
            super.reactToSale(species, seller, biomass, revenue, model, timeStep);
    }


    public Species getProtectedSpecies() {
        return protectedSpecies;
    }
}
