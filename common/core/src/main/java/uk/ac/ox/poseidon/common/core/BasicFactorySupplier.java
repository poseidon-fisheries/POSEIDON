package uk.ac.ox.poseidon.common.core;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.FactorySupplier;

import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase;

public class BasicFactorySupplier<T extends ComponentFactory<?, ?>> implements FactorySupplier {

    private final Class<? extends T> factoryClass;
    private final String factoryName;

    public BasicFactorySupplier(
        final Class<? extends T> factoryClass
    ) {
        this(factoryClass, makeFactoryName(factoryClass));
    }

    public BasicFactorySupplier(
        final Class<? extends T> factoryClass,
        final String factoryName
    ) {
        this.factoryClass = factoryClass;
        this.factoryName = factoryName;
    }

    public static String makeFactoryName(final Class<? extends ComponentFactory<?, ?>> classObject) {
        final String[] words =
            splitByCharacterTypeCamelCase(
                classObject
                    .getSimpleName()
                    .replaceAll("Factory", "")
            );
        return Stream
            .concat(
                Stream.of(words[0]),
                stream(words).skip(1).map(word -> word.toLowerCase(ENGLISH))
            )
            .collect(joining(" "));
    }

    @Override
    public String getFactoryName() {
        return factoryName;
    }

    @Override
    public Class<? extends ComponentFactory<?, ?>> getFactoryClass() {
        return factoryClass;
    }

    @Override
    public ComponentFactory<?, ?> get() {
        try {
            return factoryClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
