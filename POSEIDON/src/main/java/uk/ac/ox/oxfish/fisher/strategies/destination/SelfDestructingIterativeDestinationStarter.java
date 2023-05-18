/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * this is a weird one.
 * Basically we want to have the agents perform iteratively, but we don't want to waste hours training them to where an initial
 * good spot is. This object, when requested, will force the agent to look at a random set of sea tiles, compute their profits (with perfect knowledge)
 * and just return the best one. It should also self-destruct (basically making its delegate the real destination strategy)
 */
public class SelfDestructingIterativeDestinationStarter implements DestinationStrategy {


    private final PerTripIterativeDestinationStrategy delegate;

    private final double maxHoursOut;

    private final double fractionOfTilesToExamine;
    boolean turningOff = false;

    public SelfDestructingIterativeDestinationStarter(
        PerTripIterativeDestinationStrategy delegate,
        double maxHoursOut,
        double fractionOfTilesToExamine
    ) {
        this.delegate = delegate;
        this.maxHoursOut = maxHoursOut;
        this.fractionOfTilesToExamine = fractionOfTilesToExamine;
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
        Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction
    ) {

        Preconditions.checkState(
            false,
            "Should have self-destructed by now!"
        );
        return null;
    }

    @Override
    public void start(FishState model, Fisher fisher) {

        //this gets called when I set it as a delegate
        //    delegate.start(model,fisher);

        SeaTile bestTile = null;
        double bestProfits = -Double.MAX_VALUE;

        List<SeaTile> candidates = model.getMap().getAllSeaTilesExcludingLandAsList();
        do {


            for (SeaTile candidate : candidates) {

                //simulator
                ProfitFunction simulator = new ProfitFunction(maxHoursOut);

                if (!model.getRandom().nextBoolean(fractionOfTilesToExamine))
                    continue;

                //simulate trip!
                Double profits = simulator.simulateHourlyProfits(
                    fisher,
                    fisher.getGear().expectedHourlyCatch(fisher, candidate, 1,
                        model.getBiology()
                    ),
                    candidate,
                    model,
                    false

                );
                if (profits == null || !Double.isFinite(profits))
                    continue;
                if (profits > bestProfits) {
                    bestProfits = profits;
                    bestTile = candidate;
                }

            }


        } while (bestTile == null);

        turningOff = true;
        delegate.forceFavoriteSpot(bestTile);
        Preconditions.checkArgument(
            fisher.getDestinationStrategy() == this,
            "we are not the destination strategy; we can't replace it!"
        );
        fisher.setDestinationStrategy(delegate);
        assert delegate.getFavoriteSpot() == bestTile;
        turningOff = false;


    }

    @Override
    public void turnOff(Fisher fisher) {

        if (!turningOff)
            delegate.turnOff(fisher);
    }
}
