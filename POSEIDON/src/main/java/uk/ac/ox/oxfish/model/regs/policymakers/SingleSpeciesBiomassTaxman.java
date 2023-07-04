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

package uk.ac.ox.oxfish.model.regs.policymakers;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;

/**
 * First policymaker. In this case all it does is set a tax when the threshold is broken.
 * <p>
 * There are a lot of problems with this implementation, mostly due to the fact that the way markets are coded is ugly as sin.
 * One day we might work more on that.
 * Created by carrknight on 9/30/16.
 */
public class SingleSpeciesBiomassTaxman implements Steppable, Startable {


    private static final long serialVersionUID = 4315611477875895714L;
    final private Species species;

    /**
     * tax to impose
     */
    final private double taxToImpose;

    /**
     * if biomass passes this value, impose a tax
     */
    final private double biomassThreshold;

    /**
     * if this is true, tax is imposed when biomass<threshold otherwise it is imposed
     * when biomass > threshold
     */
    final private boolean imposeTaxWhenBiomassBelowThreshold;


    private boolean taxImposed = false;
    private Stoppable stoppable;

    public SingleSpeciesBiomassTaxman(
        final Species species, final double taxToImpose, final double biomassThreshold, final boolean imposeTaxWhenBiomassBelowThreshold
    ) {
        this.species = species;
        this.taxToImpose = taxToImpose;
        this.biomassThreshold = biomassThreshold;
        this.imposeTaxWhenBiomassBelowThreshold = imposeTaxWhenBiomassBelowThreshold;
    }


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        stoppable = model.scheduleEveryYear(this, StepOrder.POLICY_UPDATE);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if (stoppable != null)
            stoppable.stop();
    }

    @Override
    public void step(final SimState simState) {


        final FishState model = (FishState) simState;

        final double biomass = model.getTotalBiomass(species);

        //if you need to impose a tax and it is currently not active, then activate it!
        if (taxShouldBeActive(biomass) && !taxImposed) {
            taxImposed = true;
            for (final Port port : model.getPorts()) {
                final FixedPriceMarket market = (FixedPriceMarket) port.getDefaultMarketMap().getMarket(species);
                market.setPrice(market.getPrice() - taxToImpose);
            }

        }


        //if tax should not be activated but it is, take it off
        if (!taxShouldBeActive(biomass) && taxImposed) {
            taxImposed = false;
            for (final Port port : model.getPorts()) {
                final FixedPriceMarket market = (FixedPriceMarket) port.getDefaultMarketMap().getMarket(species);
                market.setPrice(market.getPrice() + taxToImpose);
            }

        }

    }


    private boolean taxShouldBeActive(final double biomass) {
        return (biomass < biomassThreshold && imposeTaxWhenBiomassBelowThreshold) ||
            (biomass > biomassThreshold && !imposeTaxWhenBiomassBelowThreshold);
    }
}
