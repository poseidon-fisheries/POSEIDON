package uk.ac.ox.oxfish.utility;

import static com.google.common.base.Predicates.not;
import static java.util.stream.Collectors.toCollection;

import ec.util.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.jetbrains.annotations.NotNull;
import sim.util.Bag;

public class MasonUtils {

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> Stream<T> bagToStream(Bag bag) {
        return Optional.ofNullable(bag)
            .map(b -> IntStream.range(0, b.size()).mapToObj(i -> (T) b.get(i)))
            .orElse(Stream.empty());
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> Optional<T> oneOf(Bag bag, MersenneTwisterFast random) {
        //noinspection ConstantConditions,Guava
        return Optional.ofNullable(bag)
            .filter(not(Bag::isEmpty))
            .map(b -> (T) b.get(random.nextInt(b.size())));
    }

    @NotNull
    public static <T> Optional<T> oneOf(List<T> list, MersenneTwisterFast random) {
        //noinspection ConstantConditions,Guava
        return Optional.ofNullable(list)
            .filter(not(List::isEmpty))
            .map(l -> l.get(random.nextInt(l.size())));
    }

    @NotNull
    public static <T> Optional<T> oneOf(Stream<T> stream, MersenneTwisterFast random) {
        final ArrayList<T> list = stream.collect(toCollection(ArrayList::new));
        return oneOf(list, random);
    }

    @NotNull
    public static <T> Optional<T> oneOf(ListOrderedSet<T> set, MersenneTwisterFast random) {
        //noinspection ConstantConditions,Guava
        return Optional.ofNullable(set)
            .filter(not(ListOrderedSet::isEmpty))
            .map(l -> l.get(random.nextInt(l.size())));
    }

}
