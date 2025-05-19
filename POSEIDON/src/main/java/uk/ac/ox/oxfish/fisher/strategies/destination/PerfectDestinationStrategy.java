/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class PerfectDestinationStrategy implements DestinationStrategy, TripListener {

    private static final long serialVersionUID = -6575548648177199503L;
    private final double maxHoursOut;

    private final FavoriteDestinationStrategy delegate;
    private FishState model;
    private Fisher fisher;

    public PerfectDestinationStrategy(
        final double maxHoursOut,
        final NauticalMap map,
        final MersenneTwisterFast random
    ) {
        this.maxHoursOut = maxHoursOut;
        delegate = new FavoriteDestinationStrategy(map, random);
    }

    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination
     * @return the destination
     */
    @Override
    public SeaTile chooseDestination(
        final Fisher fisher, final MersenneTwisterFast random, final FishState model, final Action currentAction
    ) {

        return delegate.chooseDestination(fisher, random, model, currentAction);

    }

    @Override
    public void start(final FishState model, final Fisher fisher) {

        delegate.setFavoriteSpot(pickBest(fisher, model));

        this.model = model;
        this.fisher = fisher;
        fisher.addTripListener(this);
    }

    private SeaTile pickBest(final Fisher fisher, final FishState model) {
        final ProfitFunction simulator = new ProfitFunction(
            new LameTripSimulator(),
            maxHoursOut
        );

        SeaTile best = null;
        double bestProfits = Double.NEGATIVE_INFINITY;
        for (final SeaTile seaTile : model.getMap().getAllSeaTilesExcludingLandAsList()) {
            if (seaTile.isFishingEvenPossibleHere() &&
                fisher.isAllowedToFishHere(seaTile, model)) {
                final double profitsHere = simulator.hourlyProfitFromHypotheticalTripHere(
                    fisher, seaTile, model, fisher.getGear().expectedHourlyCatch(fisher, seaTile,
                        1,
                        model.getBiology()
                    ),
                    false
                );
                if (profitsHere > bestProfits) {
                    bestProfits = profitsHere;
                    best = seaTile;
                }
            }
        }

        return best;
    }

    @Override
    public void turnOff(final Fisher fisher) {
        fisher.removeTripListener(this);
    }

    @Override
    public void reactToFinishedTrip(final TripRecord record, final Fisher fisher) {
        final SeaTile favoriteSpot = pickBest(this.fisher, model);
        if (favoriteSpot != null) //not going to make a selection when you are not allowed out!
            delegate.setFavoriteSpot(favoriteSpot);
    }
}
