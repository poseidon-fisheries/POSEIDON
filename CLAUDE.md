# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

POSEIDON is an agent-based model of fisheries, built on the [MASON](https://cs.gmu.edu/~eclab/projects/mason/) discrete-event simulation toolkit. It's a Gradle multi-module Java project (Java 25 toolchain), developed by the University of Oxford. This `SURIMI` branch is an ongoing rewrite/redesign of the original POSEIDON model.

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

## Core architectural pattern: Factory + Scenario

The model is built around a declarative configuration → runtime object pattern:

- `Factory<S extends Scope, C>` (`core/.../Factory.java`) is a `get(scope) -> C` producer. Most simulation components (species, regulations, vessels, allocators, providers…) are expressed as a `Factory` implementation plus a plain value/config class, so that scenarios can be built either programmatically or deserialized from YAML.
- `AbstractFactory` (`core/.../AbstractFactory.java`) adds per-scope, per-hashcode memoization via Caffeine so the same factory invocation for the same scope returns the same instance — this is how object identity/sharing is achieved across a scenario graph. When creating a new factory, extend this rather than implementing `Factory` directly unless you have a specific reason not to.
- `Scope` / `SimulationScope` model the lifecycle/sharing boundary a factory-produced object belongs to (global vs. per-simulation vs. relative/nested scopes — see `GlobalScopeFactory`, `PerSimulationFactory`, `RelativeScopeFactory`, `SimulationScopeFactory`).
- Prefer `RelativeScopeFactory` over `GlobalScopeFactory` for any factory with `Factory`-typed input fields: `GlobalScopeFactory` hard-codes global scope regardless of what its inputs resolve to, whereas `RelativeScopeFactory` inspects its own `Factory`-typed fields and adopts their scope automatically (falling back to global scope if it has none) — so it still ends up at global scope when its inputs do, but safely demotes to per-simulation scope instead of silently sharing stale state if an input ever isn't global. No existing `GlobalScopeFactory` subclass has a `Factory`-typed field; that's a signal you want `RelativeScopeFactory` instead.
- `Scenario` is an immutable (Lombok `@Builder`) bag of named component `Factory` instances plus a starting date/time. `Scenario.startNewSimulation(...)` builds a `Simulation` by resolving every component factory against a fresh `SimulationScope`. `SimulationStartOptions` supports temporarily overriding bean properties (via `commons-beanutils`, dotted property paths) for the duration of a single simulation run — used heavily by calibration, which needs to vary parameters run-to-run without mutating the original `Scenario`.
- `Simulation` extends MASON's `SimState` and owns the `TemporalSchedule`, an `EventManager`, and the list of resolved components; `finalProcesses` run at `finish()`.
- Scenarios are serialized to/from YAML via SnakeYAML (`io/.../ScenarioLoader`, `ScenarioWriter`); `ScenarioLoader` restricts deserializable classes to configured prefixes (`uk.ac.ox.poseidon`, `java`, plus caller-supplied extras) as a safety measure — keep this in mind if adding factories in new packages that scenarios need to reference.
- **Every `Scenario` must remain serializable to YAML.** Anything hung off a `Scenario` (components, factories, nested config) has to survive a SnakeYAML round-trip — don't introduce fields/types that can't be represented that way.
- **YAML scenario files are generated output, never hand-edited — with no exceptions for edits
  that look small, mechanical, or "verified."** They're produced from Java code via
  `ScenarioWriter`/`Factories`-style helpers (see the `writePeterSnapperScenario` Gradle task). If
  a scenario needs to change, change the Java that generates it and regenerate the YAML — don't
  patch the `.yaml` file directly, and don't reach for a scripted find-and-replace (sed, a
  hand-rolled rename script, etc.) even when you've confirmed via a regenerated copy that it
  produces the identical text. Renaming a `*Factory` class is a Java rename that gets reflected in
  YAML by rerunning the writer task, not a text substitution performed on the checked-in file. If
  running the writer task turns up a diff broader than the change you set out to make — i.e. the
  committed YAML had already drifted from what the current Java produces, for reasons unrelated to
  your change — don't hand-patch around the unrelated part and don't silently drop it either.
  Surface the drift to the user and let them decide whether to accept the full regenerated file
  (folding the unrelated fix in) or handle it as a separate change.
- **When building a `Scenario`, use the module's static `Factories` helper methods, not `new SomeFactory(...)` directly.** Modules expose a `Factories` class (e.g. `core/.../time/Factories.java`, and similarly in `io`, `geography`, `regulations`, and per-example packages) with static factory methods (`Factories.days(3)`, `Factories.dateTime(...)`, etc.) that scenario-building code should call instead of instantiating `*Factory` classes directly.

When adding a new simulation component (a new kind of provider, allocator, regulation, etc.), follow the existing pattern in the relevant package: a plain class implementing the domain interface, plus a `*Factory` (typically extending `AbstractFactory`) that YAML scenarios instantiate, and a static helper method added to that package's `Factories` class for programmatic construction.

### Factory class shape

`*Factory` classes should have exactly two constructors and nothing else constructor-wise:
- a no-args constructor, used only by SnakeYAML during deserialization;
- an all-args constructor, used by the package's static `Factories` helper method.

