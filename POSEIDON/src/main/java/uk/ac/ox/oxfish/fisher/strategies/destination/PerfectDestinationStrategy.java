/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.Nullable;
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

    private final double maxHoursOut;

    private final FavoriteDestinationStrategy delegate;


    public PerfectDestinationStrategy(double maxHoursOut,
                                      NauticalMap map,
                                      MersenneTwisterFast random) {
        this.maxHoursOut = maxHoursOut;
        delegate = new FavoriteDestinationStrategy(map,random);
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
            Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {

        return delegate.chooseDestination(fisher, random, model, currentAction);

    }

    @Nullable
    private SeaTile pickBest(Fisher fisher, FishState model) {
        ProfitFunction simulator = new ProfitFunction(new LameTripSimulator(),
                                                      maxHoursOut);

        SeaTile best = null;
        double bestProfits = Double.NEGATIVE_INFINITY;
        for (SeaTile seaTile : model.getMap().getAllSeaTilesExcludingLandAsList()) {
            if(seaTile.isFishingEvenPossibleHere() &&
                    fisher.isAllowedToFishHere(seaTile,model))
            {
                double profitsHere = simulator.hourlyProfitFromHypotheticalTripHere(
                        fisher,seaTile,model,fisher.getGear().expectedHourlyCatch(fisher, seaTile,
                                                                                  1,
                                                                                  model.getBiology()),
                        false
                );
                if(profitsHere>bestProfits)
                {
                    bestProfits = profitsHere;
                    best=seaTile;
                }
            }
        }

        return best;
    }

    private FishState model;

    private Fisher fisher;

    @Override
    public void start(FishState model, Fisher fisher) {

        delegate.setFavoriteSpot(pickBest(fisher,model));

        this.model=model;
        this.fisher=fisher;
        fisher.addTripListener(this);
    }

    @Override
    public void turnOff(Fisher fisher) {
        fisher.removeTripListener(this);
    }

    @Override
    public void reactToFinishedTrip(TripRecord record, Fisher fisher) {
        SeaTile favoriteSpot = pickBest(this.fisher, model);
        if(favoriteSpot!=null) //not going to make a selection when you are not allowed out!
            delegate.setFavoriteSpot(favoriteSpot);
    }
}
