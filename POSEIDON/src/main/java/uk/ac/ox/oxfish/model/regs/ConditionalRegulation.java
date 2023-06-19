package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public abstract class ConditionalRegulation extends DecoratedRegulation {

    ConditionalRegulation(final Regulation delegate) {
        super(delegate);
    }

    @Override
    public boolean canFishHere(final Fisher agent, final SeaTile tile, final FishState model, final int timeStep) {
        return !appliesTo(agent, timeStep) || getDelegate().canFishHere(agent, tile, model, timeStep);
    }

    abstract boolean appliesTo(final Fisher fisher, final int timeStep);

    @Override
    public double maximumBiomassSellable(
        final Fisher agent,
        final Species species,
        final FishState model,
        final int timeStep
    ) {
        return appliesTo(agent, timeStep)
            ? getDelegate().maximumBiomassSellable(agent, species, model, timeStep)
            : Double.MAX_VALUE;
    }

    @Override
    public boolean allowedAtSea(final Fisher fisher, final FishState model, final int timeStep) {
        return !appliesTo(fisher, timeStep) || getDelegate().allowedAtSea(fisher, model, timeStep);
    }

}
