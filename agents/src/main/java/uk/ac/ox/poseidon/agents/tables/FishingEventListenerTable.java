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

package uk.ac.ox.poseidon.agents.tables;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import uk.ac.ox.poseidon.agents.catches.disposition.Disposition;
import uk.ac.ox.poseidon.agents.tasks.fishing.FishingEvent;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("FieldCanBeLocal")
public class FishingEventListenerTable extends ListenerTable<FishingEvent> {

    public static final String VESSEL_ID = "vessel_id";
    public static final String START_DATE_TIME = "start_date_time";
    public static final String END_DATE_TIME = "end_date_time";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String SPECIES_CODE = "species_code";
    public static final String GROSS_CATCH = "gross_catch";
    public static final String RETAINED = "retained";
    public static final String DISCARDED_ALIVE = "discarded_alive";
    public static final String DISCARDED_DEAD = "discarded_dead";

    private final StringColumn vesselId = StringColumn.create(VESSEL_ID);
    private final DateTimeColumn startDateTime = DateTimeColumn.create(START_DATE_TIME);
    private final DateTimeColumn endDateTime = DateTimeColumn.create(END_DATE_TIME);
    private final DoubleColumn lon = DoubleColumn.create(LONGITUDE);
    private final DoubleColumn lat = DoubleColumn.create(LATITUDE);
    private final StringColumn speciesCode = StringColumn.create(SPECIES_CODE);
    private final DoubleColumn grossCatch = DoubleColumn.create(GROSS_CATCH);
    private final DoubleColumn retained = DoubleColumn.create(RETAINED);
    private final DoubleColumn discardedAlive = DoubleColumn.create(DISCARDED_ALIVE);
    private final DoubleColumn discardedDead = DoubleColumn.create(DISCARDED_DEAD);

    public FishingEventListenerTable() {
        super(FishingEvent.class);
        get().addColumns(
            vesselId,
            startDateTime,
            endDateTime,
            lon,
            lat,
            speciesCode,
            grossCatch,
            retained,
            discardedAlive,
            discardedDead
        );
    }

    @Override
    public void receive(final FishingEvent event) {
        final Disposition disposition = event.getOutcome().getDisposition();
        final Map<String, Bucket> buckets = Map.of(
            GROSS_CATCH, event.getOutcome().getGrossCatch(),
            RETAINED, disposition.getRetained(),
            DISCARDED_ALIVE, disposition.getDiscardedAlive(),
            DISCARDED_DEAD, disposition.getDiscardedDead()
        );
        final Table<Species, String, Double> table = HashBasedTable.create();
        buckets.forEach((columnName, bucket) ->
            bucket.getMap().forEach((species, content) ->
                table.put(species, columnName, content.asBiomass().asKg())
            )
        );
        table.rowKeySet().forEach(species -> {
            // FIXME: this repeats the event info for each species, which is a colossal waste
            //  of space. We should have some kind of event id (which needs to be implemented) and
            //  store the catch data in a separate table.
            vesselId.append(event.getAction().getAgent().getId());
            startDateTime.append(event.getStartDateTime());
            endDateTime.append(event.getEndDateTime());
            lon.append(event.getAction().getEndCoordinate().lon);
            lat.append(event.getAction().getEndCoordinate().lat);
            speciesCode.append(species.getCode());
            buckets.keySet().forEach(columnName ->
                get()
                    .doubleColumn(columnName)
                    .append(
                        Optional.ofNullable(table.get(species, columnName)).orElse(0.0)
                    )
            );
        });

    }
}
