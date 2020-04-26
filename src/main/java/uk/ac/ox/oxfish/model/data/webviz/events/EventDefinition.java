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

package uk.ac.ox.oxfish.model.data.webviz.events;

import java.util.Collection;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class EventDefinition {

    private final String description;
    private final Collection<Period> periods;

    EventDefinition(
        String description,
        Collection<Period> periods
    ) {
        this.description = description;
        this.periods = periods;
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    static class Period {

        private final int startTimeDays;
        private final int endTimeDays;

        Period(int startTimeDays, int endTimeDays) {
            this.startTimeDays = startTimeDays;
            this.endTimeDays = endTimeDays;
        }

    }

}
