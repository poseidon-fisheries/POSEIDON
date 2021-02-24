package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;

@FunctionalInterface
public interface GlobalAttractionModulator {
    double modulate(final Fisher fisher);
}
