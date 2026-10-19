# Core architectural pattern: Factory + Scenario

The model is built around a declarative configuration → runtime object pattern:

- `Factory<S extends Scope, P>` (`core/.../Factory.java`) is a `get(scope) -> P` producer. Most simulation components (species, regulations, vessels, allocators, providers…) are expressed as a `Factory` implementation plus a plain value/config class, so that scenarios can be built either programmatically or deserialized from YAML.
- Scenario-building code composes `Factory`-returning static helpers only — nested calls all the way down. Never call `.get(scope)`, `.apply(...)`, or `.test(...)` directly in scenario-building code; resolving the graph is the framework's job at simulation-start time, not something scenario code does itself.
- `AbstractFactory` (`core/.../AbstractFactory.java`) builds a factory's product once per owner and keeps it in a `ProductStore` keyed by factory identity: the simulation's store for a simulation-level factory, the vessel's for a vessel-level one, and the factory's own store for a global one. Sharing is declared by two things only: the factory's scope, and referencing the same factory object from several places. Two distinct factory objects never share a product, however equal their values, so a copy made by derivation builds its own. A simulation's store ends when the simulation finishes, and refuses any later build. Ending a store first runs the teardowns of the registrations made for its products, the last made first. When creating a new factory, extend this rather than implementing `Factory` directly unless you have a specific reason not to.
- A factory's **level** says which owner its product belongs to: global (`Scope`), a simulation (`SimulationScope`) or a vessel (`VesselScope`). A level base class declares it (`GlobalScopeFactory`, `SimulationScopeFactory`, `VesselScopeFactory`); a `RelativeScopeFactory` derives it. `get` refuses a scope wider than the factory's level, and widens a narrower one by constructing a scope of exactly that level over the same owners, so a build receives nothing a narrower scope offers, and its registrations go to the store that holds its product.
- Prefer `RelativeScopeFactory` over `GlobalScopeFactory` for any factory with `Factory`-typed input fields: `GlobalScopeFactory` hard-codes global scope regardless of what its inputs resolve to, whereas `RelativeScopeFactory` takes the narrowest level of its inputs (global if it has none) — so it still ends up at global scope when its inputs do, but safely demotes to per-simulation scope instead of silently sharing stale state if an input ever isn't global. Its inputs are the factories its bean properties hold that are typed in its own scope variable (`Factory<? super S, ...>`, or a collection or map of them); a child typed with a concrete scope class is held, built later under a scope the holder makes, and its level does not count (the compiler keeps the build from passing its own scope to it). A property typed as a factory with neither, such as `Factory<?, ?>`, is refused, naming the class and the property. An input that is not an `AbstractFactory`, such as a lambda in a test, has no level to read: the factory then takes the level of the scope it is asked with. A relative factory's bound is `Scope`. No existing `GlobalScopeFactory` subclass has a `Factory`-typed field; that's a signal you want `RelativeScopeFactory` instead.
- The same goes for `SimulationScopeFactory` and `VesselScopeFactory`: **`RelativeScopeFactory` is the default, and a fixed scope needs a reason tying the product to it.** There are three:
  - **the product holds mutable state** of its own (a memory, a network, an accumulator). A relative factory whose inputs are all global would be global too, and would share that state across simulations. A product that only holds a reference to a mutable object it was given isn't stateful on that account: the state belongs to whatever built that object. A cache is not state of a run either: values worked out only from the factory's inputs are the same whichever simulation asks for them, so a relative factory is right, and gives the cache the level of its inputs (`DefaultPathCacheFactory`). Two conditions come with it: everything the cached values depend on must be an input of the factory, even one it never reads, so that a derived scenario gets a new cache; and the cache must be safe to use from several threads, since the simulations sharing it may run in parallel. A shared cache also lives as long as its factory, across every simulation that shares it. A cache that is only fast from one thread, such as a fastutil map, keeps a fixed simulation level (`PathLengthCalculatorFactory`);
  - **`newInstance` uses the scope itself**: it registers its product through the scope (`scope.addListener`, `scope.register`, `scope.addFinalProcess`). A relative factory with global inputs would build it once, from the first scope it was given;
  - **`newInstance` has side effects**, such as changing the objects its inputs give it (`InitialPricesFactory` sets the prices of the markets it is given; `oneValueFrom` draws from the random number generator). The scope decides how many times the side effect runs, so it shouldn't depend on what the inputs happen to resolve at.

  A stateless product built only from what its inputs give it (`TravelCost`, `FishingEventRecorder`, `NetValuation`) gets a relative factory, typed `<S extends Scope>` like `NetValuationFactory`: it is then per vessel when an input is, and shared when none is.
