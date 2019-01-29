package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * There are a few days a year where the agent is simply not allowed to go out.
 * Otherwise follow delegate
 */
public class ArbitraryPause implements Regulation {

    private final int startDay;

    private final int endDay;

    private final Regulation delegate;


    public ArbitraryPause(int startDay, int endDay, Regulation delegate) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.delegate = delegate;
    }

    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        return delegate.canFishHere(agent, tile, model);
    }

    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model) {
        return delegate.maximumBiomassSellable(agent, species, model);
    }

    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model) {
        if(fisher.isAtPortAndDocked() ) {
            int dayOfTheYear = model.getDayOfTheYear();
            if(dayOfTheYear>=startDay && dayOfTheYear<=endDay)
                return false;
        }
        return delegate.allowedAtSea(fisher, model);

    }

    @Override
    public void reactToFishing(SeaTile where, Fisher who, Catch fishCaught, Catch fishRetained, int hoursSpentFishing) {
        delegate.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing);
    }

    @Override
    public void reactToSale(Species species, Fisher seller, double biomass, double revenue) {
        delegate.reactToSale(species, seller, biomass, revenue);
    }

    @Override
    public Regulation makeCopy() {
        return delegate.makeCopy();
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model, fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        delegate.turnOff(fisher);
    }
}
