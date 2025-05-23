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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.erotetic.EroteticAnswer;
import uk.ac.ox.oxfish.fisher.erotetic.EroteticChooser;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

import java.util.List;

/**
 * Filters sequentially looking for:
 * <ul>
 *     <li>Somewhere Safe</li>
 *     <li>Somewhere not known to fail to have acceptable profit in past.</li>
 *     <li>Somewhere legal for me.</li>
 *     <li>Somewhere socially appropriate</li>
 *     <li>Somewhere known to have acceptable profit in past.</li>
 *     <li>Somewhere randomly picked as a spot. </li>
 * </ul>
 * Created by carrknight on 5/26/16.
 */
public class SNALSARDestinationStrategy implements DestinationStrategy,
    TripListener {


    private static final long serialVersionUID = 7953893846167351953L;
    /**
     * the utility object where we put all the filters and sequentially goes through them.
     */
    private final EroteticChooser<SeaTile> chooser;

    /**
     * delegate doing the actual navigation. This destination strategy just updates it after every trip.
     */
    private final FavoriteDestinationStrategy delegate;

    private final FisherStartable startable;
    private FishState model;
    private Fisher fisher;


    public SNALSARDestinationStrategy(
        final EroteticAnswer<SeaTile> safetyFilter,
        final EroteticAnswer<SeaTile> notKnownToFailProfitFilter,
        final EroteticAnswer<SeaTile> legalFilter,
        final EroteticAnswer<SeaTile> sociallyAppropriateFilter,
        final EroteticAnswer<SeaTile> knownToHaveAcceptableProfits,
        final FavoriteDestinationStrategy delegate,
        final FisherStartable startable
    ) {
        this.chooser = new EroteticChooser<>();
        this.chooser.add(safetyFilter);
        this.chooser.add(notKnownToFailProfitFilter);
        this.chooser.add(legalFilter);
        this.chooser.add(sociallyAppropriateFilter);
        this.chooser.add(knownToHaveAcceptableProfits);
        this.delegate = delegate;
        this.startable = startable;
    }

    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
        final Fisher fisher, final MersenneTwisterFast random, final FishState model, final Action currentAction
    ) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        startable.start(model, fisher);
        this.model = model;
        this.fisher = fisher;
        fisher.addTripListener(this);
        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(final Fisher fisher) {
        delegate.turnOff(fisher);
    }

    @Override
    public void reactToFinishedTrip(final TripRecord record, final Fisher fisher) {
        //all choices
        final List<SeaTile> options = model.getMap().getAllSeaTilesExcludingLandAsList();
        delegate.setFavoriteSpot(chooser.answer(options,
                this.fisher.getTileRepresentation(),
                model, this.fisher
            )
        );
    }


    public SeaTile getFavoriteSpot() {
        return delegate.getFavoriteSpot();
    }
}
