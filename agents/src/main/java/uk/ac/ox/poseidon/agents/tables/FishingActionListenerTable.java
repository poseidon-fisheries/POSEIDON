/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import uk.ac.ox.poseidon.agents.behaviours.fishing.FishingAction;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;
import java.util.Optional;

public class FishingActionListenerTable extends SpatialActionListenerTable<FishingAction> {

    private static final String SPECIES_CODE = "species_code";
    private static final String GROSS_CATCH = "gross_catch";
    private static final String RETAINED = "retained";
    private static final String DISCARDED_ALIVE = "discarded_alive";
    private static final String DISCARDED_DEAD = "discarded_dead";

    FishingActionListenerTable() {
        super(FishingAction.class);
        get().addColumns(
            StringColumn.create(SPECIES_CODE),
            DoubleColumn.create(GROSS_CATCH),
            DoubleColumn.create(RETAINED),
            DoubleColumn.create(DISCARDED_ALIVE),
            DoubleColumn.create(DISCARDED_DEAD)
        );
    }

    @Override
    public void receive(final FishingAction fishingAction) {
        final Map<String, Bucket<?>> buckets = Map.of(
            GROSS_CATCH, fishingAction.getGrossCatch(),
            RETAINED, fishingAction.getDisposition().getRetained(),
            DISCARDED_ALIVE, fishingAction.getDisposition().getDiscardedAlive(),
            DISCARDED_DEAD, fishingAction.getDisposition().getDiscardedDead()
        );
        final Table<Species, String, Double> table = HashBasedTable.create();
        buckets.forEach((columnName, bucket) ->
            bucket.getMap().forEach((species, content) ->
                table.put(species, columnName, content.asBiomass().asKg())
            )
        );
        table.rowKeySet().forEach(species -> {
            // FIXME: this will repeat the action info for each species, which is a colossal waste
            //  of space. We should have some kind of action id (which needs to be implemented) and
            //  store the catch data in a separate table.
            super.receive(fishingAction);
            get().stringColumn(SPECIES_CODE).append(species.getCode());
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
