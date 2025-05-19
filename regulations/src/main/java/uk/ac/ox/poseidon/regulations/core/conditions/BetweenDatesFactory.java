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

package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.DateParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.LocalDate;

public class BetweenDatesFactory implements ComponentFactory<Condition> {

    private DateParameter beginningDate;
    private DateParameter endDate;

    @SuppressWarnings("unused")
    public BetweenDatesFactory() {
    }

    public BetweenDatesFactory(
        final LocalDate beginningDate,
        final LocalDate endDate
    ) {
        this(
            new DateParameter(beginningDate),
            new DateParameter(endDate)
        );
    }

    public BetweenDatesFactory(
        final DateParameter beginningDate,
        final DateParameter endDate
    ) {
        this.beginningDate = beginningDate;
        this.endDate = endDate;
    }

    public DateParameter getBeginningDate() {
        return beginningDate;
    }

    public void setBeginningDate(final DateParameter beginningDate) {
        this.beginningDate = beginningDate;
    }

    public DateParameter getEndDate() {
        return endDate;
    }

    public void setEndDate(final DateParameter endDate) {
        this.endDate = endDate;
    }

    @Override
    public Condition apply(final ModelState ignored) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.BetweenDates(
            beginningDate.getValue(),
            endDate.getValue()
        );
    }
}
