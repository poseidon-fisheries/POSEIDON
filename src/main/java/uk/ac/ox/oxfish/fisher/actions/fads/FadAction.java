package uk.ac.ox.oxfish.fisher.actions.fads;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import javax.measure.Quantity;
import javax.measure.quantity.Time;
import java.util.Optional;

import static uk.ac.ox.oxfish.fisher.actions.fads.Regions.REGION_NAMES;
import static uk.ac.ox.oxfish.fisher.actions.fads.Regions.getRegionNumber;

public abstract class FadAction implements Action, FadManagerUtils {

    /**
     * Plural name of action, used to build counter names
     */
    abstract String getActionName();

    public static String regionCounterName(String actionName, int regionNumber) {
        return "Number of " + actionName + " (" + REGION_NAMES.get(regionNumber) + " region)";
    }

    public static String proportionGathererName(String actionName, int regionNumber) {
        return "Proportion of " + actionName + " (" + REGION_NAMES.get(regionNumber) + " region)";
    }

    public static String totalCounterName(String actionName) {
        return "Total number of " + actionName;
    }

    String regionCounterName(NauticalMap map, SeaTile seaTile) {
        return regionCounterName(getActionName(), getRegionNumber(map, seaTile));
    }

    abstract Optional<SeaTile> getActionTile(Fisher fisher);

    public abstract Quantity<Time> getDuration();

    abstract boolean isPossible(FishState model, Fisher fisher);

    public boolean isAllowed(FishState model, Fisher fisher) {
        return isAllowed(model, fisher, fisher.getLocation(), model.getStep());
    }

    public boolean isAllowed(FishState model, Fisher fisher, SeaTile actionTile, int actionStep) {
        return fisher.isCheater() || fisher.getRegulation().canFishHere(fisher, actionTile, model, actionStep);
    }

    String totalCounterName() {
        return "Total number of " + getActionName();
    }

}
