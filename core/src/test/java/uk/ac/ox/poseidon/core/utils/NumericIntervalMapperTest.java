/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.core.utils;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.functions.NumericIntervalMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumericIntervalMapperTest {

    @Test
    void mapsUsingInclusiveLowerAndExclusiveUpperBounds() {
        final NumericIntervalMapper<String> mapper =
            new NumericIntervalMapper<>(List.of(
                new NumericIntervalMapper.Interval<>(0.0, 12.0, "VL0612"),
                new NumericIntervalMapper.Interval<>(12.0, 18.0, "VL1218")
            ));

        assertThat(mapper.apply(0.0)).isEqualTo("VL0612");
        assertThat(mapper.apply(11.999)).isEqualTo("VL0612");
        assertThat(mapper.apply(12.0)).isEqualTo("VL1218");
        assertThat(mapper.apply(17.999)).isEqualTo("VL1218");
        assertThat(mapper.apply(-0.001)).isNull();
        assertThat(mapper.apply(18.0)).isNull();
    }

    @Test
    void supportsIntervalsWithNoUpperBound() {
        final NumericIntervalMapper<String> mapper =
            new NumericIntervalMapper<>(List.of(
                new NumericIntervalMapper.Interval<>(24.0, 40.0, "VL2440"),
                new NumericIntervalMapper.Interval<>(40.0, null, "VL40XX")
            ));

        assertThat(mapper.apply(39.999)).isEqualTo("VL2440");
        assertThat(mapper.apply(40.0)).isEqualTo("VL40XX");
        assertThat(mapper.apply(500.0)).isEqualTo("VL40XX");
    }

    @Test
    void supportsIntervalsWithNoLowerBound() {
        final NumericIntervalMapper<String> mapper =
            new NumericIntervalMapper<>(List.of(
                new NumericIntervalMapper.Interval<>(null, 6.0, "VL0006"),
                new NumericIntervalMapper.Interval<>(6.0, 12.0, "VL0612")
            ));

        assertThat(mapper.apply(-500.0)).isEqualTo("VL0006");
        assertThat(mapper.apply(0.0)).isEqualTo("VL0006");
        assertThat(mapper.apply(5.999)).isEqualTo("VL0006");
        assertThat(mapper.apply(6.0)).isEqualTo("VL0612");
    }

    @Test
    void rejectsIntervalsWithInvertedBounds() {
        assertThatThrownBy(() ->
            new NumericIntervalMapper<>(List.of(
                new NumericIntervalMapper.Interval<>(12.0, 6.0, "invalid")
            ))
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("lower")
            .hasMessageContaining("upper");
    }

    @Test
    void rejectsZeroWidthIntervals() {
        assertThatThrownBy(() ->
            new NumericIntervalMapper<>(List.of(
                new NumericIntervalMapper.Interval<>(12.0, 12.0, "invalid")
            ))
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("lower")
            .hasMessageContaining("upper");
    }

    @Test
    void rejectsOverlappingIntervals() {
        assertThatThrownBy(() ->
            new NumericIntervalMapper<>(List.of(
                new NumericIntervalMapper.Interval<>(0.0, 12.0, "VL0612"),
                new NumericIntervalMapper.Interval<>(11.0, 18.0, "VL1218")
            ))
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("overlap");
    }

    @Test
    void rejectsIntervalsWithNoBounds() {
        assertThatThrownBy(() ->
            new NumericIntervalMapper<>(List.of(
                new NumericIntervalMapper.Interval<>(null, null, "invalid")
            ))
        ).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("bound");
    }
}
