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

package uk.ac.ox.oxfish.fisher.purseseiner.caches;

import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class LocationFisherValuesByActionCache extends FisherValuesByActionFromFileCache<Map<Coordinate, Double>> {

    public LocationFisherValuesByActionCache() {
        super(Collections::emptyMap);
    }

    protected Map<Integer, Map<String, Map<Class<? extends PurseSeinerAction>, Map<Coordinate, Double>>>> readValues(
        final Path locationValuesFile
    ) {
        return recordStream(locationValuesFile)
            .collect(
                groupingBy(
                    record -> record.getInt("year"),
                    groupingBy(
                        record -> record.getString("ves_no"),
                        groupingBy(
                            record -> ActionClass.valueOf(record.getString("action_type")).getActionClass(),
                            toMap(
                                record -> new Coordinate(record.getDouble("lon"), record.getDouble("lat")),
                                record -> record.getDouble("value")
                            )
                        )
                    )
                ));
    }

    public Map<Int2D, Double> getLocationValues(
        final Path locationValuesFile,
        final int targetYear,
        final Fisher fisher,
        final Class<? extends PurseSeinerAction> actionClass
    ) {
        return get(locationValuesFile, targetYear, fisher, actionClass)
            .entrySet()
            .stream()
            .flatMap(entry -> {
                final SeaTile tile = fisher.grabState().getMap().getSeaTile(entry.getKey());
                return tile != null && tile.isWater()
                    ? Stream.of(entry(new Int2D(tile.getGridX(), tile.getGridY()), entry.getValue()))
                    : Stream.empty();
            })
            .collect(toImmutableMap(Entry::getKey, Entry::getValue));
    }

}