Don't add other constructors or builders to a factory class — construct it either via YAML or via the static helper.

When designing a new factory, favor composition over a new bespoke implementation: check whether existing factories (in the same package or in `core`, e.g. constant/random/temporal providers) can be combined or parameterized to produce the desired behavior before writing a new `Factory` class from scratch.

Not every field on a factory needs to be `Factory`-typed. Wrap a field in `Factory<? super S,
? extends X>` only when its value must be *resolved against the scope* — a nested component
(`ModelGrid`, a species list), something computed at build time, or a value that legitimately
varies per scope. A plain literal that's just configuration — a `String`, `double`, `int`,
`boolean`, an enum — stays a plain field, exactly like `separator` on
`TimeIndexedBiomassGridsFromNetCdfFactory`. Wrapping a literal in `Factory<String>` "for
consistency" or "to be safe" is over-engineering: it adds a layer of indirection nothing resolves
differently through, and every call site has to write `scope -> "value"` for no gain.

## Lombok usage

Lombok (`buildlogic.java-common-conventions`, all modules) is used pervasively to cut constructor/
accessor/equality boilerplate. Reach for it on *any* class where these apply, not just the shapes
below — a plain class with `private final` dependency fields should get `@RequiredArgsConstructor`
rather than a hand-written constructor, and a class that needs `equals`/`hashCode`/`toString` should
get `@Value`/`@Data`/`@EqualsAndHashCode` rather than hand-rolled ones. Four recurring shapes cover
almost every case — match the shape to the class's role:

- **Injected dependencies / no mutation** (most non-factory classes: services, handlers, predicates,
  action functions): `@RequiredArgsConstructor` on the class, `private final` fields, annotated
  `@NonNull` when the constructor should null-check them.
- **Immutable value objects** (small data carriers with no setters): `@Value` — final fields,
  generated getters/`equals`/`hashCode`/`toString`. Add hand-written constructors alongside it only
  when a field needs derived/defaulted state.
- **`Factory` YAML beans** (`*Factory` classes per the Factory/Scenario pattern above): always the
  same four — `@Data @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = true)`.
  These need mutable bean semantics (no-args ctor + setters) for SnakeYAML (de)serialization, and
  `callSuper = true` so equality includes the superclass's fields. Don't add other constructors —
  see "Factory class shape" above.
- **Builder-style config objects** (e.g. `Scenario`, `SimulationStartOptions`): `@Getter @Setter
  @Builder @NoArgsConstructor` — mutable via setters or built fluently via `@Builder`.

Use `@Getter(AccessLevel.NONE)` (or `@Setter(AccessLevel.NONE)`) on individual fields to suppress
just that one accessor rather than dropping `@Data`/`@Getter` for a hand-written class.

### Sharing data across simulations

A type meant to be read-only and shared globally (e.g. across every simulation run from one loaded scenario) must not implement a mutable-by-contract interface like `MutableGrid` — even if its own methods never mutate, `MutableGrid.getField()` (or any analogous accessor) hands out a live mutable handle to the backing structure, so the object is never actually immutable. Follow the existing pattern instead: a small read-only sibling type that wraps the mutable implementation without implementing its mutable interface (e.g. `CarryingCapacityGrid` / `ImmutableBiomassGrid`, both `extends DoubleGridWrapper` rather than implementing `BiomassGrid`).

### Collections

Prefer Guava's immutable collection types (`ImmutableMap`, `ImmutableList`, `ImmutableSet`, etc.) over mutable JDK collections (`HashMap`, `ArrayList`, `HashSet`) for factory-produced results and other data that shouldn't change after construction — this is already the convention for data shared across simulations or handed out from a resolved `Factory` (e.g. `FisheableBiomassGrids`'s use of `ImmutableMap`). Reach for mutable collections only for genuinely local, mutable working state inside a method body.

## Conceptual correctness over convenient fixes

Never resolve a design problem by picking the solution that's merely convenient or that makes an
immediate symptom go away, when it costs conceptual correctness. If a fix would collapse two
distinct concepts into one because that's easier to code, don't — name the distinction and give it
its own representation instead, even if that means an extra class/predicate/exception.

Example: a "no path exists between two points" failure is not the same concept as "a path exists
but the destination is illegal" or "a path exists but is too slow" — collapsing all three into a
single boolean `false` (so an unreachable destination looks identical to a reachable-but-rejected
one) discards information a future reader needs, even though it's the smaller diff. The correct
shape kept "unreachable" as its own explicit failure (an exception, or a dedicated
reachability check run first) rather than silently folding it into the same signal as "reachable
but rejected."

When you notice yourself justifying a shortcut with "this solves the problem in front of us" rather
than "this is what the concept actually is," stop and raise the conceptual-correctness question to
the user instead of silently taking the expedient path.

## Code style

Mark everything that can be `final` as `final` — local variables, method/constructor parameters, and caught exceptions (e.g. `catch (final IOException e)`, `try (final SomeResource r = ...)`). The only common exception is a traditional counting `for` loop's own loop variable (`for (int i = 0; ...; i++)`), which must stay mutable by definition.

## Licensing

All source files carry a GNU GPLv3 header (University of Oxford copyright). Preserve this header format when creating new source files — copy it from a neighboring file in the same module rather than retyping it.
