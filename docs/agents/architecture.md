# Core architectural pattern: Factory + Scenario

The model is built around a declarative configuration → runtime object pattern:

- `Factory<S extends Scope, P>` (`core/.../Factory.java`) is a `get(scope) -> P` producer. Most simulation components (species, regulations, vessels, allocators, providers…) are expressed as a `Factory` implementation plus a plain value/config class, so that scenarios can be built either programmatically or deserialized from YAML.
- Scenario-building code composes `Factory`-returning static helpers only — nested calls all the way down. Never call `.get(scope)`, `.apply(...)`, or `.test(...)` directly in scenario-building code; resolving the graph is the framework's job at simulation-start time, not something scenario code does itself.
- `AbstractFactory` (`core/.../AbstractFactory.java`) builds a factory's product once per owner and keeps it in a `ProductStore` keyed by factory identity: the simulation's store for a simulation-level factory, the agent's for a vessel-level one, and the factory's own store for a global one. Sharing is declared by two things only: the factory's scope, and referencing the same factory object from several places. Two distinct factory objects never share a product, however equal their values, so a copy made by derivation builds its own. A simulation's store ends when the simulation finishes, and refuses any later build. When creating a new factory, extend this rather than implementing `Factory` directly unless you have a specific reason not to.
- `Scope` / `SimulationScope` model the lifecycle/sharing boundary a factory-produced object belongs to (global vs. per-simulation vs. relative/nested scopes — see `GlobalScopeFactory`, `PerSimulationFactory`, `RelativeScopeFactory`, `SimulationScopeFactory`).
- Prefer `RelativeScopeFactory` over `GlobalScopeFactory` for any factory with `Factory`-typed input fields: `GlobalScopeFactory` hard-codes global scope regardless of what its inputs resolve to, whereas `RelativeScopeFactory` inspects its own `Factory`-typed fields and adopts their scope automatically (falling back to global scope if it has none) — so it still ends up at global scope when its inputs do, but safely demotes to per-simulation scope instead of silently sharing stale state if an input ever isn't global. No existing `GlobalScopeFactory` subclass has a `Factory`-typed field; that's a signal you want `RelativeScopeFactory` instead.
- The same goes for `SimulationScopeFactory` and `VesselScopeFactory`: **`RelativeScopeFactory` is the default, and a fixed scope needs a reason tying the product to it.** There are three:
  - **the product holds mutable state** of its own (a memory, a network, an accumulator). A relative factory whose inputs are all global would be global too, and would share that state across simulations; so would one whose inputs aren't `AbstractFactory`s (a lambda), since those are ignored. A product that only holds a reference to a mutable object it was given isn't stateful on that account: the state belongs to whatever built that object;
  - **`newInstance` uses the scope itself**: it reads `scope.getVessel()`, or registers its product through the scope (`scope.addListener`, `scope.addFinalProcess`). A relative factory with global inputs would build it once, from the first scope it was given;
  - **`newInstance` has side effects**, such as changing the objects its inputs give it (`InitialPricesFactory` sets the prices of the markets it is given). The scope decides how many times the side effect runs, so it shouldn't depend on what the inputs happen to resolve at.

  A stateless product built only from what its inputs give it (`TravelCost`, `FishingEventRecorder`, `NetValuation`) gets a relative factory, typed `<S extends Scope>` like `NetValuationFactory`: it is then per vessel when an input is, and shared when none is.
- `Scenario` is an immutable (Lombok `@Builder`) bag of named component `Factory` instances plus a starting date/time. `Scenario.startNewSimulation(...)` builds a `Simulation` by resolving every component factory against a fresh `SimulationScope`. A scenario that differs in some value is derived from it, leaving it untouched: `with(address, value)` replaces the value at an address that starts at a component's name (commons-beanutils syntax, e.g. `gear.proportion`), copying every factory above the replacement and keeping every other one, so global products stay shared; `withComponent(name, factory)` adds a component, and `withStartingDateTime(start)` moves the start. Calibration derives one scenario per candidate. A run is started with its seed and, optionally, its id: `startNewSimulation(seed[, simulationId])`.
- `Simulation` extends MASON's `SimState` and owns the `TemporalSchedule`, an `EventManager`, and the list of resolved components; `finalProcesses` run at `finish()`.
- The simulation's services are inputs: its random number generator (`randomNumberGenerator()`), its clock (`currentDateTime()`, `startingDateTime()`), its event manager (`eventManager()`) and the run's id (`runId()`). A factory that draws, reads the date, publishes events or needs the run's id takes the service's factory as an input, which its helper passes, so scenario code never names it. A scope does not offer the simulation to a build: `SimulationScope`'s accessor is package-private, and the only readers, all in `core`, are the services' own factories, the store routing (`SimulationScopeFactory`, `PerSimulationFactory`), the scope's registration methods and the `SchedulingScope`. A service is simulation-level, so a relative factory that takes one is at least simulation-level.
- Scheduling is not something a scope offers. Only scheduling factories schedule: core's scheduling wrappers (`scheduledOnce`, `scheduledRepeating`, `scheduledByDateTime` and the rest, in `core/.../schedule/Factories.java`) and the fleet factories. They extend `SchedulingFactory`, whose build receives a `SchedulingScope` offering the simulation's schedule. A `SimulationScopeFactory`'s build always receives a plain `SimulationScope`, and a wrapper's input is typed `Factory<? super SimulationScope, ...>`, which no factory typed in `SchedulingScope` is, so the steppable a wrapper schedules cannot schedule anything itself. What runs when is said in the scenario, by wrapping a steppable.
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
- **In scenario-building code, write a factory inline where it is used, and give it a local variable only when:**
  - **it is used more than once.** This is about identity, not tidiness: products are stored by factory identity, so two inline copies of the same expression build two different objects. When an object must be shared (e.g. one per-vessel memory that both a learner and the view read), a single named instance is what makes it shared, and inlining it would silently duplicate it;
  - **it is a root-level component** of the `Scenario` (`.component("name", ...)`);
  - **something outside the scenario needs to reach it**, such as the GUI or a calibration (by component name or property path).

