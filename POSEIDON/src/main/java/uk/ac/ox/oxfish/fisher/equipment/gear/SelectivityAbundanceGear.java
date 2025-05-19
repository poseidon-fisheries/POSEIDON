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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticAbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.RetentionAbundanceFilter;

/**
 * homogeneous gear that keeps track of its own retention and selectivity curve; this makes them easier to monitor
 */
public class SelectivityAbundanceGear extends HomogeneousAbundanceGear {


    private final FixedProportionFilter catchability;

    private final LogisticAbundanceFilter selectivity;

    private final RetentionAbundanceFilter retention;


    public SelectivityAbundanceGear(
        final double litersOfGasConsumedEachHourFishing,
        final FixedProportionFilter catchability,
        final LogisticAbundanceFilter selectivity
    ) {
        super(litersOfGasConsumedEachHourFishing, catchability, selectivity);
        this.catchability = catchability;
        this.selectivity = selectivity;
        this.retention = null;
    }

    public SelectivityAbundanceGear(
        final double litersOfGasConsumedEachHourFishing,
        final FixedProportionFilter catchability,
        final LogisticAbundanceFilter selectivity,
        final RetentionAbundanceFilter retention
    ) {
        super(litersOfGasConsumedEachHourFishing, catchability, selectivity, retention);
        this.catchability = catchability;
        this.selectivity = selectivity;
        this.retention = retention;
    }

    @Override
    public Gear makeCopy() {
        if (retention == null)
            return new SelectivityAbundanceGear(getLitersOfGasConsumedEachHourFishing(),
                catchability, selectivity
            );
        else
            return new SelectivityAbundanceGear(getLitersOfGasConsumedEachHourFishing(),
                catchability, selectivity, retention
            );
    }


    public double getaParameter() {
        return selectivity.getaParameter();
    }

    public double getbParameter() {
        return selectivity.getbParameter();
    }

    public double getCatchability() {
        return catchability.getProportion();
    }

    public FixedProportionFilter getCatchabilityFilter() {
        return catchability;
    }

    public LogisticAbundanceFilter getSelectivity() {
        return selectivity;
    }

    public RetentionAbundanceFilter getRetention() {
        return retention;
    }
}
