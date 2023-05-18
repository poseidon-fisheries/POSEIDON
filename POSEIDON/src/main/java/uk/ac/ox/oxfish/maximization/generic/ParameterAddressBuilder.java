package uk.ac.ox.oxfish.maximization.generic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.join;

public class ParameterAddressBuilder implements Supplier<String> {
    private final ImmutableList<String> address;

    public ParameterAddressBuilder() {
        this(ImmutableList.of());
    }

    public ParameterAddressBuilder(final List<String> address) {
        this.address = ImmutableList.copyOf(address);
    }

    @Override
    public String get() {
        return join(".", address);
    }

    public ParameterAddressBuilder add(final String element) {
        return new ParameterAddressBuilder(add(address, element));
    }

    private static <T> List<T> add(final List<T> list, final T element) {
        return new ImmutableList.Builder<T>()
            .addAll(list)
            .add(element)
            .build();
    }

    public ParameterAddressBuilder addKey(final String key) {
        return new ParameterAddressBuilder(addSuffixToAddress(address, "~", key));
    }

    private static List<String> addSuffixToAddress(
        final List<String> address,
        final String separator,
        final String suffix
    ) {
        checkArgument(!address.isEmpty());
        final int size = address.size();
        return Streams
            .concat(
                address.stream().limit(size - 1),
                Stream.of(Iterables.getLast(address)).map(s -> s + separator + suffix)
            )
            .collect(toImmutableList());
    }

    public ParameterAddressBuilder addIndex(final long index) {
        return new ParameterAddressBuilder(addSuffixToAddress(address, "$", String.valueOf(index)));
    }

}
