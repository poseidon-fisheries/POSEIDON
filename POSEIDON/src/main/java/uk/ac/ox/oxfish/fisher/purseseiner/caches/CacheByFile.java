package uk.ac.ox.oxfish.fisher.purseseiner.caches;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class CacheByFile<T> implements Function<Path, T> {
    private final Cache<String, T> cache;
    private final Function<Path, T> readFunction;

    public CacheByFile(final Function<Path, T> readFunction) {
        this.readFunction = readFunction;
        this.cache = CacheBuilder.newBuilder().build();
    }

    @Override
    public T apply(final Path path) {
        try {
            return cache.get(
                path.toFile().getCanonicalPath(),
                () -> readFunction.apply(path)
            );
        } catch (final ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
