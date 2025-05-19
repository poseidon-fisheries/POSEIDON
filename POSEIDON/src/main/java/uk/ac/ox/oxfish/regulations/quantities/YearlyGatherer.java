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

package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class YearlyGatherer implements ComponentFactory<Quantity> {

    private StringParameter name;

    @SuppressWarnings("unused")
    public YearlyGatherer() {
    }

    public YearlyGatherer(final String name) {
        this.name = new StringParameter(name);
    }

    public YearlyGatherer(final StringParameter name) {
        this.name = name;
    }

    public StringParameter getName() {
        return name;
    }

    public void setName(final StringParameter name) {
        this.name = name;
    }

    @Override
    public Quantity apply(final ModelState ignored) {
        // store name in local variable because we don't
        // want to close over the mutable StringParameter
        final String gathererName = name.getValue();
        return action -> {
            final FishState fishState =
                ((Fisher) action.getAgent()).grabState();
            return fishState
                .getYearlyDataSet()
                .getGatherer(gathererName)
                .apply(fishState);
        };
    }

}
