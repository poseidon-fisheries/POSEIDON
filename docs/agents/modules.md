# Modules

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
  scenario-writing/calibration entry points with `main` methods. Run one with
  `./gradlew :examples:run`; `examples/build.gradle.kts` has the
  `writePeterSnapperScenario` task that writes a scenario YAML from Java code.
