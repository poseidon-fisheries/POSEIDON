package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.Streams;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.maximization.generic.HardEdgeOptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.ParameterAddressBuilder;
import uk.ac.ox.oxfish.parameters.FreeParameter;
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

public class EpoPathPlanningAbundanceScenarioTest extends TestCase {

    private static Object invoke(final Object object, final Method method) {
        try {
            return method.invoke(object);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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

    public void testParameterAnnotationReading() {
        final EpoPathPlanningAbundanceScenario scenario = new EpoPathPlanningAbundanceScenario();
        final List<OptimizationParameter> freeParameters =
            getFreeParameters(scenario, new ParameterAddressBuilder()).collect(Collectors.toList());
        freeParameters.forEach(System.out::println);
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<OptimizationParameter> getFreeParameters(
        final Object object,
        final ParameterAddressBuilder addressBuilder
    ) {
        return Streams
            .stream(Optional.ofNullable(object))
            .filter(o -> !(o instanceof Path)) // Path objects iterate on themselves, creating an infinite loop
            .flatMap(o -> {
                if (o instanceof Map) {
                    return getFreeParametersFromMap((Map<?, ?>) o, addressBuilder);
                } else if (o instanceof Iterable) {
                    return getFreeParametersFromIterable((Iterable<?>) o, addressBuilder);
                } else {
                    return getFreeParametersFromObject(o, addressBuilder);
                }
            });
    }

    @NotNull
    private Stream<OptimizationParameter> getFreeParametersFromObject(
        final Object o,
        final ParameterAddressBuilder addressBuilder
    ) {
        final List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors(o);
        return Stream.concat(
            propertyDescriptors.stream()
                .filter(this::isFreeParameter)
                .map(p -> new HardEdgeOptimizationParameter(
                    (o instanceof DoubleParameter ? addressBuilder : addressBuilder.add(p.getName())).get(),
                    p.getWriteMethod().getAnnotation(FreeParameter.class)
                )),
            propertyDescriptors.stream()
                .filter(p -> p.getReadMethod() != null)
                .flatMap(p -> getFreeParameters(
                    invoke(o, p.getReadMethod()),
                    addressBuilder.add(p.getName())
                ))
        );
    }

    private Stream<OptimizationParameter> getFreeParametersFromMap(
        final Map<?, ?> objectMap,
        final ParameterAddressBuilder address
    ) {
        return objectMap
            .entrySet()
            .stream()
            .flatMap(entry ->
                getFreeParameters(
                    entry.getValue(),
                    address.addKey(entry.getKey().toString())
                )
            );
    }

    private Stream<OptimizationParameter> getFreeParametersFromIterable(
        final Iterable<?> objects,
        final ParameterAddressBuilder address
    ) {
        return Streams
            .mapWithIndex(
                Streams.stream(objects),
                FishStateUtilities::entry
            )
            .flatMap(entry ->
                getFreeParameters(
                    entry.getKey(),
                    address.addIndex(entry.getValue())
                )
            );
    }

    private boolean isFreeParameter(final PropertyDescriptor propertyDescriptor) {
        return Optional.ofNullable(propertyDescriptor.getWriteMethod())
            .map(method -> method.getAnnotation(FreeParameter.class))
            .isPresent();
    }

}