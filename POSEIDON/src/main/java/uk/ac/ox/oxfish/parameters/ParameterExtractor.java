package uk.ac.ox.oxfish.parameters;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.maximization.generic.ParameterAddressBuilder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParameterExtractor<P extends DoubleParameter> {

    private final Class<? extends P> parameterClass;

    @SuppressWarnings("WeakerAccess")
    public ParameterExtractor(final Class<? extends P> parameterClass) {
        this.parameterClass = parameterClass;
    }

    @NotNull
    private static List<PropertyDescriptor> getPropertyDescriptors(final Object object) {
        try {
            return Arrays
                .stream(Introspector.getBeanInfo(object.getClass()).getPropertyDescriptors())
                .filter(propertyDescriptor -> !propertyDescriptor.getName().equals("class"))
                .collect(Collectors.toList());
        } catch (final IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object invoke(final Object object, final Method method) {
        try {
            return method.invoke(object);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<Parameter> getParameters(final Object object) {
        return getParameters(object, new ParameterAddressBuilder());
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<Parameter> getParameters(
        final Object object,
        final ParameterAddressBuilder addressBuilder
    ) {
        return Streams
            .stream(Optional.ofNullable(object))
            .filter(o ->
                // Exclude problematic types from search:
                // - Class objects have annotation getters that generate IllegalAccessException on newer JVMs
                // - Path objects iterate on themselves, creating an infinite loop
                !(o instanceof Class || o instanceof Path)
            )
            .flatMap(o -> {
                if (parameterClass.isAssignableFrom(o.getClass())) {
                    return Stream.of(new Parameter(addressBuilder.get(), parameterClass.cast(o)));
                } else if (o instanceof Map) {
                    return getParametersFromMap((Map<?, ?>) o, addressBuilder);
                } else if (o instanceof Iterable) {
                    return getParametersFromIterable((Iterable<?>) o, addressBuilder);
                } else {
                    return getParametersFromObject(o, addressBuilder);
                }
            });
    }

    @NotNull
    private Stream<Parameter> getParametersFromObject(
        final Object o,
        final ParameterAddressBuilder addressBuilder
    ) {
        return getPropertyDescriptors(o)
            .stream()
            .filter(propertyDescriptor -> propertyDescriptor.getReadMethod() != null)
            .flatMap(propertyDescriptor -> getParameters(
                invoke(o, propertyDescriptor.getReadMethod()),
                addressBuilder.add(propertyDescriptor.getName())
            ));
    }

    private Stream<Parameter> getParametersFromMap(
        final Map<?, ?> objectMap,
        final ParameterAddressBuilder address
    ) {
        return objectMap
            .entrySet()
            .stream()
            .flatMap(entry ->
                getParameters(
                    entry.getValue(),
                    address.addKey(entry.getKey().toString())
                )
            );
    }

    private Stream<Parameter> getParametersFromIterable(
        final Iterable<?> objects,
        final ParameterAddressBuilder address
    ) {
        return Streams
            .mapWithIndex(
                Streams.stream(objects),
                FishStateUtilities::entry
            )
            .flatMap(entry ->
                getParameters(
                    entry.getKey(),
                    address.addIndex(entry.getValue())
                )
            );
    }

    public class Parameter {
        private final String address;
        private final P object;

        public Parameter(final String address, final P object) {
            this.address = address;
            this.object = object;
        }

        @SuppressWarnings("WeakerAccess")
        public String getAddress() {
            return address;
        }

        public P getObject() {
            return object;
        }
    }

}
