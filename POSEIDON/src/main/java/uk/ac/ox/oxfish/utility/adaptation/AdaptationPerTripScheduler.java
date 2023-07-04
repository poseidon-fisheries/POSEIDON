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

package uk.ac.ox.oxfish.utility.adaptation;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A list of adaptations to fire each time the fisher finishes a trip
 * Created by carrknight on 8/10/15.
 */
public class AdaptationPerTripScheduler implements TripListener, FisherStartable {

    private static final long serialVersionUID = 6191310505350347880L;
    private final Collection<Adaptation> adaptations = new LinkedList<>();
    private FishState model;
    private Fisher fisher;

    @Override
    public void start(final FishState model, final Fisher fisher) {
        this.model = model;
        this.fisher = fisher;
        fisher.addTripListener(this);

        for (final Adaptation a : adaptations)
            a.start(model, fisher);

    }

    @Override
    public void turnOff(final Fisher fisher) {
        if (this.fisher != null)
            this.fisher.removeTripListener(this);
    }

    /**
     * add an adaptation algorithm to the list. Start it if we have already started
     */
    public void registerAdaptation(final Adaptation adaptation) {

        adaptations.add(adaptation);
        if (model != null)
            adaptation.start(model, fisher);


    }


    public void removeAdaptation(final Adaptation adaptation) {
        adaptations.remove(adaptation);
    }

    @Override
    public void reactToFinishedTrip(final TripRecord record, final Fisher fisher) {
        for (final Adaptation a : adaptations)
            a.adapt(this.fisher, model, this.fisher.grabRandomizer());
    }
}
