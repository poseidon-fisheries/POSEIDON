/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.epo.calibration;

import java.nio.file.Paths;

/**
 * just a little class for me to run some main(String[] args) files to resume/evaluate simulation runs that crashed
 */
public class TunaCalibratorHelper {

    public static void main(final String[] args) {

        final double[] best =
            new double[]{-0.175, -8.976, 14.666, -12.253, -15.149, -0.562, -15.000, 2.498, 10.691, 10.155, 11.123,
                14.545, 5.289, -5.486, 14.450, -10.683, 12.639, -7.796, -7.644, 0.437, 13.376, 11.686, -6.076, 7.558,
                -1.402, -12.524, 9.244, -9.006, -14.192, -2.245, -0.347, 10.491, 14.521, -1.605, -15.117, 7.976,
                -1.995, -2.738, -5.585, 10.815, 7.209, 0.142, -8.730, 12.533, 14.469, 11.422, -8.580, 6.478, -0.833,
                -11.739, -12.597, 8.794, 9.256, 9.152, -4.318, -15.547};
        TunaCalibrator.evaluateSolutionAndPrintOutErrors(Paths.get(
            "/home/carrknight/code/tuna/tuna/calibration/results/oneboat_duds/carrknight/2022-02-06_11.06" +
                ".59_local_dudforced_filtering_search/",
            "calibration.yaml"
        ), best);

    }

}
