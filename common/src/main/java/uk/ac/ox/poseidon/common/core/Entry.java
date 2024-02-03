package uk.ac.ox.poseidon.common.core;

import java.util.AbstractMap;

public class Entry {
    private Entry() {
    }

    /**
     * Just makes it nicer to create AbstractMap.SimpleImmutableEntry objects.
     */
    public static <K, V> AbstractMap.SimpleImmutableEntry<K, V> entry(
        final K k,
        final V v
    ) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }
}