- `Scenario` is an immutable (Lombok `@Builder`) bag of named component `Factory` instances plus a starting date/time. `Scenario.startNewSimulation(...)` builds a `Simulation` by resolving every component factory against a fresh `SimulationScope`. A scenario that differs in some value is derived from it, leaving it untouched: `with(address, value)` replaces the value at an address that starts at a component's name (commons-beanutils syntax, e.g. `gear.proportion`), copying every factory above the replacement and keeping every other one, so global products stay shared; `withComponent(name, factory)` adds a component, and `withStartingDateTime(start)` moves the start. Calibration derives one scenario per candidate. A run is started with its seed and, optionally, its id: `startNewSimulation(seed[, simulationId])`.
- `Simulation` extends MASON's `SimState` and owns the `TemporalSchedule`, an `EventManager`, and the list of resolved components; `finalProcesses` run at `finish()`.
- A **vessel component** (its gear, engine, fuel tank, hold, behaviour, option values, learners, recorders) is made once, with `vesselComponent(factory)`, or as a choice by the vessel's attributes, and the component is what consumers take: `fishing(gear, hold, fuelTank, ...)`, `refuel(fuelTank, ...)`, `componentProperty(hold, "availableCapacityInKg")`. A consumer reads what the component built for the vessel when it uses it, with `gear.of(vessel)` (or `findIn` where absence is normal). A component is also a value of a vessel (a global `ValueFactory` whose product reads the vessel's component), so it goes wherever a vessel value is expected: a picker takes the option values' component as the option values of the vessel it picks for. The vessel builds the components its vessel specification lists, each under a `VesselComponentScope` carrying a store of its own, so each has a lifetime of its own, and keeps what each built under it. Components are compared by identity, as the keys they are. What holds the working state of one component (tasks, the behaviour, holds, learners, listener wrappers) extends `VesselComponentScopeFactory`; what a vessel knows across components (memories, option values) is vessel-level. A choice (`choice(by("main_fishing_gear"), when("PS").then(purseSeine), when("OTB").then(bottomTrawl), otherwise(none()))`) is a table of exact values of the attributes it reads, each combination giving a factory or no component, with an optional default; a combination listed twice fails when the choice is built, and a vessel lacking an attribute it reads, or whose values no row lists when there is no default, fails when it is given the component. A choice reads attributes only, never another component, and a component property drawn by `oneValueFrom` is drawn when its component is built. Only the vessel builds what a component's factory or rows build: building the component itself builds only its reader. Before a simulation starts, the description's `ScenarioCheck`s run over every node: a component some consumer uses but no vessel specification lists fails then, naming the consumer.
- The **vessel specification** (`vesselSpecification(fleet, behaviour)`, given the other components by name with `component(name, component)`) is what a vessel of the scenario is made of, and the only thing that creates vessels: a creation source (`createVesselsAtPort`, the vessel register) asks it to create each vessel from an id and attributes. It names the attributes that give a vessel's home port (`homePortAttribute`) and its name (`nameAttribute`, if vessels have names); the home port is the port of that code in the port grid, looked up whenever it is read, and a code naming no port leaves the vessel without a home port, and so inactive. Creation runs in a fixed order: the vessel joins the fleet with an account, takes its attributes (which place it at its home port), and builds its components, the behaviour first, then the others as listed; only then does the fleet announce its entry to its entry listeners (`addEntryListener`), so a listener only ever sees a complete vessel, before its first step.
- A source states things about an existing vessel through the fleet: a change of attributes, or its exit. A statement joins the vessel's queue and takes effect at the start of the vessel's next step in which its behaviour is not running, never inside the call that made it; one queued on a vessel with no step pending schedules one at once. Scheduling wrappers, and so statements and creations, run at ordering 0, and vessel steps at ordering 1: every statement due at an instant is queued before any vessel steps then, and a vessel takes its first step at the instant it is created. A vessel's account changes at once, from any call. Once the queue is applied, the vessel rebuilds every component whose choice reads an attribute whose value changed, even if the same row is picked: their stores end in the reverse of the build order, then they are built in that order, each under a fresh store; every other component, the vessel's own store (its memories) and its account stay. A hold that still holds catch refuses to have its store ended, so a rebuild only ever drops an empty hold. An exit ends the queue: the vessel's component stores end in the reverse of the build order, then its own store; it leaves the vessel field, then the fleet, which announces the exit to its exit listeners (`addExitListener`) and keeps the id with its exit date, so that a later statement about it is an error and ids are never reused. The vessel register (`scheduledByDateTime(vesselRegister(table, vesselSpecification))`) turns each row into a dated statement; it folds each vessel's rows dated at or before the start into one creation at the start, and a vessel that exited by then is never created, its exit recorded with the fleet.
- A build registers through its scope, never directly. In a vessel scope, `addListener` registers on the vessel's event manager and `register(registry, item)` registers in a `VesselRegistry` (the fishing event sharer), which takes the vessel from the scope and returns its own teardown; the scope gives each teardown to the vessel's store. In a simulation scope, `addListener` registers on the simulation's event manager with no teardown: the simulation's store ends only at the finish, which takes nothing back. The global scope offers no way to register.
- The simulation's services are inputs: its random number generator (`randomNumberGenerator()`), its clock (`currentDateTime()`, `hasDateTime()`, `startingDateTime()`), its event manager (`eventManager()`) and the run's id (`runId()`). A factory that draws, reads the date, publishes events or needs the run's id takes the service's factory as an input, which its helper passes, so scenario code never names it. A scope does not offer the simulation to a build: `SimulationScope`'s accessor is package-private, and the only readers, all in `core`, are the services' own factories, the store routing (`SimulationScopeFactory`, `PerSimulationFactory`), the scope's registration methods and the `SchedulingScope`. A service is simulation-level, so a relative factory that takes one is at least simulation-level.
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

