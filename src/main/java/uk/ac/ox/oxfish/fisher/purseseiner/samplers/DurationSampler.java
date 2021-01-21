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

package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import uk.ac.ox.oxfish.utility.MTFApache;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;

public class DurationSampler {

    private final LogNormalDistribution logNormalDistribution;

    public DurationSampler(
        final MersenneTwisterFast rng,
        final double meanLogDurationInHours,
        final double standardDeviationLogInHours
    ) {
        this.logNormalDistribution = new LogNormalDistribution(
            new MTFApache(rng),
            meanLogDurationInHours,
            standardDeviationLogInHours
        );
    }

    public Quantity<Time> nextDuration() {
        return getQuantity(logNormalDistribution.sample(), HOUR);
    }

}
