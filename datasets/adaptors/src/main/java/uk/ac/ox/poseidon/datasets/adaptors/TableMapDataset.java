package uk.ac.ox.poseidon.datasets.adaptors;

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
    public List<String> getTableNames() {
        return ImmutableList.copyOf(tables.keySet());
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
