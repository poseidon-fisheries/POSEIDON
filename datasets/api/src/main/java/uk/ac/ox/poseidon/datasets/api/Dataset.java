package uk.ac.ox.poseidon.datasets.api;

import java.util.List;

public interface Dataset {
    List<String> getTableNames();

    List<Table> getTables();

    Table getTable(String name);
}
