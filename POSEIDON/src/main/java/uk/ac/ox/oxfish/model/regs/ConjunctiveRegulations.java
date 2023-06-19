package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;

/**
 * A regulation composed of multiple sub-regulations, and a simpler alternative to {@link MultipleRegulations}.
 * Fishing must be allowed by all sub-regulations to be allowed, and the maximum biomass sellable
 * is the minimum permitted by any of the sub-regulations.
 */
public class ConjunctiveRegulations implements Regulation {

    private final Collection<? extends Regulation> regulations;

    public ConjunctiveRegulations(final Collection<? extends Regulation> regulations) {
        this.regulations = regulations;
    }

    public Collection<? extends Regulation> getRegulations() {
        return regulations;
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        regulations.forEach(reg -> reg.start(model, fisher));
    }

    @Override
    public void turnOff(final Fisher fisher) {
        regulations.forEach(reg -> reg.turnOff(fisher));
    }

    @Override
    public boolean canFishHere(final Fisher agent, final SeaTile tile, final FishState model, final int timeStep) {
        return regulations
            .stream()
            .allMatch(regulation -> regulation.canFishHere(agent, tile, model, timeStep));
    }

    @Override
    public double maximumBiomassSellable(
        final Fisher agent,
        final Species species,
        final FishState model,
        final int timeStep
    ) {
        return regulations
            .stream()
            .mapToDouble(reg -> reg.maximumBiomassSellable(agent, species, model, timeStep))
            .min()
            .orElse(Double.MAX_VALUE);
    }

    @Override
    public boolean allowedAtSea(final Fisher fisher, final FishState model, final int timeStep) {
        return regulations
            .stream()
            .allMatch(reg -> reg.allowedAtSea(fisher, model, timeStep));
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
        regulations.forEach(reg ->
            reg.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing, model, timeStep)
        );
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
        regulations.forEach(reg ->
            reg.reactToSale(species, seller, biomass, revenue, model, timeStep)
        );
    }

    @Override
    public Regulation makeCopy() {
        return new ConjunctiveRegulations(ImmutableList.copyOf(regulations));
    }
}
