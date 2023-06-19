package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public abstract class DecoratedRegulation implements Regulation {

    private final Regulation delegate;

    public DecoratedRegulation(final Regulation delegate) {
        this.delegate = delegate;
    }

    public Regulation getDelegate() {
        return delegate;
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(final Fisher fisher) {
        delegate.turnOff(fisher);
    }

    @Override
    public boolean canFishHere(final Fisher agent, final SeaTile tile, final FishState model, final int timeStep) {
        return delegate.canFishHere(agent, tile, model, timeStep);
    }

    @Override
    public double maximumBiomassSellable(
        final Fisher agent,
        final Species species,
        final FishState model,
        final int timeStep
    ) {
        return delegate.maximumBiomassSellable(agent, species, model);
    }

    @Override
    public boolean allowedAtSea(final Fisher fisher, final FishState model, final int timeStep) {
        return delegate.allowedAtSea(fisher, model, timeStep);
    }

    @Override
    public void reactToFishing(
        final SeaTile where,
        final Fisher who,
        final Catch fishCaught,
        final Catch fishRetained,
        final int hoursSpentFishing,
        final FishState model,
        final int timeStep
    ) {
        delegate.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing, model, timeStep);
    }

    @Override
    public void reactToSale(
        final Species species,
        final Fisher seller,
        final double biomass,
        final double revenue,
        final FishState model,
        final int timeStep
    ) {
        delegate.reactToSale(species, seller, biomass, revenue, model, timeStep);
    }

}
