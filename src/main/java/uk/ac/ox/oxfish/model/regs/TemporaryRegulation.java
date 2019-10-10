package uk.ac.ox.oxfish.model.regs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a regulation that is only active for part of the year. This is a more general version
 * of the {@link TemporaryProtectedArea} class, with the following differences:
 * - The active delegate is a parameter and can be any kind of regulation, not just {@link ProtectedAreasOnly}.
 * - If startDay > endDay, the regulation is active after start day, through the end of year and before endDay from the beginning of year.
 * - The {@link TemporaryRegulation#reactToFishing(SeaTile, Fisher, Catch, Catch, int) reactToFishing} and
 * {@link TemporaryRegulation#reactToSale(Species, Fisher, double, double) reactToSale} methods are also delegated.
 * This class probably makes {@link TemporaryProtectedArea} obsolete and should be used for new code.
 */
public class TemporaryRegulation implements Regulation {

    private final int startDay;
    private final int endDay;
    private final Regulation activeDelegate;
    private final Anarchy inactiveDelegate = new Anarchy();
    // We keep a reference to the FishState because we always need to know
    // the current day in order to figure out the current delegate, but some
    // of the Regulation methods we implement do not pass the FishState in.
    @Nullable private FishState state = null;

    public TemporaryRegulation(int startDay, int endDay, Regulation activeDelegate) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.activeDelegate = activeDelegate;
    }

    public boolean isActive() {
        checkNotNull(state, "No reference to FishState; the model probably hasn't been started.");
        final int d = state.getDayOfTheYear();
        return startDay <= endDay ?
            d >= startDay && d <= endDay :
            d >= startDay || d <= endDay;
    }

    @NotNull public Regulation currentDelegate() { return isActive() ? activeDelegate : inactiveDelegate; }

    @Override public void turnOff(Fisher fisher) {
        state = null;
        activeDelegate.turnOff(fisher);
        inactiveDelegate.turnOff(fisher);
    }

    @Override public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        return currentDelegate().canFishHere(agent, tile, model);
    }

    @Override public double maximumBiomassSellable(Fisher agent, Species species, FishState model) {
        return currentDelegate().maximumBiomassSellable(agent, species, model);
    }

    @Override public boolean allowedAtSea(Fisher fisher, FishState model) {
        return currentDelegate().allowedAtSea(fisher, model);
    }

    @Override public void reactToFishing(SeaTile where, Fisher who, Catch fishCaught, Catch fishRetained, int hoursSpentFishing) {
        currentDelegate().reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing);
    }

    @Override public void reactToSale(Species species, Fisher seller, double biomass, double revenue) {
        currentDelegate().reactToSale(species, seller, biomass, revenue);
    }

    @NotNull @Override public Regulation makeCopy() {
        return new TemporaryRegulation(startDay, endDay, activeDelegate.makeCopy());
    }

    @Override public void start(FishState model, Fisher fisher) {
        state = model;
        activeDelegate.start(model, fisher);
        inactiveDelegate.start(model, fisher);
    }
}
