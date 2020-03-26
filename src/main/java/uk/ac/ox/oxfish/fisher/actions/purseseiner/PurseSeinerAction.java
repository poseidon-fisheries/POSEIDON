/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.actions.purseseiner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import static uk.ac.ox.oxfish.fisher.actions.purseseiner.Regions.REGION_NAMES;
import static uk.ac.ox.oxfish.fisher.actions.purseseiner.Regions.getRegionNumber;

public abstract class PurseSeinerAction implements Action, FadManagerUtils {

    private final FishState model;
    private final Fisher fisher;
    private final SeaTile seaTile;
    private final int step;

    protected PurseSeinerAction(FishState model, Fisher fisher) {
        this(model, fisher, fisher.getLocation(), model.getStep());
    }

    protected PurseSeinerAction(FishState model, Fisher fisher, SeaTile seaTile, int step) {
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

    public final boolean canHappen() { return isPossible() && (fisher.isCheater() || isAllowed()); }

    abstract boolean isPossible();

    public boolean isAllowed() {
        return !isForbidden() && fisher.getRegulation().canFishHere(fisher, seaTile, model, step);
    }

    public boolean isForbidden() {
        return getFadManager().getActionSpecificRegulations().isForbidden(this);
    }

    public FadManager getFadManager() { return FadManagerUtils.getFadManager(fisher); }

    String totalCounterName() {
        return "Total number of " + getActionName();
    }

    boolean isFadHere(Fad targetFad) {
        return getModel().getFadMap().getFadTile(targetFad)
            .filter(fadTile -> fadTile.equals(seaTile))
            .isPresent();
    }

    public FishState getModel() { return model; }
}
