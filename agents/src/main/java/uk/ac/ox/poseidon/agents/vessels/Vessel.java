/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.vessels;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.geography.ports.Port;

@Getter
@Setter
public class Vessel implements Oriented2D {

    private final String id;
    private final VesselField vesselField;
    private Behaviour initialBehaviour;
    private Port homePort;
    private double cruisingSpeed;
    private double heading;

    public Vessel(
        final String id,
        @NonNull final Port homePort,
        final double cruisingSpeed,
        final VesselField vesselField
    ) {
        this.id = id;
        this.homePort = homePort;
        this.cruisingSpeed = cruisingSpeed;
        this.vesselField = vesselField;
    }

    @Override
    public double orientation2D() {
        return heading;
    }

    public void setHeadingTowards(
        final Int2D destinationCell
    ) {
        setHeadingTowards(getVesselField().getGridExtent().toPoint(destinationCell));
    }

    public void setHeadingTowards(
        final Double2D destinationPoint
    ) {
        final Double2D location = getCurrentPoint();
        final double dx = destinationPoint.x - location.x;
        final double dy = destinationPoint.y - location.y;
        setHeading(Math.atan2(dy, dx));
    }

    public Double2D getCurrentPoint() {
        return vesselField.getPoint(this);
    }

    public void setLocation(final Int2D cell) {
        vesselField.setCell(this, cell);
    }

    public void setLocation(final Double2D point) {
        vesselField.setPoint(this, point);
    }

    public Int2D getCurrentCell() {
        return vesselField.getCell(this);
    }
}
