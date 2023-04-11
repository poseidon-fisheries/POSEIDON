package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

import java.util.concurrent.atomic.AtomicLong;

public class AbundanceFadAttractionEvent {

    private static final AtomicLong nextId = new AtomicLong();
    private final long id = nextId.getAndIncrement();
    private final AbundanceAggregatingFad fad;
    private final AbundanceLocalBiology tileAbundanceBefore;
    private final AbundanceLocalBiology fadAbundanceDelta;

    public AbundanceFadAttractionEvent(
        final AbundanceAggregatingFad fad,
        final AbundanceLocalBiology tileAbundanceBefore,
        final AbundanceLocalBiology fadAbundanceDelta
    ) {
        this.fad = fad;
        this.tileAbundanceBefore = tileAbundanceBefore;
        this.fadAbundanceDelta = fadAbundanceDelta;
    }

    public long getId() {
        return id;
    }

    public AbundanceAggregatingFad getFad() {
        return fad;
    }

    public AbundanceLocalBiology getTileAbundanceBefore() {
        return tileAbundanceBefore;
    }

    public AbundanceLocalBiology getFadAbundanceDelta() {
        return fadAbundanceDelta;
    }
}
