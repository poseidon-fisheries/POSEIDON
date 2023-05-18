package uk.ac.ox.oxfish.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.throwingMerger;

public class Constructors {

    /**
     * Given a map from class objects to names, uses reflection to build a map from names to constructors.
     * This is meant to ease the building of the various classes that are used with AlgorithmFactories.
     * Note: not sure that the return result *has* to be a LinkedHashMap.
     */
    public static <T> LinkedHashMap<String, Supplier<AlgorithmFactory<? extends T>>> fromNames(
        Map<Class<? extends AlgorithmFactory>, String> names
    ) {
        return names.entrySet().stream().collect(toMap(
            Map.Entry::getValue,
            e -> getSupplier(e.getKey()),
            throwingMerger(),
            LinkedHashMap::new
        ));
    }

    /**
     * Given a class object, returns a Supplier based on either its constructor or a getInstance method.
     */
    public static <T> Supplier<AlgorithmFactory<? extends T>> getSupplier(Class<? extends AlgorithmFactory> classObject) {
        try {
            // First try to access a possibly provided `getInstance` method
            final Method getInstance = classObject.getMethod("getInstance");
            return wrap(() -> (AlgorithmFactory<? extends T>) getInstance.invoke(null));
        } catch (NoSuchMethodException | SecurityException e) {
            // Otherwise, get the public argument-less constructor
            try {
                final Constructor<? extends AlgorithmFactory> constructor = classObject.getConstructor();
                return wrap(() -> constructor.newInstance());
            } catch (NoSuchMethodException | SecurityException ex) {
                throw new RuntimeException(
                    "Can't find `getInstance` method or argument-less public constructor for class " + classObject, ex
                );
            }
        }
    }

    /**
     * This is a convenience method to get around the fact that Java won't allow lambdas to
     * throw checked Exceptions. Adapted from https://stackoverflow.com/a/14045585/487946.
     */
    private static <T> Supplier<T> wrap(Callable<T> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
