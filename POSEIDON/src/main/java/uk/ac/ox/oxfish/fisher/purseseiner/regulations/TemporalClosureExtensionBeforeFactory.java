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

package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.core.conditions.AgentHasTagFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.AllOfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.BetweenYearlyDatesFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.FalseFactory;

import java.time.MonthDay;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;
import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.TemporalClosure.forbidDeploymentsBefore;

public class TemporalClosureExtensionBeforeFactory implements ComponentFactory<Condition> {
    private TemporalClosure originalClosure;
    private IntegerParameter numberOfDaysToExtend;

    @SuppressWarnings("unused")
    public TemporalClosureExtensionBeforeFactory() {
    }

    @SuppressWarnings("WeakerAccess")
    public TemporalClosureExtensionBeforeFactory(
        final TemporalClosure originalClosure,
        final int numberOfDaysToExtend
    ) {
        this(originalClosure, new IntegerParameter(numberOfDaysToExtend));
    }

    @SuppressWarnings("WeakerAccess")
    public TemporalClosureExtensionBeforeFactory(
        final TemporalClosure originalClosure,
        final IntegerParameter numberOfDaysToExtend
    ) {
        checkArgument(numberOfDaysToExtend.getIntValue() >= 1);
        this.originalClosure = originalClosure;
        this.numberOfDaysToExtend = numberOfDaysToExtend;
    }

    @SuppressWarnings("unused")
    public TemporalClosure getOriginalClosure() {
        return originalClosure;
    }

    @SuppressWarnings("unused")
    public void setOriginalClosure(final TemporalClosure originalClosure) {
        this.originalClosure = originalClosure;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getNumberOfDaysToExtend() {
        return numberOfDaysToExtend;
    }

    @SuppressWarnings("unused")
    public void setNumberOfDaysToExtend(final IntegerParameter numberOfDaysToExtend) {
        checkArgument(numberOfDaysToExtend.getIntValue() >= 1);
        this.numberOfDaysToExtend = numberOfDaysToExtend;
    }

    @Override
    public Condition apply(final ModelState modelState) {
        final MonthDay newBeginning = addDays(originalClosure.beginning(), -numberOfDaysToExtend.getIntValue());
        final int daysOfForbiddenDeployments = originalClosure.getDaysToForbidDeploymentsBefore().getIntValue();
        return new AllOfFactory(
            new AgentHasTagFactory(originalClosure.getAgentTag().getValue()),
            new AllOfFactory(
                daysOfForbiddenDeployments >= 1
                    ? forbidDeploymentsBefore(newBeginning, daysOfForbiddenDeployments)
                    : new FalseFactory(),
                new BetweenYearlyDatesFactory(
                    newBeginning,
                    addDays(originalClosure.beginning(), -1)
                )
            )
        ).apply(modelState);
    }

}
