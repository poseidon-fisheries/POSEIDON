/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * There are a few days a year where the agent is simply not allowed to go out.
 * Otherwise follow delegate
 */
public class ArbitraryPause implements Regulation {

    private final int startDay;

    private final int endDay;

    private final Regulation delegate;


    public ArbitraryPause(int startDay, int endDay, Regulation delegate) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.delegate = delegate;
    }

    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) {
        return delegate.canFishHere(agent, tile, model, timeStep);
    }

    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model, int timeStep) {
        return delegate.maximumBiomassSellable(agent, species, model, timeStep);
    }

    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) {
        if (fisher.isAtPortAndDocked()) {
            int dayOfTheYear = model.getDayOfTheYear(timeStep);
            if (dayOfTheYear >= startDay && dayOfTheYear <= endDay)
                return false;
        }
        return delegate.allowedAtSea(fisher, model, timeStep);

    }

    @Override
    public void reactToFishing(
        SeaTile where,
        Fisher who,
        Catch fishCaught,
        Catch fishRetained,
        int hoursSpentFishing,
        FishState model,
        int timeStep
    ) {
        delegate.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing, model, timeStep);
    }

    @Override
    public void reactToSale(
        Species species,
        Fisher seller,
        double biomass,
        double revenue,
        FishState model,
        int timeStep
    ) {
        delegate.reactToSale(species, seller, biomass, revenue, model, timeStep);
    }

    @Override
    public Regulation makeCopy() {
        return delegate.makeCopy();
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }
}
