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
    private static final long serialVersionUID = 2562852069866501476L;

    private final FishState fishState;

    public PurseSeineTripLogger(final FishState fishState) {
        super(false, HEADERS);
        this.fishState = fishState;
    }

    @Override
    public void reactToNewTrip(final TripRecord record, final Fisher fisher) {
        logTripEvent(record, fisher, "trip_start");
    }

    private void logTripEvent(final TripRecord record, final Fisher fisher, final String event) {
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
    public void reactToFinishedTrip(final TripRecord record, final Fisher fisher) {
        logTripEvent(record, fisher, "trip_end");
    }

    @Override
    public void start(final FishState fishState) {
        checkArgument(fishState == this.fishState);
        fishState.getFishers().forEach(fisher -> fisher.addTripListener(this));
    }

}
