package uk.ac.ox.oxfish.fisher.actions.fads;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import static uk.ac.ox.oxfish.fisher.actions.fads.Regions.REGION_NAMES;
import static uk.ac.ox.oxfish.fisher.actions.fads.Regions.getRegionNumber;

public abstract class FadAction implements Action, FadManagerUtils {

    private final FishState model;
    private final Fisher fisher;
    private final SeaTile seaTile;
    private final int step;

    protected FadAction(FishState model, Fisher fisher) {
        this(model, fisher, fisher.getLocation(), model.getStep());
    }

    protected FadAction(FishState model, Fisher fisher, SeaTile seaTile, int step) {
        this.model = model;
        this.fisher = fisher;
        this.seaTile = seaTile;
        this.step = step;
    }

    public static String proportionGathererName(String actionName, int regionNumber) {
        return "Proportion of " + actionName + " (" + REGION_NAMES.get(regionNumber) + " region)";
    }

    public static String totalCounterName(String actionName) {
        return "Total number of " + actionName;
    }

    public FishState getModel() { return model; }

    public Fisher getFisher() { return fisher; }

    public SeaTile getSeaTile() { return seaTile; }

    public int getStep() { return step; }

    String regionCounterName() {
        return regionCounterName(getActionName(), getRegionNumber(model.getMap(), seaTile));
    }

    public static String regionCounterName(String actionName, int regionNumber) {
        return "Number of " + actionName + " (" + REGION_NAMES.get(regionNumber) + " region)";
    }

    /**
     * Plural name of action, used to build counter names
     */
    abstract String getActionName();

    public abstract Quantity<Time> getDuration();

    abstract boolean isPossible();

    public boolean isAllowed() {
        return fisher.isCheater() || fisher.getRegulation().canFishHere(fisher, seaTile, model, step);
    }

    String totalCounterName() {
        return "Total number of " + getActionName();
    }

    FadManager getFadManager() { return FadManagerUtils.getFadManager(fisher); }

    boolean isFadHere(Fad targetFad) {
        return getModel().getFadMap().getFadTile(targetFad)
            .filter(fadTile -> fadTile.equals(seaTile))
            .isPresent();
    }
}
