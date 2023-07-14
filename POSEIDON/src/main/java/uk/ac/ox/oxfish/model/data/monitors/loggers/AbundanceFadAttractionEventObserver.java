package uk.ac.ox.oxfish.model.data.monitors.loggers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFadAttractionEvent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.Observer;

import java.util.List;

public class AbundanceFadAttractionEventObserver
    implements Observer<AbundanceFadAttractionEvent> {

    private final FishState fishState;
    private final ClearableLogger eventLogger = new ClearableLogger(
        true, "fad_id", "event_id", "date", "lon", "lat"
    );
    private final List<String> abundanceHeaders =
        ImmutableList.of("event_id", "species_code", "sub", "bin", "abundance");
    private final ClearableLogger tileAbundanceLogger = new ClearableLogger(true, abundanceHeaders);
    private final ClearableLogger fadAbundanceLogger = new ClearableLogger(true, abundanceHeaders);

    public AbundanceFadAttractionEventObserver(
        final FishState fishState
    ) {
        this.fishState = fishState;
    }

    public ClearableLogger getTileAbundanceLogger() {
        return tileAbundanceLogger;
    }

    public ClearableLogger getFadAbundanceLogger() {
        return fadAbundanceLogger;
    }

    public ClearableLogger getEventLogger() {
        return eventLogger;
    }

    @Override
    public void observe(final AbundanceFadAttractionEvent event) {
        final AbundanceAggregatingFad fad = event.getFad();
        final Coordinate coordinates = fad.getCoordinate();
        eventLogger.addRow(
            fad.getId(), event.getId(), fishState.getDate(), coordinates.x, coordinates.y
        );
        ImmutableMap.of(
            event.getFadAbundanceDelta(), fadAbundanceLogger,
            event.getTileAbundanceBefore(), tileAbundanceLogger
        ).forEach((biology, logger) ->
            biology.getStructuredAbundance().forEach((species, abundance) ->
                abundance.forEachIndex((sub, bin) ->
                    logger.addRow(
                        event.getId(),
                        species.getCode(),
                        sub,
                        bin,
                        abundance.getAbundance(sub, bin)
                    )
                )
            )
        );
    }
}
