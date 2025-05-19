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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class YearlyActionCount implements ComponentFactory<Quantity> {

    private StringParameter actionCode;

    public YearlyActionCount() {
    }

    public YearlyActionCount(final String actionCode) {
        this(new StringParameter(actionCode));
    }

    public YearlyActionCount(final StringParameter actionCode) {
        this.actionCode = actionCode;
    }

    public StringParameter getActionCode() {
        return actionCode;
    }

    public void setActionCode(final StringParameter actionCode) {
        this.actionCode = actionCode;
    }

    @Override
    public Quantity apply(final ModelState modelState) {
        checkArgument(modelState instanceof FishState);
        final FishState fishState = (FishState) modelState;
        final String actionCodeValue = actionCode.getValue();
        return action -> {
            checkArgument(action instanceof Getter);
            return ((Getter) action).getYearlyActionCount(getYear(fishState), actionCodeValue);
        };
    }

    abstract int getYear(final FishState fishState);

    public interface Getter {
        long getYearlyActionCount(
            int year,
            String actionCode
        );
    }
}
