package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;
import java.util.List;

public class TaggedRegulationFactory
    extends DecoratedObjectFactory<AlgorithmFactory<? extends Regulation>>
    implements AlgorithmFactory<TaggedRegulation> {

    private List<String> tags;

    public TaggedRegulationFactory() {
    }

    public TaggedRegulationFactory(
        final AlgorithmFactory<? extends Regulation> delegate,
        final String... tags
    ) {
        this(delegate, ImmutableList.copyOf(tags));
    }

    @SuppressWarnings("WeakerAccess")
    public TaggedRegulationFactory(
        final AlgorithmFactory<? extends Regulation> delegate,
        final List<String> tags
    ) {
        super(delegate);
        this.tags = tags;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    @Override
    public TaggedRegulation apply(final FishState fishState) {
        return new TaggedRegulation(getDelegate().apply(fishState), tags);
    }
}
