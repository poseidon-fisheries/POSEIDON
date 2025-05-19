/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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
