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
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

import static java.lang.Math.pow;

public class AttractionField implements FisherStartable {

    private static final Double2D ZERO_VECTOR = new Double2D(0, 0);
    private final LocationValues locationValues;
    private final AttractionModulator modulator;

    AttractionField(
        final LocationValues locationValues,
        final AttractionModulator modulator
    ) {
        this.locationValues = locationValues;
        this.modulator = modulator;
    }

    public Double2D netAttraction(Int2D here, Fisher fisher, FishState fishState) {
        return locationValues
            .getValues()
            .filter(entry -> !entry.getKey().equals(here))
            .map(entry -> attraction(here, entry.getKey(), entry.getValue(), fisher, fishState))
            .reduce(Double2D::add)
            .filter(v -> !v.equals(ZERO_VECTOR)) // avoids crashing normalize
            .map(Double2D::normalize)
            .orElse(ZERO_VECTOR);
    }

    @NotNull
    private Double2D attraction(
        final Int2D here,
        final Int2D there,
        final double value,
        final Fisher fisher,
        final FishState fishState
    ) {
        final double distance = distance(fishState.getMap(), here, there);
        final double speed = fisher.getBoat().getSpeedInKph();
        final double travelTime = distance / speed;
        final int t = (int) (fishState.getStep() + travelTime / fishState.getHoursPerStep());
        final Double2D unitVector = new Double2D(there.x - here.x, there.y - here.y).normalize();
        final Double2D attractionVector = unitVector.multiply(value / pow(travelTime, 2));
        return attractionVector.multiply(modulator.modulate(there.x, there.y, t, fisher));
    }

    private double distance(NauticalMap map, Int2D here, Int2D there) {
        SeaTile tileHere = map.getSeaTile(here.x, here.y);
        SeaTile tileThere = map.getSeaTile(there.x, there.y);
        return map.getDistance().distance(tileHere, tileThere, map);
    }

    public double getValueAt(Int2D location) { return locationValues.getValueAt(location); }

    @Override public void start(final FishState model, final Fisher fisher) {
        locationValues.start(model, fisher);
    }

}
