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
import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.mapWithIndex;
import static java.lang.Math.toIntExact;

public class TidyFisherYearlyData implements RowProvider {

    private static final List<String> HEADERS = ImmutableList.of("boat_id", "year", "variable", "value");
    private final Fisher fisher;
    private final String boatId;

    public TidyFisherYearlyData(final Fisher fisher, final String boatId) {
        this.fisher = fisher;
        this.boatId = boatId;
    }

    @Override public List<String> getHeaders() { return HEADERS; }

    @SuppressWarnings("UnstableApiUsage")
    @Override public Iterable<? extends Collection<?>> getRows() {
        return fisher.getYearlyData().getColumns().stream().flatMap(column ->
            mapWithIndex(column.stream(), (value, index) -> ImmutableList.of(
                boatId, // boat_id
                index + 1, // year
                column.getName(), // variable
                column.get(toIntExact(index)) // value
            ))
        ).collect(toImmutableList());
    }

}
