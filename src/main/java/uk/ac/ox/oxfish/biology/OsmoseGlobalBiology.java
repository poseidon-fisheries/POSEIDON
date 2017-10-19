/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology;

import fr.ird.osmose.OsmoseSimulation;
import uk.ac.ox.oxfish.geography.osmose.OsmoseStepper;

/**
 * Global Biology with additionally a link to the OSMOSE simulation
 * Created by carrknight on 11/5/15.
 */
public class OsmoseGlobalBiology extends GlobalBiology {

    private final OsmoseSimulation simulation;

    private final OsmoseStepper stepper;


    public OsmoseGlobalBiology(OsmoseSimulation simulation,
                               OsmoseStepper stepper,
                               Species... species) {
        super(species);
        this.simulation = simulation;
        this.stepper = stepper;
    }


    public OsmoseSimulation getSimulation()
    {
        return simulation;
    }

    public OsmoseStepper getStepper() {
        return stepper;
    }
}
