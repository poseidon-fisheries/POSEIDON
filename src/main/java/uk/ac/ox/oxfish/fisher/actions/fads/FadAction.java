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

import static java.lang.Math.floor;

public interface FadAction extends Action, FadManagerUtils {
    Optional<SeaTile> getActionTile(Fisher fisher);
    Quantity<Time> getDuration();
    boolean isPossible(FishState model, Fisher fisher);

    default boolean isAllowed(FishState model, Fisher fisher) {
        return isAllowed(model, fisher, fisher.getLocation(), model.getStep());
    }

    default boolean isAllowed(FishState model, Fisher fisher, SeaTile actionTile, int actionStep) {
        return fisher.isCheater() || fisher.getRegulation().canFishHere(fisher, actionTile, model, actionStep);
    }

    default String totalCounterName() { return "Total number of " + actionName(); }

    /**
     * Plural name of action, used to build counter names
     */
    String actionName();

    default String regionCounterName(NauticalMap map, SeaTile seaTile) {
        return "Number of " + actionName() + " (" + getRegionName(map, seaTile) + " region)";
    }

    default String getRegionName(NauticalMap map, SeaTile seaTile) {
        return getRegionName(getRegionNumber(map, seaTile));
    }

    default String getRegionName(int regionNumber) {
        switch (regionNumber) {
            case 11: return "Northwest";
            case 12: return "North";
            case 13: return "Northeast";
            case 21: return "West";
            case 22: return "Central";
            case 23: return "East";
            case 31: return "Southwest";
            case 32: return "South";
            case 33: return "Southeast";
        }
        return null;
    }

    default int getRegionNumber(NauticalMap map, SeaTile seaTile) {
        final double divisions = 3.0;
        final double regionWidth = map.getWidth() / divisions;
        final double regionHeight = map.getHeight() / divisions;
        return (int) ((1 + floor(seaTile.getGridX() / regionWidth)) * 10) +
            (int) (1 + floor(seaTile.getGridY() / regionHeight));
    }

}
