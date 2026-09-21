/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.core.time;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.time.Duration;

/**
 * A {@link GlobalScopeFactory} for a fixed {@link Duration}. No separate plain component class
 * here: the produced value is a bare JDK type, with no wrapper to carry documentation. Built via
 * {@link Factories} — {@code duration(...)}, {@code days/hours/minutes/seconds(long)}, or the
 * {@code ONE_SECOND}/{@code ONE_MINUTE}/{@code ONE_HOUR}/{@code ONE_DAY} constants.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DurationFactory extends GlobalScopeFactory<Duration> {

    private long days;
    private long hours;
    private long minutes;
    private long seconds;

    /**
     * @param duration the duration to decompose into days/hours/minutes/seconds
     */
    public DurationFactory(final Duration duration) {
        days = duration.toDaysPart();
        hours = duration.toHoursPart();
        minutes = duration.toMinutesPart();
        seconds = duration.toSecondsPart();
    }

    /**
     * @param iso8601Duration an ISO-8601 duration string (e.g. {@code "PT1H30M"}), as accepted by
     *                        {@link Duration#parse}
     */
    public DurationFactory(final String iso8601Duration) {
        this(Duration.parse(iso8601Duration));
    }

    @Override
    protected Duration newInstance(final Scope scope) {
        return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }
}
