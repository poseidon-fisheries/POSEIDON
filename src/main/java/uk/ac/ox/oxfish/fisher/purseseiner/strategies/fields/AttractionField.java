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

import static java.lang.Math.pow;

import java.util.Map.Entry;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableDouble2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

public class AttractionField implements FisherStartable {

    private final LocationValues locationValues;
    private final LocalAttractionModulator localModulator;
    private final GlobalAttractionModulator globalModulator;
    private final double actionDistanceExponent;
    private final double destinationDistanceExponent;
    private Fisher fisher;

    AttractionField(
        final LocationValues locationValues,
        final LocalAttractionModulator localModulator,
        final GlobalAttractionModulator globalModulator,
        final double actionDistanceExponent,
        final double destinationDistanceExponent
    ) {
        this.locationValues = locationValues;
        this.localModulator = localModulator;
        this.globalModulator = globalModulator;
        this.actionDistanceExponent = actionDistanceExponent;
        this.destinationDistanceExponent = destinationDistanceExponent;
    }

    public Double2D netAttractionHere() {

        final FishState fishState = fisher.grabState();
        final double speed = fisher.getBoat().getSpeedInKph();
        final Int2D here = fisher.getLocation().getGridLocation();

        assert speed > 0 : "boat speed must be > 0";
        assert fishState.getHoursPerStep() > 0 : "hours per step must be > 0";

        // going all mutable here in order to minimize object creations
        // this is a pretty tight loop in the simulation...

        final MutableDouble2D netAttraction = new MutableDouble2D();
        final MutableDouble2D locationAttraction = new MutableDouble2D();

        locationValues.getValues().forEach(entry -> {
            final Int2D there = entry.getKey();
            final double travelTime = distance(here, there) / speed;
            final int t = (int) (fishState.getStep() + travelTime / fishState.getHoursPerStep());
            locationAttraction.x = there.x - here.x;
            locationAttraction.y = there.y - here.y;
            if (locationAttraction.length() > 0) {
                locationAttraction.normalize().multiplyIn(
                    // scale to modulated "there" value, decreasing with travel time
                    entry.getValue() * localModulator.modulate(there.x, there.y, t, fisher)
                        / pow(travelTime, destinationDistanceExponent)
                );
            }
            netAttraction.addIn(locationAttraction);
        });

        if (netAttraction.length() > 0) {
            netAttraction.normalize().multiplyIn(globalModulator.modulate(fisher));
        }
        return new Double2D(netAttraction);

    }

    private double distance(final Int2D here, final Int2D there) {
        return fisher.grabState().getMap().distance(here, there);
    }

    public double getActionValueAt(final Int2D here) {
        return locationValues.getValues().stream()
            .mapToDouble(entry -> {
                final double distance = distance(here, entry.getKey());
                return entry.getValue() / pow(distance + 1, actionDistanceExponent);
            })
            .sum();
    }

    public double getValueAt(final Int2D location) {
        return locationValues.getValueAt(location);
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        this.fisher = fisher;
        locationValues.start(model, fisher);
    }

}
