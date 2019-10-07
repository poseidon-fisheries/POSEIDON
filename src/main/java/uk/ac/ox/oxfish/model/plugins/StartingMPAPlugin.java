/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class StartingMPAPlugin implements AdditionalStartable {


    final  private List<StartingMPA> startingMPAs;


    public StartingMPAPlugin(List<StartingMPA> startingMPAs) {
        this.startingMPAs = startingMPAs;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {


        for(StartingMPA mpa : startingMPAs)
        {
            mpa.buildMPA(model.getMap());
        }

        model.getMap().recomputeTilesMPA();
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }
}
