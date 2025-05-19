/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility.yaml;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;

/**
 * A simple hash-map that gets created at the end of the model to collect FishState information
 * Created by carrknight on 11/20/15.
 */
public class ModelResults extends HashMap<String, Object> {


    private static final long serialVersionUID = -5596654891501051700L;

    /**
     * Construct the hash-map. Ready for serialization!
     */
    public ModelResults(final FishState state) {


        //dump columns of yearly data in
        final HashMap<String, Object> modelData = new HashMap<>(state.getYearlyDataSet().getColumns().size());
        for (final DataColumn column : state.getYearlyDataSet().getColumns())
            modelData.put(column.getName(), column.copy());
        this.put("FishState", modelData);

        //also daily data
        //dump columns of yearly data in
        final HashMap<String, Object> dailyData = new HashMap<>(state.getDailyDataSet().getColumns().size());
        for (final DataColumn column : state.getDailyDataSet().getColumns())
            dailyData.put(column.getName(), column.copy());
        this.put("FishState Daily", dailyData);

        //Also prints out fisher's averages
        if (state.getFishers().size() > 0) {
            final HashMap<String, Object> fisherData = new HashMap<>();
            final Collection<DataColumn> fisherColumns = state.getFishers().get(0).getYearlyData().getColumns();
            final int years = fisherColumns.iterator().next().size();
            for (final DataColumn column : fisherColumns) {
                final HashMap<String, Object> columnData = new HashMap<>();
                fisherData.put(column.getName(), columnData);

                for (int year = 0; year < years; year++) {

                    final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
                    for (final Fisher fisher : state.getFishers())
                        if (fisher.getYearlyData().numberOfObservations() > year)
                            stats.accept(fisher.getYearlyData().getColumn(column.getName()).get(year));
                        else
                            stats.accept(Double.NaN);
                    final HashMap<String, Object> yearData = new HashMap<>();
                    columnData.put(Integer.toString(year), yearData);
                    yearData.put("Average", stats.getAverage());
                    yearData.put("Max", stats.getMax());
                    yearData.put("Min", stats.getMin());
                    yearData.put("Count", stats.getCount());
                }

            }
            this.put("Fisher", fisherData);
        }
    }

}
