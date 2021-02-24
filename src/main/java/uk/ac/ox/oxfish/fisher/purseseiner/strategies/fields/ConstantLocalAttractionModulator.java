package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;

public enum ConstantLocalAttractionModulator implements LocalAttractionModulator {

    INSTANCE;

    @Override
    public double modulate(int x, int y, int t, Fisher fisher) { return 1.0; }
}
