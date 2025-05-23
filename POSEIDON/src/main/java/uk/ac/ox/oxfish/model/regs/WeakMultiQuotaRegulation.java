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

package uk.ac.ox.oxfish.model.regs;

import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * Like the original TAC, but you are allowed to go out as long as at least one quota is positive!
 * Created by carrknight on 5/3/17.
 */
public class WeakMultiQuotaRegulation extends MultiQuotaRegulation {

    private static final long serialVersionUID = 4776807752025664410L;
    /**
     * contains the last day where that species was still allowed to be fished
     */
    final public int[] lastSeasonDay;
    private Stoppable receipt;


    public WeakMultiQuotaRegulation(final double[] yearlyQuota, final FishState state) {
        super(yearlyQuota, state);
        lastSeasonDay = new int[state.getSpecies().size()];
        Arrays.fill(lastSeasonDay, 365);

    }

    /**
     * burn through quotas; because of "maximum biomass sellable"  method, I expect here that the biomass
     * sold is less or equal to the quota available
     *
     * @param species the species of fish sold
     * @param seller  agent selling the fish
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    @Override
    public void reactToSale(
        final Species species, final Fisher seller, final double biomass, final double revenue, final FishState model, final int timeStep
    ) {
        final double before = super.getQuotaRemaining(species.getIndex());
        super.reactToSale(species, seller, biomass, revenue, model, timeStep);
        //if this was the landing that broke the quota, record the day
        final double after = super.getQuotaRemaining(species.getIndex());
        if (before > FishStateUtilities.EPSILON && after < FishStateUtilities.EPSILON)
            lastSeasonDay[species.getIndex()] = super.getState().getDayOfTheYear();

    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        super.start(model, fisher);
        receipt = model.scheduleEveryYear(
            (Steppable) simState -> Arrays.fill(lastSeasonDay, 365),
            StepOrder.DATA_RESET
        );


    }

    @Override
    public void turnOff(final Fisher fisher) {
        super.turnOff(fisher);
        if (receipt != null)
            receipt.stop();
    }

    @Override
    public boolean isFishingStillAllowed() {

        for (int i = 0; i < getNumberOfSpeciesTracked(); i++)
            if (getQuotaRemaining(i) > FishStateUtilities.EPSILON)
                return true;

        return false;


    }

    /**
     * Getter for property 'lastSeasonDay'.
     *
     * @return Value for property 'lastSeasonDay'.
     */
    public int[] getLastSeasonDay() {
        return lastSeasonDay;
    }
}
