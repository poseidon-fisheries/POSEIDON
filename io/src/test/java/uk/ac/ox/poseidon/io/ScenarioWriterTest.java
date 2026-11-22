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

package uk.ac.ox.poseidon.io;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.constant;
import static uk.ac.ox.poseidon.core.providers.math.Factories.maxDouble;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ox.poseidon.core.Scenario;

class ScenarioWriterTest {

  @TempDir
  Path directory;

  @Test
  void roundTripPreservesPropertiesAndSharedFactories() {
    final var scenario = scenario();

    final var loaded = new ScenarioLoader().load(
      new ScenarioWriter().write(scenario)
    );

    assertSameScenario(scenario, loaded);
  }

  @Test
  void stringWriterAndFileOverloadsPreserveTheSameScenario()
    throws IOException {
    final var scenario = scenario();
    final var writer = new ScenarioWriter();
    final var stringWriter = new StringWriter();
    final var path = directory.resolve("scenario-path.yaml");
    final var file = directory.resolve("scenario-file.yaml").toFile();

    final var yaml = writer.write(scenario);
    writer.write(scenario, stringWriter);
    writer.write(scenario, path);
    writer.write(scenario, file);

    final var loader = new ScenarioLoader();
    assertSameScenario(scenario, loader.load(yaml));
    assertSameScenario(scenario, loader.load(stringWriter.toString()));
    assertSameScenario(scenario, loader.load(Files.readString(path, UTF_8)));
    assertSameScenario(
      scenario,
      loader.load(Files.readString(file.toPath(), UTF_8))
    );
  }

  @Test
  void fileOverloadsPreserveTheCauseWhenTheParentDirectoryIsMissing() {
    final var scenario = scenario();
    final var writer = new ScenarioWriter();
    final var path = directory.resolve("missing/scenario.yaml");

    assertThatThrownBy(() -> writer.write(scenario, path))
      .isInstanceOf(RuntimeException.class)
      .hasCauseInstanceOf(IOException.class);
    assertThatThrownBy(() -> writer.write(scenario, path.toFile()))
      .isInstanceOf(RuntimeException.class)
      .hasCauseInstanceOf(IOException.class);
  }

  private static void assertSameScenario(
    final Scenario scenario,
    final Scenario loaded
  ) {
    assertThat(loaded.getStartingDateTime()).isEqualTo(
      scenario.getStartingDateTime()
    );
    assertThat(
      loaded.getComponents().keySet()
    ).containsExactlyInAnyOrderElementsOf(scenario.getComponents().keySet());
    scenario
      .getComponents()
      .forEach((name, factory) ->
        assertThat(loaded.getComponents().get(name)).isEqualTo(factory)
      );
    assertThat(loaded.getComponents().get("first")).isSameAs(
      loaded.getComponents().get("second")
    );
    assertThat(loaded.getComponents().get("first")).isNotSameAs(
      scenario.getComponents().get("first")
    );
  }

  private static Scenario scenario() {
    final var sharedObject = object("L'été à Oxford — 漁");
    return Scenario.builder()
      .startingDateTime(dateTime(LocalDateTime.of(2026, 11, 16, 1, 2, 3)))
      .component("first", sharedObject)
      .component("second", sharedObject)
      .component("bounded", maxDouble(constant(-3.5), 2.5))
      .build();
  }
}
