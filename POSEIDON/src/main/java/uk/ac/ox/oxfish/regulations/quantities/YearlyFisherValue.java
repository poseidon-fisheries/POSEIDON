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

package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class YearlyFisherValue implements AlgorithmFactory<Quantity> {

    private StringParameter name;
    private IntegerParameter entriesFromLast;

    @SuppressWarnings("unused")
    public YearlyFisherValue() {
    }

    public YearlyFisherValue(
        final StringParameter name,
        final IntegerParameter entriesFromLast
    ) {
        this.name = name;
        this.entriesFromLast = entriesFromLast;
    }

    public IntegerParameter getEntriesFromLast() {
        return entriesFromLast;
    }

    public void setEntriesFromLast(final IntegerParameter entriesFromLast) {
        this.entriesFromLast = entriesFromLast;
    }

    public StringParameter getName() {
        return name;
    }

    public void setName(final StringParameter name) {
        this.name = name;
    }

    @Override
    public Quantity apply(final FishState fishState) {
        return makeQuantity(name.getValue(), entriesFromLast.getValue());
    }

    static Quantity makeQuantity(
        final String name,
        final int entriesFromLast
    ) {
        return action -> {
            final TimeSeries<Fisher> yearlyData = ((Fisher) action.getAgent()).getYearlyData();
            final DataColumn column = yearlyData.getColumn(name);
            if (column == null) {
                throw new RuntimeException("Time series not found: " + name);
            }
            return column.getDatumXStepsAgo(entriesFromLast);
        };
    }
}
