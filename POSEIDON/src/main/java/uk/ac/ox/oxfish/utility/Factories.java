package uk.ac.ox.oxfish.utility;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.BasicFactorySupplier.makeFactoryName;
import static uk.ac.ox.oxfish.utility.Constructors.getSupplier;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.throwingMerger;

public class Factories<T> {

    private final Class<T> classObject;
    private final Map<Class<? extends AlgorithmFactory<?>>, String> names;
    private final Map<String, Supplier<AlgorithmFactory<? extends T>>> constructors;

    @SafeVarargs
    @SuppressWarnings("varargs")
    public Factories(
        final Class<T> classObject,
        final Class<? extends AlgorithmFactory<? extends T>>... classes
    ) {
        this(classObject, ImmutableMap.of(), classes);
    }

    @SafeVarargs
    @SuppressWarnings({"varargs", "unchecked"})
    public Factories(
        final Class<T> classObject,
        final Map<Class<? extends AlgorithmFactory<? extends T>>, String> names,
        final Class<? extends AlgorithmFactory<? extends T>>... classes
    ) {
        this(
            classObject,
            Stream
                .concat(
                    names.entrySet().stream(),
                    Arrays.stream(classes)
                        .map(factoryClass -> entry(factoryClass, makeFactoryName(factoryClass)))
                )
                .collect(toImmutableMap(
                    Entry::getKey,
                    Entry::getValue
                ))
        );
    }

    public Factories(
        final Class<T> classObject,
        final Map<Class<? extends AlgorithmFactory<? extends T>>, String> names
    ) {
        this.classObject = classObject;
        this.names = ImmutableMap.copyOf(names);
        this.constructors =
            this.names.entrySet().stream().collect(toMap(
                Entry::getValue,
                e -> getSupplier(e.getKey()),
                throwingMerger(),
                LinkedHashMap::new
            ));
    }

    public Class<T> getClassObject() {
        return classObject;
    }

    public Map<Class<? extends AlgorithmFactory<?>>, String> getNames() {
        return names;
    }

    public Map<String, Supplier<AlgorithmFactory<? extends T>>> getConstructors() {
        return constructors;
    }
}
