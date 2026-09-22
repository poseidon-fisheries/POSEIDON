# Core architectural pattern: Factory + Scenario

The model is built around a declarative configuration → runtime object pattern:

- `Factory<S extends Scope, C>` (`core/.../Factory.java`) is a `get(scope) -> C` producer. Most simulation components (species, regulations, vessels, allocators, providers…) are expressed as a `Factory` implementation plus a plain value/config class, so that scenarios can be built either programmatically or deserialized from YAML.
- Scenario-building code composes `Factory`-returning static helpers only — nested calls all the way down. Never call `.get(scope)`, `.apply(...)`, or `.test(...)` directly in scenario-building code; resolving the graph is the framework's job at simulation-start time, not something scenario code does itself.
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

## Factory class shape

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
