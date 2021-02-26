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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import org.jetbrains.annotations.NotNull;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Math.pow;

public class AttractionField implements FisherStartable {

    private static final Double2D ZERO_VECTOR = new Double2D(0, 0);
    private final LocationValues locationValues;
    private final LocalAttractionModulator localModulator;
    private final GlobalAttractionModulator globalModulator;
    private Fisher fisher;

    AttractionField(
        final LocationValues locationValues,
        final LocalAttractionModulator localModulator,
        final GlobalAttractionModulator globalModulator
    ) {
        this.locationValues = locationValues;
        this.localModulator = localModulator;
        this.globalModulator = globalModulator;
    }

    public Double2D netAttractionHere() {
        return locations(fisher.getLocation().getGridLocation())
            .filter(location -> location.distance > 0)
            .map(this::attraction)
            .reduce(Double2D::add)
            .filter(v -> v.length() > 0) // because very small vectors get length 0 and become infinite when normalized
            .map(v -> v.normalize().multiply(globalModulator.modulate(fisher)))
            .orElse(ZERO_VECTOR);
    }

    public Stream<Location> locations(Int2D here) {
        return locationValues
            .getValues()
            .map(entry -> new Location(entry.getKey(), entry.getValue(), distance(here, entry.getKey())));
    }

    @NotNull
    Double2D attraction(final Location location) {
        final FishState fishState = fisher.grabState();
        final double speed = fisher.getBoat().getSpeedInKph();
        final Int2D here = fisher.getLocation().getGridLocation();
        final Int2D there = location.gridLocation;
        checkState(speed > 0, "boat speed must be > 0");
        checkState(fishState.getHoursPerStep() > 0, "hour per step must be > 0");
        final double travelTime = location.distance / speed;
        final int t = (int) (fishState.getStep() + travelTime / fishState.getHoursPerStep());

        return new Double2D(there.x - here.x, there.y - here.y)
            .normalize() // normalized direction vector
            .multiply(
                // scale to modulated location value, decreasing with travel time
                location.value * localModulator.modulate(there.x, there.y, t, fisher)
                    / pow(travelTime, 2)
            );
    }

    private double distance(Int2D here, Int2D there) {
        return fisher.grabState().getMap().distance(here, there);
    }

    public double getActionValueAt(Int2D here) {
        return locations(here)
            .mapToDouble(loc -> loc.value / pow(loc.distance + 1, 2))
            .sum();
    }

    public double getValueAt(Int2D location) {
        return locationValues.getValueAt(location);
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        this.fisher = fisher;
        locationValues.start(model, fisher);
    }

    static class Location {
        private final Int2D gridLocation;
        private final double value;
        private final double distance;

        public Location(Int2D gridLocation, double value, double distance) {
            this.gridLocation = gridLocation;
            this.value = value;
            this.distance = distance;
        }
    }

}
