/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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
package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.MTFApache;
import uk.ac.ox.poseidon.common.api.Observer;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;

/**
 * this object exists to draw a location given a locationvalues object and then turn that location into a planned action
 * of any sort
 */
public abstract class DrawFromLocationValuePlannedActionGenerator<PA extends PlannedAction>
    implements Observer<LocationValues> {

    protected final NauticalMap map;
    private final Fisher fisher;
    /**
     * the rng to use (compatible with Apache)
     */
    private final MTFApache localRng;
    /**
     * here I use Nic's object on location values to use the whole reading toolchain; in practice however all we need
     * here is a mapping coords --> weight
     */
    private final LocationValues originalLocationValues;
    private boolean ready = false;
    private EnumeratedDistribution<SeaTile> seaTilePicker;

    private boolean nearbyLocationsHaveBeenTried = false;

    DrawFromLocationValuePlannedActionGenerator(
        final Fisher fisher,
        final LocationValues originalLocationValues,
        final NauticalMap map,
        final MersenneTwisterFast rng
    ) {
        this.fisher = checkNotNull(fisher);
        this.originalLocationValues = checkNotNull(originalLocationValues);
        this.map = checkNotNull(map);
        this.localRng = new MTFApache(checkNotNull(rng));
    }

    public void init() {
        originalLocationValues.getObservers().register(LocationValues.class, this);
        nearbyLocationsHaveBeenTried = false;
        seaTilePicker = pickerFromPmf(pmfFromLocationValues(originalLocationValues));
        ready = true;
    }

    private EnumeratedDistribution<SeaTile> pickerFromPmf(List<Pair<SeaTile, Double>> pmf) {
        if (pmf.isEmpty() && !nearbyLocationsHaveBeenTried) {
            nearbyLocationsHaveBeenTried = true;
            pmf = pmfFromNearbyLocations();
        }
        return pmf.isEmpty()
            ? null
            : new EnumeratedDistribution<>(localRng, pmf);
    }

    private List<Pair<SeaTile, Double>> pmfFromLocationValues(final LocationValues locationValues) {
        return locationValues
            .getValues()
            .stream()
            .filter(entry -> entry.getValue() > 0)
            .map(entry -> new Pair<>(map.getSeaTile(entry.getKey()), entry.getValue()))
            .collect(toList());
    }

    private List<Pair<SeaTile, Double>> pmfFromNearbyLocations() {
        return map
            .getAllSeaTilesExcludingLandAsList()
            .stream()
            .map(seaTile -> new Pair<>(seaTile, 1 / (1 + map.distance(fisher.getLocation(), seaTile))))
            .sorted(comparingDouble(pair -> 0.0 - pair.getValue()))
            .limit(500)
            .collect(toList());
    }

    /**
     * Draw location values until we find one that can generate a legal action, or we run out of possibilities (in which
     * case we return null).
     */
    public PA drawNewPlannedAction() {
        PA plannedAction = null;
        while (plannedAction == null && seaTilePicker != null) {
            plannedAction = locationToPlannedAction(seaTilePicker.sample());
            if (!plannedAction.isAllowedNow(fisher)) {
                removeLocation(plannedAction.getLocation());
                plannedAction = null;
            }
        }
        return plannedAction;
    }

    protected abstract PA locationToPlannedAction(SeaTile location);

    public void removeLocation(final SeaTile location) {
        seaTilePicker = pickerFromPmf(removeSeaTileFromPmf(seaTilePicker.getPmf(), location));
    }

    private List<Pair<SeaTile, Double>> removeSeaTileFromPmf(
        final Collection<? extends Pair<SeaTile, Double>> pmf,
        final SeaTile seaTile
    ) {
        return pmf
            .stream()
            .filter(pair -> pair.getKey() != seaTile)
            .collect(toList());
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    public void observe(final LocationValues locationValues) {
        seaTilePicker = pickerFromPmf(pmfFromLocationValues(originalLocationValues));
    }

}
