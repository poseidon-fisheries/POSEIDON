# AGENTS.md

This file provides guidance to coding agents (Claude Code and others) when working with code in
this repository. Longer topic-specific sections live in `docs/agents/` — read the relevant one
before touching code it covers:

- `docs/agents/architecture.md` — the Factory + Scenario pattern, `*Factory` class shape.
- `docs/agents/javadoc.md` — Javadoc conventions for the component/`Factory`/`Factories` triplet.
- `docs/agents/lombok.md` — which Lombok annotation shape to use per class role.
- `docs/agents/conceptual-correctness.md` — don't collapse distinct concepts for a smaller diff.

## What this is

POSEIDON is an agent-based model of fisheries, built on the [MASON](https://cs.gmu.edu/~eclab/projects/mason/) discrete-event simulation toolkit. It's a Gradle multi-module Java project (Java 25 toolchain), developed by the University of Oxford. This `main` branch is an ongoing rewrite/redesign of the original POSEIDON model.

## Build & test commands

```
./gradlew build                     # build all modules, run all tests + spotbugs
./gradlew test                      # run all unit tests
./gradlew :core:test                # run tests for a single module (core, io, geography, biology, agents, regulations, calibration, gui, examples)
./gradlew :core:test --tests "uk.ac.ox.poseidon.core.ScenarioTest"          # run a single test class
./gradlew :core:test --tests "uk.ac.ox.poseidon.core.ScenarioTest.methodName"  # run a single test method
./gradlew jacocoTestReport          # generate coverage reports (build/reports/jacoco/test/…) per module
./gradlew spotbugsMain              # run SpotBugs static analysis on a module (Werror is on for javac too)
```

Notes:
- Compilation treats warnings as errors (`-Werror`), so unused-import or unchecked warnings fail the build, not just linting.
- Test tasks are auto-configured with a Mockito Java agent (`-javaagent`, `-Xshare:off`) — don't remove this if editing `buildlogic.java-common-conventions.gradle.kts`.
- Tests use JUnit 5 (`useJUnitPlatform()`), AssertJ, Mockito, and jqwik (property-based testing) — check existing tests in a module before assuming which style applies.
- SpotBugs exclusions live in `spotbugs_exclude.xml` at the repo root; consult it before assuming a finding is a false positive.
- To run one of the example scenarios directly: `./gradlew :examples:run` (see `examples/build.gradle.kts` for the `writePeterSnapperScenario` task that writes a scenario YAML from Java code).

## Module structure & dependency graph

Modules are Gradle subprojects under the root; dependencies flow one way (no cycles):

```
core  →  calibration
core  →  io  →  geography  →  biology  →  agents  →  gui
                geography  →  regulations  →  agents
                                              agents  →  examples
                            calibration, io, gui, regulations, biology  →  examples
```

- **core** — Foundational abstractions used by everything else: `Simulation` (extends MASON's `SimState`), `Scenario` (declarative, serializable simulation configuration), the `Factory<Scope, C>` pattern, `Scope`/`SimulationScope` (dependency-scoping for factories), `TemporalSchedule`. Depends on MASON (vendored jar in `core/libs/mason`), units-of-measurement libs (Indriya/JSR-385), fastutil.
- **io** — YAML scenario (de)serialization (`ScenarioLoader`, `ScenarioWriter`, backed by SnakeYAML) and tabular data I/O (Tablesaw).
- **geography** — Spatial primitives and grids: coordinates, envelopes, bathymetry, ports, spatial allocators — built on JTS and GeoTools.
- **biology** — Biological/ecological model: species, biomass, "fisheable" content on the grid.
- **regulations** — Fishing regulation rules: `Regulations`, `ForbiddenIf`/`PermittedIf`, spatial/temporal actions/predicates.
- **agents** — The fishing-agent model itself: vessels, trips, tasks, choices, catches, fuel, markets/money, regulation enforcement on agents.
- **calibration** — Parameter calibration using Jenetics (genetic algorithms); `CalibrationProblem`, `CalibrationRunner`.
- **gui** — Swing-based visualization (FlatLaf look-and-feel, Batik for SVG) built on MASON's UI facilities (`ScenarioWithUI`, portrayals, palettes).
- **examples** — Runnable example scenarios (e.g. `petersnapper`) that wire the other modules together into an application; also contains scenario-writing/calibration entry points with `main` methods.
- `common/` at the repo root is a stray leftover directory (has build artifacts but no `build.gradle.kts` and is not listed in `settings.gradle.kts`) — it is not an active module.

Convention plugins (in `buildSrc/src/main/kotlin/`) centralize shared config:
- `buildlogic.java-common-conventions` — shared dependencies (Guava, Lombok, Caffeine, fastutil), Java 25 toolchain, `-Werror`, JUnit 5 + Jacoco + SpotBugs wiring.
- `buildlogic.java-library-conventions` — used by library modules (adds `java-library`).
- `buildlogic.java-application-conventions` — used by runnable modules (adds `application`, default main class `uk.ac.ox.poseidon.server.Server`).

## Code style

Mark everything that can be `final` as `final` — local variables, method/constructor parameters, and caught exceptions (e.g. `catch (final IOException e)`, `try (final SomeResource r = ...)`). The only common exception is a traditional counting `for` loop's own loop variable (`for (int i = 0; ...; i++)`), which must stay mutable by definition.

## Licensing

All source files carry a GNU GPLv3 header (University of Oxford copyright). Preserve this header format when creating new source files — copy it from a neighboring file in the same module rather than retyping it.
