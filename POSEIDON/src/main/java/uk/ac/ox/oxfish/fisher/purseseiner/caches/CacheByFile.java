package uk.ac.ox.oxfish.fisher.purseseiner.caches;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class CacheByFile<T> implements Function<Path, T> {
    private final Cache<Object, T> cache;
    private final Function<Path, T> readFunction;
    public CacheByFile(final Function<Path, T> readFunction) {
        this.readFunction = readFunction;
        this.cache = CacheBuilder.newBuilder().build();
    }

    public Cache<Object, T> getCache() {
        return cache;
    }

    @Override
    public T apply(final Path path) {
        try {
            return cache.get(fileKey(path), () -> readFunction.apply(path));
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object fileKey(final Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class).fileKey();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
