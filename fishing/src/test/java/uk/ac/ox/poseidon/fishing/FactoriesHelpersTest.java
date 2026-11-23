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

package uk.ac.ox.poseidon.fishing;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uk.ac.ox.poseidon.core.testing.FactoriesHelpers;

final class FactoriesHelpersTest {

  @TestFactory
  Stream<DynamicTest> everyHelperBuildsItsResult() throws IOException {
    return FactoriesHelpers.everyHelperBuildsItsResult(
      "uk.ac.ox.poseidon.fishing",
      FactoriesHelpersTest::rejectsDefaultArguments
    );
  }

  /**
   * @return whether {@code method} rejects empty lists, as the default
   *         arguments are: its own tests cover it
   */
  private static boolean rejectsDefaultArguments(final Method method) {
    return method.getName().equals("vesselRegister");
  }
}
