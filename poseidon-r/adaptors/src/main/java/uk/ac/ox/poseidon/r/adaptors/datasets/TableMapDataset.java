/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
package uk.ac.ox.poseidon.r.adaptors.datasets;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.Table;

import java.util.List;
import java.util.Map;

public class TableMapDataset implements Dataset {

    private final Map<String, Table> tables;

    public TableMapDataset(final Map<String, Table> tables) {
        this.tables = ImmutableMap.copyOf(tables);
    }

    @Override
    public String[] getTableNames() {
        return tables.keySet().toArray(new String[0]);
    }

    @Override
    public List<Table> getTables() {
        return ImmutableList.copyOf(tables.values());
    }

    @Override
    public Table getTable(final String name) {
        return tables.get(name);
    }
}
