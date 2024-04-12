/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024-2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * this object exists to generate a new deployment action (probably to add to a plan)
 */
public class DeploymentPlannedActionGenerator
    extends DrawFromLocationValuePlannedActionGenerator<PlannedAction.Deploy> {

    /**
     * the time it takes for the boat to "recover" after a deployment; 0 means you can drop another immediately
     */
    private final double delayInHoursAfterADeployment;

    public DeploymentPlannedActionGenerator(
        final Fisher fisher,
        final LocationValues originalLocationValues,
        final NauticalMap map,
        final MersenneTwisterFast random
    ) {
        this(fisher, originalLocationValues, map, random, 0);
    }

    DeploymentPlannedActionGenerator(
        final Fisher fisher,
        final LocationValues originalLocationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double delayInHoursAfterADeployment
    ) {
        super(fisher, originalLocationValues, map, random);
        this.delayInHoursAfterADeployment = delayInHoursAfterADeployment;

    }

    @Override
    protected PlannedAction.Deploy locationToPlannedAction(final SeaTile location) {
        Preconditions.checkState(isReady(), "Did not start the deploy generator yet!");
        return new PlannedAction.Deploy(
            location,
            delayInHoursAfterADeployment
        );
    }

}
