package uk.ac.ox.poseidon.datasets.api;

public interface Row {

    Object getValue(String columnName);

    default <V> V getValue(final String columnName, final Class<V> expectedType) {
        return expectedType.cast(getValue(columnName));
    }

}
