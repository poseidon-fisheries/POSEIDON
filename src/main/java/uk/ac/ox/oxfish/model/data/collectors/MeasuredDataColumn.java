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

package uk.ac.ox.oxfish.model.data.collectors;

import javax.measure.Quantity;
import javax.measure.Unit;

import static com.google.common.base.Preconditions.checkNotNull;

public class MeasuredDataColumn<Q extends Quantity<Q>> extends DataColumn {

    private final Unit<Q> unit;
    private final String yLabel;

    public MeasuredDataColumn(final String name, final Unit<Q> unit, final String yLabel) {
        super(name);
        this.unit = checkNotNull(unit);
        this.yLabel = checkNotNull(yLabel);
    }

    public Unit<Q> getUnit() { return unit; }

    public String getYLabel() { return yLabel; }

}
