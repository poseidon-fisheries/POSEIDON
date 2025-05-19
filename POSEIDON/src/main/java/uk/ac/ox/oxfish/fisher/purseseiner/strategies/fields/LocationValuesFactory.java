/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.Map;

public abstract class LocationValuesFactory implements AlgorithmFactory<LocationValueByActionClass> {
    private static final LocationFisherValuesByActionCache locationValuesCache =
        new LocationFisherValuesByActionCache();
    private InputPath locationValuesFile;
    private IntegerParameter targetYear;

    @SuppressWarnings("WeakerAccess")
    public LocationValuesFactory() {
    }

    public LocationValuesFactory(
        final InputPath locationValuesFile,
        final IntegerParameter targetYear
    ) {
        this.locationValuesFile = locationValuesFile;
        this.targetYear = targetYear;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    @SuppressWarnings("unused")
    public InputPath getLocationValuesFile() {
        return locationValuesFile;
    }

    @SuppressWarnings("unused")
    public void setLocationValuesFile(final InputPath locationValuesFile) {
        this.locationValuesFile = locationValuesFile;
    }

    Map<Int2D, Double> loadLocationValues(
        final Fisher fisher,
        final Class<? extends PurseSeinerAction> actionClass
    ) {
        return locationValuesCache.getLocationValues(
            locationValuesFile.get(),
            targetYear.getValue(),
            fisher,
            actionClass
        );
    }
}
