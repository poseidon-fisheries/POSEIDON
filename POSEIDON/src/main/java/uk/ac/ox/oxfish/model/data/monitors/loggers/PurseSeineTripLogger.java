package uk.ac.ox.oxfish.model.data.monitors.loggers;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class PurseSeineTripLogger
    extends ClearableLogger
    implements AdditionalStartable, TripListener {

    private static final List<String> HEADERS = ImmutableList.of(
        "ves_no", "trip_id", "event", "date", "port_name", "lon", "lat"
    );

    private final FishState fishState;

    public PurseSeineTripLogger(final FishState fishState) {
        super(false, HEADERS);
        this.fishState = fishState;
    }

    @Override
    public void reactToNewTrip(TripRecord record, Fisher fisher) {
        logTripEvent(record, fisher, "trip_start");
    }

    @Override
    public void reactToFinishedTrip(TripRecord record, Fisher fisher) {
        logTripEvent(record, fisher, "trip_end");
    }

    private void logTripEvent(TripRecord record, Fisher fisher, String event) {
        final Port port = fisher.getHomePort();
        final Coordinate portCoordinates = fishState.getMap().getCoordinates(port.getLocation());
        addRow(
            fisher.getTags().get(0), // ves_no
            record.getTripId(), // trip_id
            event, // event
            fishState.getDate(), // date
            port.getName(), // port_name
            portCoordinates.x, // lon
            portCoordinates.y // lat
        );
    }

    @Override
    public void start(FishState fishState) {
        checkArgument(fishState == this.fishState);
        fishState.getFishers().forEach(fisher -> fisher.addTripListener(this));
    }

}
