package uk.ac.ox.poseidon.datasets.api;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

public interface DatasetFactory extends
    Predicate<Object>,
    Function<Object, Entry<String, Dataset>> {
    boolean isAutoRegistered();
    String getDatasetName();
}
