# AGENTS.md

This file provides guidance to coding agents (Claude Code and others) when
working with code in this repository. Longer topic-specific sections live in
`docs/agents/` — read the relevant one before touching code it covers:

- `docs/agents/architecture.md` — before writing or changing a factory, a
  `Factories` helper or scenario code, or working with model time values: the
  Factory + Scenario pattern, whole-second time precision, `*Factory` class
  shape, live parameters, data shared across simulations, determinism.
- `docs/agents/runtime-model.md` — before changing `core` or `fishing`
  internals: product stores, levels and scopes, vessel components, vessel
  creation and statements, registration, services, scheduling.
- `docs/agents/javadoc.md` — before writing or editing Javadoc: layout, and the
  component/`Factory`/`Factories` triplet.
- `docs/agents/lombok.md` — before writing a class or changing its Lombok
  annotations: which annotation shape fits each class role.
- `docs/agents/code-style.md` — before writing or reviewing Java: readability,
  Checkstyle's checks, scenario classes, `Factories` helper names, streams
  versus loops, single exit, hot paths, and the Prettier/Spotless setup
  SURIMI-POSEIDON duplicates.
- `docs/agents/testing.md` — before writing or editing a test: AssertJ, test
  layout, and mocks that scopes hold weakly.
- `docs/agents/naming.md` — before naming or reviewing any identifier:
  factories, steppables, tasks, predicates, cells, fields and function-valued
  fields, and what reads as English.
- `docs/agents/conceptual-correctness.md` — before fixing a design problem or
  giving missing state a default: don't collapse distinct concepts for a smaller
  diff; absence is not a default.

## What this is

POSEIDON is an agent-based model of fisheries, built on the
[MASON](https://cs.gmu.edu/~eclab/projects/mason/) discrete-event simulation
toolkit. It's a Gradle multi-module Java project (Java 25 toolchain), developed
by the University of Oxford. This `main` branch is an ongoing rewrite/redesign
of the original POSEIDON model.

## Build & test commands

```
./gradlew build                     # build all modules, run all tests + spotbugs + checkstyle
./gradlew test                      # run all unit tests
./gradlew :core:test                # run tests for a single module (core, io, geography, biology, fishing, regulations, calibration, gui, examples)
./gradlew :core:test --tests "uk.ac.ox.poseidon.core.ScenarioTest"          # run a single test class
./gradlew :core:test --tests "uk.ac.ox.poseidon.core.ScenarioTest.methodName"  # run a single test method
./gradlew jacocoTestReport          # generate coverage reports (build/reports/jacoco/test/…) per module
./gradlew spotbugsMain              # run SpotBugs static analysis on a module (Werror is on for javac too)
./gradlew spotlessApply             # format Java and Markdown with Prettier, remove unused imports, add missing license headers
./gradlew checkstyleMain            # run Checkstyle on main sources (checkstyleTest for tests)
```

Notes:

- Shared build configuration (dependencies, toolchain, compiler flags, test and
  analysis wiring) lives in the convention plugins in
  `buildSrc/src/main/kotlin/`, not in the modules' build files.
- `build` runs `spotlessCheck`, which fails on any Java or Markdown file
  `spotlessApply` would change: run `./gradlew spotlessApply` after editing
  either. For license-header years, see "Licensing".
- Compilation runs every javac lint category except `serial` and treats warnings
  as errors (`-Werror -Xlint:all,-serial`), so an unchecked or deprecation
  warning fails the build, not just linting.
- Checkstyle (`config/checkstyle/checkstyle.xml`) fails the build on any
  violation; the checks code most often trips are listed in
  `docs/agents/code-style.md`.
- Test tasks are auto-configured with a Mockito Java agent (`-javaagent`,
  `-Xshare:off`): keep it when editing
  `buildlogic.java-common-conventions.gradle.kts`.
- jdtls (the LSP tool) can report type-inference errors that javac accepts;
  `./gradlew compileJava` is authoritative.
- SpotBugs exclusions live in `spotbugs_exclude.xml` at the repo root; consult
  it before assuming a finding is a false positive.
- To run one of the example scenarios directly: `./gradlew :examples:run` (see
  `examples/build.gradle.kts` for the `writePeterSnapperScenario` task that
  writes a scenario YAML from Java code).

## Module structure & dependency graph

Modules are Gradle subprojects under the root; dependencies flow one way (no
cycles):

```
core  →  calibration
core  →  io  →  geography  →  biology  →  fishing  →  gui
                geography  →  regulations  →  fishing
                                              fishing  →  examples
                            calibration, io, gui, regulations, biology  →  examples
```

- **core** — Foundational abstractions used by everything else: `Simulation`
  (extends MASON's `SimState`), `Scenario` (declarative, serializable simulation
  configuration), the `Factory<Scope, P>` pattern, `Scope`/`SimulationScope`
  (dependency-scoping for factories), `TemporalSchedule`. MASON is an upstream
  commit built by JitPack, pinned in `gradle/libs.versions.toml`.
- **io** — YAML scenario (de)serialization (`ScenarioLoader`, `ScenarioWriter`,
  backed by SnakeYAML) and tabular data I/O (Tablesaw).
- **geography** — Spatial primitives and grids: coordinates, envelopes,
  bathymetry, ports, spatial allocators — built on JTS and GeoTools.
- **biology** — Biological/ecological model: species, biomass, "fisheable"
  content on the grid.
- **regulations** — Fishing regulation rules: `Regulations`,
  `ForbiddenIf`/`PermittedIf`, spatial/temporal actions/predicates.
- **fishing** — The vessels, the model's agents, and what they do and deal with:
  trips, tasks, choices, catches, fuel, markets/money, regulation enforcement on
  vessels.
- **calibration** — Parameter calibration using Jenetics (genetic algorithms);
  `CalibrationProblem`, `CalibrationRunner`.
- **gui** — Swing-based visualization (FlatLaf look-and-feel, Batik for SVG)
  built on MASON's UI facilities (`ScenarioWithUI`, portrayals, palettes).
- **examples** — Runnable example scenarios (e.g. `petersnapper`) that wire the
  other modules together into an application; also contains
  scenario-writing/calibration entry points with `main` methods.

## Code style

Mark everything that can be `final` as `final` — local variables,
method/constructor parameters, and caught exceptions (e.g.
`catch (final IOException e)`, `try (final SomeResource r = ...)`). The only
common exception is a traditional counting `for` loop's own loop variable
(`for (int i = 0; ...; i++)`), which must stay mutable by definition.

Formatting is Prettier's, applied by `./gradlew spotlessApply`; wrap comments by
hand within 80 columns. A method or lambda has at most one `return`, as its last
statement. Scenario classes (such as `PeterSnapperScenario`) stay readable for
people new to Java, as `docs/agents/code-style.md` details.

## Merging

Merge branches into `main` by fast-forward only (`git merge --ff-only`), never
with a merge commit. If `main` has moved on, rebase the branch onto it first.

## Licensing

All source files carry a GNU GPLv3 header (University of Oxford copyright);
`./gradlew spotlessApply` adds it to new Java files. Spotless keeps whatever
year a header has, so when you modify a file, end its year range at the current
year yourself: with `YYYY` the current year, `2024` and `2024-2025` both become
`2024-YYYY`, and `YYYY` stays.