Type parameters keep one meaning throughout: `S` the scope, `P` a factory's product, `C` the
context of a live value, `T` the thing a mapping maps (or, outside the two shapes, the main type a
class is about), `R` a result.

A name says what its product is used for, as a guideline rather than a rule. A product used for
what it gives back (a grid, a price, a vessel function) takes a noun naming that thing. A product
called only for its side effects, such as a `Steppable` or a task, should not take a plain noun
(`vesselsAtPort`), which reads as if it gave back the things it names. It takes a verb when it is
a one-off action (`createVesselsAtPort`, `landCatches`), and the name of the role that acts when
it is an ongoing process (`fixedCostCollector`, `biomassDiffuser`). A noun can still fit a
wrapper that arranges other steppables or tasks (`steppableSequence`), or a product that steps
and also gives something back (a table it fills).

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
each time it is used, rather than fixing them when it is built. It then works the same whether the
value is fixed, depends on the date, the vessel or the state of the simulation, or is changed
mid-run, and any value in `core/providers` can feed it.

A live parameter is a function of a *context*: what the caller has at hand when it asks, usually
the vessel it acts for. It comes in two shapes, each with its factory type in `core`:

- a *value* of the context, `Function<C, R>`, built by a `ValueFactory<S, C, R>` (a constant, the
  current date-time, a property of the vessel);
- a *mapping* of something under the context, `BiFunction<C, T, R>`, built by a
  `MappingFactory<S, C, T, R>` (a comparison with a threshold, a cell's value, a property of a
  route).

A reader of a vessel's state (`currentCell()`, `currentTripStartDateTime()`, `vesselProperty(...)`)
is a value of the vessel: it reads the vessel it is given and holds none, so one serves every
vessel and answers for whichever it is given. Every other object that reads a vessel takes it on
each call too, from its caller, and holds none: a route, a cell checker, a time cost, a valuation, a
picker. A picker takes the option values of the vessel it picks for as a function of that vessel:
values worked out from a memory are valued for it, while a learned table ignores the vessel and
gives itself.

