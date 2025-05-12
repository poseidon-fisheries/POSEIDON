package uk.ac.ox.poseidon.io.tables;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.nio.file.Path;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StringColumnReaderFactory extends GlobalScopeFactory<List<String>> {

    private Factory<? extends Path> path;
    private String columnName;

    @Override
    protected List<String> newInstance(final Simulation simulation) {
        return Table
            .read()
            .csv(path.get(simulation).toFile())
            .stream()
            .map(row -> row.getString(columnName))
            .toList();
    }
}
