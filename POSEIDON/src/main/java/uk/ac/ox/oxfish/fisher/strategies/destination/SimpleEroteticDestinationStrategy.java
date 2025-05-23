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

import java.util.List;

/**
 * Simple erotetic strategy: works first by given answer strategy and otherwise at random random
 * Created by carrknight on 4/11/16.
 */
public class SimpleEroteticDestinationStrategy implements DestinationStrategy,
    TripListener {

    private static final long serialVersionUID = 6172875513192915377L;
    private final EroteticChooser<SeaTile> chooser = new EroteticChooser<>();

    /**
     * this strategy works by modifying the "favorite" destination of its delegate
     */
    private final FavoriteDestinationStrategy delegate;

    /**
     * grabbed at start(.)
     */
    private Fisher fisher;

    /**
     * grabbed at start(.)
     */
    private FishState model;

    /**
     * the work is done almost exclusively by the argument passed, which contains all the important parameters
     *
     * @param thresholder
     */
    public SimpleEroteticDestinationStrategy(
        final EroteticAnswer<SeaTile> thresholder,
        final FavoriteDestinationStrategy delegate
    ) {
        chooser.add(thresholder);
        this.delegate = delegate;
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
        final Fisher fisher, final MersenneTwisterFast random,
        final FishState model, final Action currentAction
    ) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
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

    @Override
    public void start(final FishState model, final Fisher fisher) {

        this.fisher = fisher;
        this.model = model;
        fisher.addTripListener(this);
        for (final EroteticAnswer<SeaTile> filter : chooser)
            filter.start(model);
    }


    @Override
    public void turnOff(final Fisher fisher) {
        this.fisher.removeTripListener(this);
        for (final EroteticAnswer<SeaTile> filter : chooser)
            filter.turnOff();
        this.fisher = null;
    }
}
