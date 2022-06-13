package uk.ac.ox.oxfish.geography.fads;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;

public interface FadInitializer<B extends LocalBiology, F extends AbstractFad<B, F>> {


    public F makeFad(@NotNull final FadManager<B, F> fadManager,
                     @Nullable Fisher owner,
                     @NotNull SeaTile initialLocation);


}