When adding a new simulation component (a new kind of provider, allocator, regulation, etc.), follow the existing pattern in the relevant package: a plain class implementing the domain interface, plus a `*Factory` (typically extending `AbstractFactory`) that YAML scenarios instantiate, and a static helper method added to that package's `Factories` class for programmatic construction.

## Factory class shape

`*Factory` classes are immutable: their fields are final and they have no setters. Each has one
constructor and a Lombok builder:
- an all-args constructor, used by the package's static `Factories` helper method;
- `@SuperBuilder(toBuilder = true)`: `ScenarioLoader` builds factories through it, and
  `toBuilder()` makes a changed copy.

Don't add other constructors to a factory class — construct it either via YAML or via the static helper.

When designing a new factory, favor composition over a new bespoke implementation: check whether existing factories (in the same package or in `core`, e.g. constant/random/temporal providers) can be combined or parameterized to produce the desired behavior before writing a new `Factory` class from scratch.

Not every field on a factory needs to be `Factory`-typed. Wrap a field in `Factory<? super S,
? extends X>` only when its value must be *resolved against the scope* — a nested component
(`ModelGrid`, a species list), something computed at build time, or a value that legitimately
varies per scope. A plain literal that's just configuration — a `String`, `double`, `int`,
`boolean`, an enum — stays a plain field, exactly like `separator` on
`TimeIndexedBiomassGridsFromNetCdfFactory`. Wrapping a literal in `Factory<String>` "for
consistency" or "to be safe" is over-engineering: it adds a layer of indirection nothing resolves
differently through, and every call site has to write `scope -> "value"` for no gain.

## Live parameters, with a plain-value overload

A building block reads its model parameters (a threshold, a duration, an amount of time, a rate)
through a `Supplier` each time it is used, rather than fixing them when it is built. It then works
the same whether the value is fixed, depends on the date, the vessel or the state of the
simulation, or is changed mid-run, and any provider in `core/providers` can feed it. Its package's
`Factories` class offers the live version, taking a `Factory<? super S, ? extends Supplier<…>>`,
plus an overload taking a plain value that it wraps in `constant(...)`:

- `lessThan(double)` and `lessThan(Factory)` in `core/predicates/comparable`;
- `dateTimeAfter(referenceDateTime, DurationFactory)` and
  `dateTimeAfter(referenceDateTime, Factory)` in `core/providers/temporal`.

Use this shape for new building blocks even when no caller needs a varying value yet. Reading a
`Constant` costs a call that the JIT inlines; it doesn't show next to the work around it.

What counts as a plain value depends on the YAML:

- a `double`, `int`, `boolean`, `String` or enum is written to the YAML as it is, so the overload
  takes it directly;
- a `java.time` value, a `Quantity` or another object without a builder is not: the YAML would
  hold whatever its getters give, and loading it would fail. The overload takes its value factory instead (`DurationFactory`, `PeriodFactory`,
  `TimeFactory`…), which is what scenario code holds anyway (`hours(3)`).

Never write the overload as a generic `foo(T value)` for such types: `foo(hours(3))` would then
compile with `T = DurationFactory` and wrap the factory itself as the value. An overload taking
`Factory<? super S, ? extends X>` isn't possible either, because it erases to the same signature
as the live version; a concrete value-factory class such as `DurationFactory` doesn't clash.

This doesn't apply to the configuration literals of the previous section (a column name, a
separator, a file path): they say how to build or read something, not how the model behaves, and
stay plain fields.

## Determinism

A simulation's results depend only on its scenario and its seed: the same seed gives the same
results in one JVM or across JVMs, whatever ran before it. Code that runs during a simulation
therefore never iterates a collection whose order depends on identity hash codes:

- a `HashMap` or `HashSet` keyed by a class without its own `equals`/`hashCode` (`Vessel`, `Agent`,
  `Market`, `Fleet`, most components) iterates in an order that changes from run to run, because
  identity hash codes depend on how many the thread has already handed out. Use a `LinkedHashMap`
  or `LinkedHashSet` (insertion order), a Guava immutable collection, or sort by a stable key. A
  hash collection that is only looked up, never iterated, is fine;
- Guava graphs: set `incidentEdgeOrder(ElementOrder.stable())`, since the default order of a
  node's neighbours is unordered (`SocialNetwork`);
- `Collectors.toMap`, `toSet` and `groupingBy` build hash collections: use Guava's
  `toImmutableMap`/`toImmutableSet` or pass an ordered supplier when the result is iterated.

Keys with value equality (`String`, `Species`, `Int2D`, records, `@Value` classes, enums) iterate in
the same order in every run and are fine. Randomness comes only from the simulation's random
number generator. Each example scenario has a test that runs the same seed twice in one JVM and
compares the results (`PeterSnapperScenarioTest.sameSeedGivesSameLandings`; SURIMI-POSEIDON's
`ScenarioReproducibilityTest`).
