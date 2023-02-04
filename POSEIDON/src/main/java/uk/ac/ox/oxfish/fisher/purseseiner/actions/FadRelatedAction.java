package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;

public interface FadRelatedAction<B extends LocalBiology, F extends AbstractFad<B, F>> {
    F getFad();
}
