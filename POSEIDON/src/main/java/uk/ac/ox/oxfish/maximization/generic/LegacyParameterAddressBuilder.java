package uk.ac.ox.oxfish.maximization.generic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.join;

public class LegacyParameterAddressBuilder implements ParameterAddressBuilder {
    private final ImmutableList<String> address;

    public LegacyParameterAddressBuilder() {
        this(ImmutableList.of());
    }

    public LegacyParameterAddressBuilder(final Collection<String> address) {
        this.address = ImmutableList.copyOf(address);
    }

    private static <T> List<T> add(
        final Iterable<T> list,
        final T element
    ) {
        return new ImmutableList.Builder<T>()
            .addAll(list)
            .add(element)
            .build();
    }

    private static List<String> addSuffixToAddress(
        final Collection<String> address,
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

    @Override
    public String get() {
        return join(".", address);
    }

    @Override
    public ParameterAddressBuilder add(final String element) {
        return new LegacyParameterAddressBuilder(add(address, element));
    }

    @Override
    public ParameterAddressBuilder addKey(final String key) {
        return new LegacyParameterAddressBuilder(addSuffixToAddress(address, "~", key));
    }

    @Override
    public ParameterAddressBuilder addIndex(final long index) {
        return new LegacyParameterAddressBuilder(addSuffixToAddress(
            address,
            "$",
            String.valueOf(index)
        ));
    }

}
