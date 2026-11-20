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

package uk.ac.ox.poseidon.core;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import javax.measure.Quantity;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uk.ac.ox.poseidon.core.testing.FactoriesHelpers;

final class FactoriesHelpersTest {

  private static final Set<String> PARSING_HELPERS = Set.of(
    "massOf",
    "speedOf",
    "volumeOf",
    "volumetricFlowRateOf",
    "duration",
    "period"
  );

  @TestFactory
  Stream<DynamicTest> everyHelperBuildsItsResult() throws IOException {
    return FactoriesHelpers.everyHelperBuildsItsResult(
      "uk.ac.ox.poseidon.core",
      FactoriesHelpersTest::readsItsArguments
    );
  }

  /**
   * @return whether {@code method} parses a quantity or a duration from its
   *         argument, or reads a quantity, which none of the default argument
   *         values suits: tests of their own cover them
   */
  private static boolean readsItsArguments(final Method method) {
    return (
      PARSING_HELPERS.contains(method.getName()) &&
      Arrays.stream(method.getParameterTypes()).anyMatch(
        type -> type == String.class || Quantity.class.isAssignableFrom(type)
      )
    );
  }
}
