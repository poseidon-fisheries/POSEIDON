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

package uk.ac.ox.oxfish.model.data.monitors;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.KsTestAccumulator;

import javax.measure.quantity.Dimensionless;
import java.util.Map;

import static tech.units.indriya.AbstractUnit.ONE;
import static uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy.EVERY_YEAR;

public class CatchSizeDistributionMonitor<A extends AbstractSetAction>
    extends AbstractMonitor<AbstractSetAction, Double, Dimensionless> {

    private static final long serialVersionUID = 2860152553029319065L;
    private final Class<A> actionClass;
    private final Species species;

    public CatchSizeDistributionMonitor(
        final Class<A> actionClass,
        final Species species,
        final Map<Integer, double[]> empiricalDistributionPerYear
    ) {
        super(
            species.getName() + " catches from " + ActionClass.classMap.get(actionClass).name() + " sets",
            EVERY_YEAR,
            () -> new KsTestAccumulator(empiricalDistributionPerYear),
            ONE,
            "Kolmogorov-Smirnov test statistic"
        );
        this.actionClass = actionClass;
        this.species = species;
    }

    @Override
    public Iterable<Double> extractValues(final AbstractSetAction observable) {
        if (actionClass.isInstance(observable)) {
            return ImmutableList.of(
                observable
                    .getCatchesKept()
                    .map(catchesKept -> catchesKept.getWeightCaught(species))
                    .orElse(0.0)
            );
        } else {
            return ImmutableList.of();
        }
    }
}