The context is always the vessel the call is for. A test relating it to another vessel, such as a
friendship condition, is a mapping of that other vessel, and reads values off it with
`other(...)`: `condition(other(vesselProperty("homePort")), equal(vesselProperty("homePort")))`
holds when the other vessel's home port is the vessel's. A value without `other(...)` reads the
vessel the call is for, and leaving it out where a mapping is expected does not compile.

A yes/no is a value or a mapping with a `Boolean` result. A value or mapping that reads nothing
from its context has `Object` as its context, so it fits wherever any context is given. A fixed
value is `constant(x)` where a value is expected and `always(x)` where a mapping is (a filter, a
part of a key, a term of a sum over routes); the compiler refuses the wrong one. A
combinator that makes sense in both shapes (`condition`, `allOf`, `anyOf`, `not`,
`defaultIfNull`, `combinedDuration`…) has one overload per shape; a helper method returning a live
value must therefore declare its shape type, or it matches no overload.

Such a combinator has one implementation, its mapping, so it means the same in both shapes. Its
value overload builds the mapping over its value arguments, each made a mapping of `Void` that
ignores what it maps (`ValueAsMapping`), and makes the result a value by giving it `null` as the
thing mapped (`MappingAsValue`). Neither appears in scenario code. A block that transforms what it
is given takes it as its first argument, so a call reads in the order data flows, with one overload
per shape of that input: `mapLookup(key, map)`, `routeProperty(route, path)`, `intervalLabel(number,
intervals…)`, `tableLookup(key, table, …)`, and `condition(subject, predicate)`, which states the
predicate of its subject. There is no generic composition: a mapping of one's own becomes a block
that takes its input, so the YAML says what it is.

A component passes the context it has: a task passes its vessel, and a picker the vessel it picks
for. A component that has none (a grid
built cell by cell, a regulation judging an action, a gear fishing, a table's columns) has its
factory take the value with `Void` as its context, so that a value reading a vessel is refused when
the scenario is compiled, and convert it once, when it builds, into the plain `Supplier`,
`Predicate` or `Function` the component holds (`WithoutContext`). Numbers are boxed: there are no
primitive shapes.

The package's `Factories` class offers the live version plus an overload taking a plain value
that it wraps in `constant(...)`:

- `lessThan(double)` and `lessThan(ValueFactory)` in `core/predicates/comparable`;
- `dateTimeAfter(referenceDateTime, DurationFactory)` and
  `dateTimeAfter(referenceDateTime, ValueFactory)` in `core/providers/temporal`.

Use this shape for new building blocks even when no caller needs a varying value yet. Reading a
`Constant` costs a call that the JIT inlines; it doesn't show next to the work around it.

What counts as a plain value depends on the YAML:

- a `double`, `int`, `boolean`, `String` or enum is written to the YAML as it is, so the overload
  takes it directly;
- a `java.time` value, a `Quantity` or another object without a builder is not: the YAML would
  hold whatever its getters give, and loading it would fail. The overload takes its value factory instead (`DurationFactory`, `PeriodFactory`,
  `TimeFactory`…), which is what scenario code holds anyway (`hours(3)`).

Never write the overload as a generic `foo(T value)` for such types: `foo(hours(3))` would then
compile with `T = DurationFactory` and wrap the factory itself as the value. Nor should it take a
plain `Factory<? super S, ? extends X>`: a value factory is a factory too, so the two overloads
would differ only in how specific they are. A concrete value-factory class such as
`DurationFactory` is unambiguous.

This doesn't apply to the configuration literals of the previous section (a column name, a
separator, a file path): they say how to build or read something, not how the model behaves, and
stay plain fields.

## Determinism

A simulation's results depend only on its scenario and its seed: the same seed gives the same
results in one JVM or across JVMs, whatever ran before it. Code that runs during a simulation
therefore never iterates a collection whose order depends on identity hash codes:

- a `HashMap` or `HashSet` keyed by a class without its own `equals`/`hashCode` (`Vessel`,
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
