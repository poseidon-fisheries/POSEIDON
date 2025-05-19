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
package uk.ac.ox.poseidon.r;

import uk.ac.ox.poseidon.simulations.api.Simulation;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class Utils {
    public static String getStackTrace(final Throwable e) {
        try (final StringWriter sw = new StringWriter()) {
            try (final PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                return sw.toString();
            }
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void step(final Simulation[] simulations) {
        Arrays.stream(simulations).parallel().forEach(Simulation::step);
    }

}
