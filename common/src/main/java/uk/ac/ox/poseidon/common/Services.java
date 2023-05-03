package uk.ac.ox.poseidon.common;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;

public class Services {
    public static <S> S loadFirst(final Class<S> service, final Predicate<S> predicate) {
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
    public static <S> List<S> loadAll(final Class<S> service, final Predicate<S> predicate) {
        return stream(ServiceLoader.load(service).iterator())
            .filter(predicate)
            .collect(toImmutableList());
    }
}
