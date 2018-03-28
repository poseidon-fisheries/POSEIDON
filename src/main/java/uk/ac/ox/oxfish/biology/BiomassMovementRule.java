/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import uk.ac.ox.oxfish.biology.complicated.AbundanceBasedLocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

/**
 * Marker for all diffuser objects that deal with biomass
 * Created by carrknight on 7/6/17.
 */
public interface BiomassMovementRule {

    /**
     decide how much fish moves and use the setters to make it so
     */
    public void move(Species species,
                     SeaTile here,
                     double biomassHere,
                     SeaTile there,
                     double biomassThere,
                     double delta,
                     double carryingCapacityHere,
                     double carryingCapacityThere,

                     BiomassLocalBiology biologyHere,
                     BiomassLocalBiology biologyThere
    );




}
