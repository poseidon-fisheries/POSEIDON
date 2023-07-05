package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.Collection;
import java.util.Set;

public class TaggedRegulation extends ConditionalRegulation {

    private final Set<String> tags;

    public TaggedRegulation(
        final Regulation delegate,
        final Collection<String> tags
    ) {
        super(delegate);
        this.tags = ImmutableSet.copyOf(tags);
    }

    public Set<String> getTags() {
        return ImmutableSet.copyOf(tags);
    }

    @Override
    boolean appliesTo(final Fisher fisher, final int timeStep) {
        return fisher.getTagsList().stream().anyMatch(tags::contains);
    }

    @Override
    public Regulation makeCopy() {
        return new TaggedRegulation(getDelegate().makeCopy(), tags);
    }
}
