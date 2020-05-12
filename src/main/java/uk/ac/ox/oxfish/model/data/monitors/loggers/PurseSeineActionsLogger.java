/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.monitors.loggers;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeUnassociatedSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.monitors.observers.PurseSeinerActionObserver;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

public class PurseSeineActionsLogger implements AdditionalStartable, RowProvider {

    private static final List<String> HEADERS = ImmutableList.of(
        "boat_id",
        "action_type",
        "lon",
        "lat",
        "step",
        "trip_start",
        "trip_end"
    );
    private final Collection<ActionObserver<? extends PurseSeinerAction>> observers = ImmutableList.of(
        new ActionObserver<>(DeployFad.class),
        new ActionObserver<>(MakeFadSet.class),
        new ActionObserver<>(MakeUnassociatedSet.class)
    );
    private final ImmutableList.Builder<ActionRecord> actionRecords = new ImmutableList.Builder<>();
    private final double hoursPerStep;
    private final double stepsPerDay;
    private final NauticalMap nauticalMap;
    private FishState fishState;

    public PurseSeineActionsLogger(
        final FishState fishState
    ) {
        this.hoursPerStep = fishState.getHoursPerStep();
        this.stepsPerDay = fishState.getStepsPerDay();
        this.nauticalMap = fishState.getMap();
    }

    @Override public List<String> getHeaders() { return HEADERS; }

    @Override public Iterable<? extends Collection<?>> getRows() {
        return actionRecords.build().stream().map(ActionRecord::asRow).collect(toImmutableList());
    }

    @Override public void start(final FishState fishState) {
        this.fishState = fishState;
        observers.forEach(observer -> observer.start(fishState));
    }

    private class ActionRecord {

        private final String boatId;
        private final String actionType;
        private final double lon;
        private final double lat;
        private final int actionStep;
        private final int tripStartStep;
        private Integer tripEndStep;

        private ActionRecord(PurseSeinerAction action) {
            this.boatId = action.getFisher().getTags().get(0);
            this.actionType = actionType(action);
            final Coordinate coordinates = nauticalMap.getCoordinates(action.getLocation());
            this.lon = coordinates.x;
            this.lat = coordinates.y;
            this.actionStep = action.getStep();
            final TripRecord currentTrip = action.getFisher().getCurrentTrip();
            this.tripStartStep = (int) (currentTrip.getTripDate() * stepsPerDay);
            action.getFisher().addTripListener(new TripEndRecorder(currentTrip));
        }

        private String actionType(PurseSeinerAction action) {
            if (action instanceof DeployFad) return "DEPLOY";
            else if (action instanceof MakeFadSet) return "FADSET";
            else if (action instanceof MakeUnassociatedSet) return "UNASET";
            else throw new IllegalArgumentException("Unknown action type.");
        }

        private List<?> asRow() {
            return unmodifiableList(newArrayList(
                boatId,
                actionType,
                lon,
                lat,
                actionStep,
                tripStartStep,
                tripEndStep
            ));
        }

        private class TripEndRecorder implements TripListener {

            private final TripRecord currentTrip;

            private TripEndRecorder(final TripRecord currentTrip) {
                this.currentTrip = currentTrip;
            }

            @Override public void reactToFinishedTrip(final TripRecord trip, final Fisher fisher) {
                if (trip == currentTrip) {
                    tripEndStep = tripStartStep + (int) (trip.getDurationInHours() / hoursPerStep);
                    fishState.scheduleOnce(__ -> fisher.removeTripListener(this), StepOrder.DAWN);
                }
            }

        }

    }

    private class ActionObserver<A extends PurseSeinerAction> extends PurseSeinerActionObserver<A> {

        ActionObserver(final Class<A> observedClass) { super(observedClass); }

        @Override public void observe(final A action) { actionRecords.add(new ActionRecord(action)); }

    }

}
