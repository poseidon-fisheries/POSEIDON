package uk.ac.ox.oxfish.utility.parameters;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Map;

public class FixedParameterTable implements ParameterTable {
    private final Table<Integer, String, FixedDoubleParameter> table;

    public FixedParameterTable(final Table<Integer, String, FixedDoubleParameter> table) {
        this.table = ImmutableTable.copyOf(table);
    }

    @Override
    public DoubleParameter get(
        final int year,
        final String parameterName
    ) {
        return table.get(year, parameterName);
    }

    @Override
    public Map<String, ? extends DoubleParameter> getParameters(final int year) {
        return table.row(year);
    }

}
