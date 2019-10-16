package uk.ac.ox.oxfish.model.regs;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Represents a regulation that is only active for part of the year. This is a more general version
 * of the {@link TemporaryProtectedArea} class, with the following differences:
 * - The active delegate is a parameter and can be any kind of regulation, not just {@link ProtectedAreasOnly}.
 * - If startDay > endDay, the regulation is active after start day, through the end of year and before endDay from the beginning of year.
 * - The {@link TemporaryRegulation#reactToFishing(SeaTile, Fisher, Catch, Catch, int, FishState, int) reactToFishing} and
 * {@link TemporaryRegulation#reactToSale(Species, Fisher, double, double, FishState, int) reactToSale} methods are also delegated.
 * This class probably makes {@link TemporaryProtectedArea} obsolete and should be used for new code.
 */
public class TemporaryRegulation implements Regulation {

    private final int startDay;
    private final int endDay;
    private final Regulation delegateWhenActive;
    private final Anarchy delegateWhenInactive = new Anarchy();

    public TemporaryRegulation(int startDay, int endDay, Regulation delegateWhenActive) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.delegateWhenActive = delegateWhenActive;
    }

    public boolean isActive(int dayOfTheYear) {
        assert dayOfTheYear >= 1 && dayOfTheYear <= 365;
        return startDay <= endDay ?
            dayOfTheYear >= startDay && dayOfTheYear <= endDay :
            dayOfTheYear >= startDay || dayOfTheYear <= endDay;
    }

    @NotNull public Regulation currentDelegate(FishState model, int timeStep) {
        return isActive(model.getDayOfTheYear(timeStep)) ? delegateWhenActive : delegateWhenInactive;
    }

    @Override public void turnOff(Fisher fisher) {
        delegateWhenActive.turnOff(fisher);
        delegateWhenInactive.turnOff(fisher);
    }

    @Override public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) {
        return currentDelegate(model, timeStep).canFishHere(agent, tile, model, timeStep);
    }

    @Override public double maximumBiomassSellable(Fisher agent, Species species, FishState model, int timeStep) {
        return currentDelegate(model, timeStep).maximumBiomassSellable(agent, species, model, timeStep);
    }

    @Override public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) {
        return currentDelegate(model, timeStep).allowedAtSea(fisher, model, timeStep);
    }

    @Override public void reactToFishing(
        SeaTile where, Fisher who, Catch fishCaught, Catch fishRetained,
        int hoursSpentFishing, FishState model, int timeStep
    ) {
        currentDelegate(model, timeStep).reactToFishing(
            where, who, fishCaught, fishRetained, hoursSpentFishing, model, timeStep
        );
    }

    @Override public void reactToSale(
        Species species, Fisher seller, double biomass, double revenue, FishState model, int timeStep
    ) {
        currentDelegate(model, timeStep).reactToSale(species, seller, biomass, revenue, model, timeStep);
    }

    @NotNull @Override public Regulation makeCopy() {
        return new TemporaryRegulation(startDay, endDay, delegateWhenActive.makeCopy());
    }

    @Override public void start(FishState model, Fisher fisher) {
        delegateWhenActive.start(model, fisher);
        delegateWhenInactive.start(model, fisher);
    }
}
