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

package uk.ac.ox.oxfish.fisher.purseseiner.caches;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

/**
 * Caches based on this class store values loaded from a file and uniquely identified by:
 * <ul>
 *     <li>path of the file</li>
 *     <li>year</li>
 *     <li>string id of the boat</li>
 *     <li>action class</li>
 * </ul>
 * <p>
 * Currently implemented by {@link ActionWeightsCacheBoat}, where the values are doubles, and {@link LocationBoatValuesByActionCache},
 * where the values are maps from coordinates to doubles.
 *
 * @param <T> the type of the cached value
 */
public abstract class BoatValuesByActionFromFileCache<T>
    extends BoatValuesFromFileCache<Map<Class<? extends PurseSeinerAction>, T>> {

    private final Supplier<T> defaultValue;

    BoatValuesByActionFromFileCache(final Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
    }

    public T get(
        final Path valuesFile,
        final int targetYear,
        final Fisher fisher,
        Class<? extends PurseSeinerAction> actionClass
    ) {
        return super.get(valuesFile, targetYear, fisher)
            .orElse(emptyMap())
            .getOrDefault(actionClass, defaultValue.get());
    }

    /**
     * These enums are mostly used to convert strings to action classes
     * when loading data from CSV files, e.g:
     * <p>
     * {@code ActionClasses.valueOf(record.getString("event")).getActionClass()}
     * <p>
     * They could also be used to get the action class directly,
     * e.g. {@code OFS.getActionClass()} instead of writing
     * {@code OpportunisticFadSetAction.class}.
     */
    public enum ActionClasses {

        FAD(FadSetAction.class),
        DEL(DolphinSetAction.class),
        NOA(NonAssociatedSetAction.class),
        OFS(OpportunisticFadSetAction.class),
        DPL(FadDeploymentAction.class);

        private final Class<? extends PurseSeinerAction> actionClass;

        ActionClasses(final Class<? extends PurseSeinerAction> actionClass) {this.actionClass = actionClass;}

        public Class<? extends PurseSeinerAction> getActionClass() { return actionClass; }
    }

}
