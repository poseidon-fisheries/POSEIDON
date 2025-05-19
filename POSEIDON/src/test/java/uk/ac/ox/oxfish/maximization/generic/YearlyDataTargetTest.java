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

package uk.ac.ox.oxfish.maximization.generic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.nio.file.Paths;

import static org.mockito.Mockito.*;

public class YearlyDataTargetTest {

    @Test
    public void zeroError() {
        final YearlyDataTarget target = new YearlyDataTarget(
            Paths.get("inputs", "tests", "landings3.csv").toString(),
            "fakeData",
            true,
            1,
            1,
            false
        );

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final DataColumn fakeData = new DataColumn("fakeData");
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d);
        fakeData.add(100d);

        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        Assertions.assertEquals(target.computeError(model), 0, .0001);

    }

    @Test
    public void tenError() {
        final YearlyDataTarget target = new YearlyDataTarget(
            Paths.get("inputs", "tests", "landings3.csv").toString(),
            "fakeData",
            true,
            1,
            1,
            false
        );

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final DataColumn fakeData = new DataColumn("fakeData");
        fakeData.add(90d);
        fakeData.add(90d);
        fakeData.add(90d);
        fakeData.add(90d);

        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        Assertions.assertEquals(target.computeError(model), .1, .0001);

        //if you decrease coefficient of variation, you will weigh your error more!
        target.setCoefficientOfVariation(.1);
        Assertions.assertEquals(target.computeError(model), 1, .0001);


    }

    @Test
    public void nonSTD() {
        final YearlyDataTarget target = new YearlyDataTarget(
            Paths.get("inputs", "tests", "landings2.csv").toString(),
            "fakeData",
            false,
            -1,
            1,
            false
        );

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final DataColumn fakeData = new DataColumn("fakeData");
        final double[] input = {324214,
            324215,
            324216,
            324217,
            324218,
            324219,
            324220,
            324221,
            324222,
            324223,
            324224,
            324225,
            324226,
            324227,
            0};
        for (final double toAdd : input) {
            fakeData.add(toAdd);
        }


        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        Assertions.assertEquals(target.computeError(model), 324228 / 15d, .0001);

        //if you decrease coefficient of variation, you will weigh your error more!
        target.setCoefficientOfVariation(Double.NaN);
        Assertions.assertEquals(target.computeError(model), 324228 / 15d, .0001);

        target.setCoefficientOfVariation(0);
        Assertions.assertEquals(target.computeError(model), 324228 / 15d, .0001);

    }

    @Test
    public void squared() {
        final YearlyDataTarget target = new YearlyDataTarget(
            Paths.get("inputs", "tests", "landings2.csv").toString(),
            "fakeData",
            false,
            -1,
            2,
            false
        );

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final DataColumn fakeData = new DataColumn("fakeData");
        final double[] input = {324214,
            324215,
            324216,
            324217,
            324218,
            324219,
            324220,
            324221,
            324222,
            324223,
            324224,
            324225,
            324226,
            324227,
            0};
        for (final double toAdd : input) {
            fakeData.add(toAdd);
        }


        when(model.getYearlyDataSet().getColumn("fakeData")).thenReturn(fakeData);

        Assertions.assertEquals(target.computeError(model), 105123795984d / 15d, .0001);

        //if you decrease coefficient of variation, you will weigh your error more!
        target.setCoefficientOfVariation(Double.NaN);
        Assertions.assertEquals(target.computeError(model), 105123795984d / 15d, .0001);

        target.setCoefficientOfVariation(0);
        Assertions.assertEquals(target.computeError(model), 105123795984d / 15d, .0001);

    }
}
