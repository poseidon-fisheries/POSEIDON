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

package uk.ac.ox.oxfish.fisher.log.initializers;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

import java.util.function.Consumer;

/**
 * Any object whose job is to create "logbook" data of some kind. As of this writing the only kind of logbook
 * I can think of is for logistic fits  but maybe other kinds might be possible.
 * This object is not to write the logbook itself rather it is to set up all the objects that do as the model runs
 *
 * Created by carrknight on 2/17/17.
 */
public interface LogbookInitializer extends Startable
{



    void add(Fisher fisher, FishState state);




}
