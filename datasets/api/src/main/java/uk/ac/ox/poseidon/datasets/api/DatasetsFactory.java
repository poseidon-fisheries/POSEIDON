package uk.ac.ox.poseidon.datasets.api;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface DatasetsFactory extends
    Predicate<Object>,
    Function<Object, Map<String, Dataset>> {
}
