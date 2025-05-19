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

package uk.ac.ox.oxfish.model.regs;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * This is a container for two regulations, one that is supposed to be "business as usual" and one that we trigger
 * whenever a specific indicator falls below a threshold.
 * We will return to the "business as usual" regulation only when the indicator is above a higher threshold
 */
public class TriggerRegulation implements Regulation, Steppable {


    private static final long serialVersionUID = -6086972340417316834L;
    private final double lowThreshold;

    private final double highThreshold;

    private final String indicatorName;

    private final Regulation businessAsUsual;

    private final Regulation emergency;

    private Regulation currentRegulation;


    public TriggerRegulation(
        final double lowThreshold, final double highThreshold, final String indicatorName,
        final Regulation businessAsUsual, final Regulation emergency
    ) {
        Preconditions.checkArgument(lowThreshold <= highThreshold);
        this.lowThreshold = lowThreshold;
        this.highThreshold = highThreshold;
        this.indicatorName = indicatorName;
        this.businessAsUsual = businessAsUsual;
        this.emergency = emergency;
        currentRegulation = businessAsUsual;

    }

    /**
     * checks each year whether to trigger the emergency regulation or whether to return to business as usual
     *
     * @param simState
     */
    @Override
    public void step(final SimState simState) {
        final FishState model = (FishState) simState;
        final double indicator = model.getLatestYearlyObservation(indicatorName);
        //if there is no observation, then NaN
        if (!Double.isFinite(indicator))
            return;

        if (currentRegulation == emergency) {
            if (indicator > highThreshold)
                currentRegulation = businessAsUsual;
        } else {
            if (indicator < lowThreshold)
                currentRegulation = emergency;
        }
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        businessAsUsual.start(model, fisher);
        emergency.start(model, fisher);


        model.scheduleEveryYear(
            this,
            StepOrder.AFTER_DATA
        );
    }

    @Override
    public void turnOff(final Fisher fisher) {
        businessAsUsual.turnOff(fisher);
        emergency.turnOff(fisher);
    }

    /**
     * can the agent fish at this location?
     *
     * @param agent the agent that wants to fish
     * @param tile  the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    @Override
    public boolean canFishHere(final Fisher agent, final SeaTile tile, final FishState model, final int timeStep) {
        return currentRegulation.canFishHere(agent, tile, model, timeStep);
    }

    /**
     * how much of this species biomass is sellable. Zero means it is unsellable
     *
     * @param agent   the fisher selling its catch
     * @param species the species we are being asked about
     * @param model   a link to the model
     * @return a positive biomass if it sellable. Zero if you need to throw everything away
     */
    @Override
    public double maximumBiomassSellable(final Fisher agent, final Species species, final FishState model, final int timeStep) {
        return currentRegulation.maximumBiomassSellable(agent, species, model, timeStep);
    }

    /**
     * Can this fisher be at sea?
     *
     * @param fisher the  fisher
     * @param model  the model
     * @return true if it can be out. When it's false the fisher can't leave port and ought to go back to port if he is
     * at sea
     */
    @Override
    public boolean allowedAtSea(final Fisher fisher, final FishState model, final int timeStep) {
        return currentRegulation.allowedAtSea(fisher, model, timeStep);
    }

    /**
     * tell the regulation object this much has been caught
     *
     * @param where             where the fishing occurred
     * @param who               who did the fishing
     * @param fishCaught        catch object
     * @param fishRetained
     * @param hoursSpentFishing how many hours were spent fishing
     */
    @Override
    public void reactToFishing(
        final SeaTile where,
        final Fisher who,
        final Catch fishCaught,
        final Catch fishRetained,
        final int hoursSpentFishing,
        final FishState model,
        final int timeStep
    ) {
        currentRegulation.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing, model, timeStep);
    }

    /**
     * tell the regulation object this much of this species has been sold
     *
     * @param species the species of fish sold
     * @param seller  agent selling the fish
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
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
        currentRegulation.reactToSale(species, seller, biomass, revenue, model, timeStep);
    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return currentRegulation.makeCopy();
    }

    /**
     * Getter for property 'lowThreshold'.
     *
     * @return Value for property 'lowThreshold'.
     */
    public double getLowThreshold() {
        return lowThreshold;
    }

    /**
     * Getter for property 'highThreshold'.
     *
     * @return Value for property 'highThreshold'.
     */
    public double getHighThreshold() {
        return highThreshold;
    }

    /**
     * Getter for property 'indicatorName'.
     *
     * @return Value for property 'indicatorName'.
     */
    public String getIndicatorName() {
        return indicatorName;
    }

    /**
     * Getter for property 'businessAsUsual'.
     *
     * @return Value for property 'businessAsUsual'.
     */
    public Regulation getBusinessAsUsual() {
        return businessAsUsual;
    }

    /**
     * Getter for property 'emergency'.
     *
     * @return Value for property 'emergency'.
     */
    public Regulation getEmergency() {
        return emergency;
    }
}
