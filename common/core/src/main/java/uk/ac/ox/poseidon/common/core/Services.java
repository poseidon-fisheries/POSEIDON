package uk.ac.ox.poseidon.common.core;

import uk.ac.ox.poseidon.common.api.AdaptorFactory;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;

public class Services {

    public static <D, T, F extends AdaptorFactory<D, T>> F loadAdaptorFactory(
        final Class<F> adaptorFactoryClass,
        final Class<D> delegateClass
    ) {
        final F adaptorFactory = Services.loadFirst(
            adaptorFactoryClass,
            f -> f.getDelegateClass().isAssignableFrom(delegateClass)
        );
        return adaptorFactoryClass.cast(adaptorFactory);
    }

    public static <S> S loadFirst(
        final Class<S> service,
        final Predicate<? super S> predicate
    ) {
        final Iterator<S> it = ServiceLoader.load(service).iterator();
        if (!it.hasNext()) {
            throw new IllegalStateException(String.format(
                "No service provider found on classpath for %s",
                service.getCanonicalName()
            ));
        }
        while (it.hasNext()) {
            final S serviceProvider = it.next();
            if (predicate.test(serviceProvider)) {
                return serviceProvider;
            }
        }
        throw new IllegalStateException(String.format(
            "No service provider matching the predicate found for %s",
            service.getCanonicalName()
        ));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <S> List<S> loadAll(
        final Class<? extends S> service,
        final Predicate<? super S> predicate
    ) {
        return stream(ServiceLoader.load(service).iterator())
            .filter(predicate)
            .collect(toImmutableList());
    }
}
