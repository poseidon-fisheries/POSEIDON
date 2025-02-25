/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core.time;

import lombok.*;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.Duration;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DurationFactory extends GlobalScopeFactory<Duration> {

    public static final DurationFactory ONE_HOUR = new DurationFactory("PT1H");
    public static final DurationFactory ONE_DAY = new DurationFactory("P1D");

    private long days;
    private long hours;
    private long minutes;
    private long seconds;

    public DurationFactory(final String iso8601Duration) {
        final Duration duration = Duration.parse(iso8601Duration);
        days = duration.toDaysPart();
        hours = duration.toHoursPart();
        minutes = duration.toMinutesPart();
        seconds = duration.toSecondsPart();
    }

    @Override
    protected Duration newInstance(final @NonNull Simulation simulation) {
        return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }
}
