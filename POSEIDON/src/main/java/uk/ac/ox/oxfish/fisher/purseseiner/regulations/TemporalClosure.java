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
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.ForbiddenIfFactory;
import uk.ac.ox.poseidon.regulations.core.conditions.*;

import java.time.MonthDay;
import java.util.List;

import static uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations.addDays;

public class TemporalClosure implements ComponentFactory<Regulations>, YearsActive {
    private List<Integer> yearsActive;
    private StringParameter agentTag;
    private IntegerParameter beginningDay;
    private IntegerParameter beginningMonth;
    private IntegerParameter endDay;
    private IntegerParameter endMonth;
    private IntegerParameter daysToForbidDeploymentsBefore;

    @SuppressWarnings("unused")
    public TemporalClosure() {
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public TemporalClosure(
        final List<Integer> yearsActive,
        final String agentTag,
        final MonthDay beginning,
        final MonthDay end,
        final int daysToForbidDeploymentsBefore
    ) {
        this(
            yearsActive,
            new StringParameter(agentTag),
            new IntegerParameter(beginning.getDayOfMonth()),
            new IntegerParameter(beginning.getMonthValue()),
            new IntegerParameter(end.getDayOfMonth()),
            new IntegerParameter(end.getMonthValue()),
            new IntegerParameter(daysToForbidDeploymentsBefore)
        );
    }

    @SuppressWarnings("WeakerAccess")
    public TemporalClosure(
        final List<Integer> yearsActive,
        final StringParameter agentTag,
        final IntegerParameter beginningDay,
        final IntegerParameter beginningMonth,
        final IntegerParameter endDay,
        final IntegerParameter endMonth,
        final IntegerParameter daysToForbidDeploymentsBefore
    ) {
        this.agentTag = agentTag;
        this.yearsActive = yearsActive;
        this.beginningDay = beginningDay;
        this.beginningMonth = beginningMonth;
        this.endDay = endDay;
        this.endMonth = endMonth;
        this.daysToForbidDeploymentsBefore = daysToForbidDeploymentsBefore;
    }

    @SuppressWarnings("unused")
    public List<Integer> getYearsActive() {
        return yearsActive;
    }

    @SuppressWarnings("unused")
    public void setYearsActive(final List<Integer> yearsActive) {
        this.yearsActive = yearsActive;
    }

    @SuppressWarnings("WeakerAccess")
    public StringParameter getAgentTag() {
        return agentTag;
    }

    @SuppressWarnings("unused")
    public void setAgentTag(final StringParameter agentTag) {
        this.agentTag = agentTag;
    }

    @SuppressWarnings("WeakerAccess")
    public IntegerParameter getDaysToForbidDeploymentsBefore() {
        return daysToForbidDeploymentsBefore;
    }

    @SuppressWarnings("unused")
    public void setDaysToForbidDeploymentsBefore(final IntegerParameter daysToForbidDeploymentsBefore) {
        this.daysToForbidDeploymentsBefore = daysToForbidDeploymentsBefore;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getBeginningDay() {
        return beginningDay;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBeginningDay(final IntegerParameter beginningDay) {
        this.beginningDay = beginningDay;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getBeginningMonth() {
        return beginningMonth;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBeginningMonth(final IntegerParameter beginningMonth) {
        this.beginningMonth = beginningMonth;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getEndDay() {
        return endDay;
    }

    @SuppressWarnings("WeakerAccess")
    public void setEndDay(final IntegerParameter endDay) {
        this.endDay = endDay;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getEndMonth() {
        return endMonth;
    }

    @SuppressWarnings("WeakerAccess")
    public void setEndMonth(final IntegerParameter endMonth) {
        this.endMonth = endMonth;
    }

    public void setEnd(final MonthDay monthDay) {
        setEndMonth(new IntegerParameter(monthDay.getMonthValue()));
        setEndDay(new IntegerParameter(monthDay.getDayOfMonth()));
    }

    public void setBeginning(final MonthDay monthDay) {
        setBeginningMonth(new IntegerParameter(monthDay.getMonthValue()));
        setBeginningDay(new IntegerParameter(monthDay.getDayOfMonth()));
    }

    @Override
    public Regulations apply(final ModelState modelState) {
        final MonthDay beginning = beginning();
        return new ForbiddenIfFactory(
            new AllOfFactory(
                new AgentHasTagFactory(agentTag.getValue()),
                new AnyOfFactory(yearsActive.stream().map(InYearFactory::new)),
                new AnyOfFactory(
                    daysToForbidDeploymentsBefore.getIntValue() >= 1
                        ? forbidDeploymentsBefore(beginning, daysToForbidDeploymentsBefore.getIntValue())
                        : new FalseFactory(),
                    new BetweenYearlyDatesFactory(beginning, end())
                )
            )
        ).apply(modelState);
    }

    public MonthDay beginning() {
        return makeMonthDay(beginningMonth, beginningDay);
    }

    static AllOfFactory forbidDeploymentsBefore(
        final MonthDay beginning,
        final int numDays
    ) {
        return new AllOfFactory(
            new ActionCodeIsFactory("DPL"),
            new BetweenYearlyDatesFactory(
                addDays(beginning, -numDays),
                addDays(beginning, -1)
            )
        );
    }

    public MonthDay end() {
        return makeMonthDay(endMonth, endDay);
    }

    private static MonthDay makeMonthDay(
        final IntegerParameter month,
        final IntegerParameter day
    ) {
        return MonthDay.of(month.getIntValue(), day.getIntValue());
    }
}
