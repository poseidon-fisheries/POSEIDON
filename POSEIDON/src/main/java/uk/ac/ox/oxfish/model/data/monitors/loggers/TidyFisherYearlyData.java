/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

import java.util.List;
import java.util.function.Predicate;

import static java.lang.Math.toIntExact;

public class TidyFisherYearlyData extends TidyTimeSeries<TimeSeries<Fisher>> {

    private static final List<String> HEADERS = ImmutableList.of("ves_no", "year", "variable", "value", "unit");
    private final String boatId;

    public TidyFisherYearlyData(
        final TimeSeries<Fisher> fisherYearlyData,
        final Predicate<String> columnNamePredicate,
        final String boatId
    ) {
        super(fisherYearlyData, columnNamePredicate);
        this.boatId = boatId;
    }

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    List<Object> makeRow(
        final DataColumn column,
        final int index
    ) {
        return ImmutableList.of(
            boatId, // boat_id
            index + 1, // year
            column.getName(), // variable
            column.get(toIntExact(index)), // value
            column.getUnit().getSymbol() // unit of measure ("" if unit is dimensionless, i.e., ONE)
        );
    }

}
