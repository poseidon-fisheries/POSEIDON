# AGENTS.md

This file provides guidance to coding agents (Claude Code and others) when working with code in
this repository. Longer topic-specific sections live in `docs/agents/` — read the relevant one
before touching code it covers:

- `docs/agents/architecture.md` — the Factory + Scenario pattern, `*Factory` class shape, live parameters with plain-value overloads.
- `docs/agents/javadoc.md` — Javadoc layout, and conventions for the component/`Factory`/`Factories` triplet.
- `docs/agents/lombok.md` — which Lombok annotation shape to use per class role.
- `docs/agents/conceptual-correctness.md` — don't collapse distinct concepts for a smaller diff; absence is not a default.

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
./gradlew spotlessApply             # format Java sources with Prettier and add missing license headers
```

Notes:
- `build` runs `spotlessCheck`, which fails on any Java file `spotlessApply` would change: run `./gradlew spotlessApply` after editing Java.
- Compilation treats warnings as errors (`-Werror`), so unused-import or unchecked warnings fail the build, not just linting.
- Test tasks are auto-configured with a Mockito Java agent (`-javaagent`, `-Xshare:off`) — don't remove this if editing `buildlogic.java-common-conventions.gradle.kts`.
- Tests use JUnit 5 (`useJUnitPlatform()`), AssertJ, Mockito, and jqwik (property-based testing) — check existing tests in a module before assuming which style applies.
- `SimulationScope` and `AgentScope` hold their simulation and agent through `WeakReference`s. In tests, keep the mocked `Simulation` or agent in a field: one that only the scope references (`new SimulationScope(mock(Simulation.class))`, or a local variable not used afterwards) can be garbage-collected mid-test, and the test then fails at random with a `NullPointerException`.
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

Formatting is Prettier's (prettier-plugin-java, options in `.prettierrc`), applied by `./gradlew spotlessApply`. Prettier leaves comments as they are: wrap them by hand within 80 columns. It moves a comment placed at the start of a lambda body down the method chain, so put such a comment above the call that takes the lambda. SURIMI-POSEIDON duplicates this setup (`.prettierrc`, `package.json`, the Spotless block and license header in `build.gradle.kts`, the `nodejs`/`node-gradle`/`spotless` versions in `libs.versions.toml`, the Node.js repository in `settings.gradle.kts`): change both repos together.

Prefer lambdas to anonymous classes, and method references to lambdas, wherever they fit. For a `Listener`, which has two abstract methods, use `new SimpleListener<>(EventClass.class, consumer)`. Scenario classes (such as `PeterSnapperScenario`) are the exception: they stay readable for people new to Java, with no method references or other clever tricks.

## Licensing

All source files carry a GNU GPLv3 header (University of Oxford copyright); `./gradlew spotlessApply` adds it to new Java files. Spotless keeps whatever year a header has, so when you modify a file, end its year range at the current year yourself: with `YYYY` the current year, `2024` and `2024-2025` both become `2024-YYYY`, and `YYYY` stays.
