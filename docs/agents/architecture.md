# Core architectural pattern: Factory + Scenario

This document holds the rules for writing factories, scenarios and model time
values. How the framework runs them (product stores, levels and scopes, vessel
components, vessel creation and statements, registration, services, scheduling)
is in `runtime-model.md`.

The model is built around a declarative configuration → runtime object pattern:

- `Factory<S extends Scope, P>` (`core/.../Factory.java`) is a `get(scope) -> P`
  producer. Most simulation components (species, regulations, vessels,
  allocators, providers…) are expressed as a `Factory` implementation plus a
  plain value/config class, so that scenarios can be built either
  programmatically or deserialized from YAML.
- Scenario-building code composes static helpers only — nested calls all the way
  down. A helper takes all its arguments and returns a finished scenario part,
  which is a factory; scenario code calls nothing on a helper's result. Never
  call `.get(scope)`, `.apply(...)`, or `.test(...)` directly in
  scenario-building code; resolving the graph is the framework's job at
  simulation-start time, not something scenario code does itself.
- When creating a new factory, extend `AbstractFactory`
  (`core/.../AbstractFactory.java`) rather than implementing `Factory` directly
  unless you have a specific reason not to.
- **`RelativeScopeFactory` is the default level for every factory, and a fixed
  level (`GlobalScopeFactory`, `SimulationScopeFactory`, `VesselScopeFactory`)
  needs a reason tying the product to it.** There are three:
  - **the product holds mutable state** of its own (a memory, a network, an
    accumulator). A relative factory whose inputs are all global would be global
    too, and would share that state across simulations. A product that only
    holds a reference to a mutable object it was given isn't stateful on that
    account: the state belongs to whatever built that object. A cache is not
    state of a run: its values depend only on the factory's inputs, so a
    relative factory is right and gives the cache the level of its inputs
    (`DefaultPathCacheFactory`). Everything the cached values depend on must
    then be an input of the factory, even one it never reads, so that a derived
    scenario gets a new cache, and the cache must be safe to use from several
    threads, since the simulations sharing it may run in parallel. A shared
    cache lives as long as its factory. A cache that is only fast from one
    thread, such as a fastutil map, keeps a fixed simulation level
    (`PathLengthCalculatorFactory`);
  - **`newInstance` uses the scope itself**: it registers its product through
    the scope (`scope.addListener`, `scope.register`, `scope.addFinalProcess`).
    A relative factory with global inputs would build it once, from the first
    scope it was given;
  - **`newInstance` has side effects**, such as changing the objects its inputs
    give it (`InitialPricesFactory` sets the prices of the markets it is given;
    `oneValueFrom` draws from the random number generator). The scope decides
    how many times the side effect runs, so it shouldn't depend on what the
    inputs happen to resolve at.

  A relative factory takes the narrowest level of its inputs, global if it has
  none; which properties count as its inputs is in `RelativeScopeFactory`'s
  Javadoc. Type it `<S extends Scope>`, like `NetOptionValuesFactory`.

- `Scenario` is an immutable (Lombok `@Builder`) bag of named component
  `Factory` instances plus a starting date/time. A scenario that differs in some
  value is derived from it, leaving it untouched: `with(address, value)`
  replaces the value at an address that starts at a component's name
  (commons-beanutils syntax, e.g. `gear.proportion`), copying every factory
  above the replacement and keeping every other one, so global products stay
  shared; `withComponent(name, factory)` adds a component, and
  `withStartingDateTime(start)` moves the start. Calibration derives one
  scenario per candidate. A run is started with its seed and, optionally, its
  id: `startNewSimulation(seed[, simulationId])`.
- Consumers take a **vessel component** (`vesselComponent(factory)` or
  `varyingBy`), not what it builds: see "Vessel components" in
  `runtime-model.md`.
- **Nothing goes on `Vessel` unless every possible model needs it.** State or
  queries that only some models need (a chosen destination, the current fishing
  trip, whether a fishing trip started today) live in vessel components, or in
  other objects built for the models that need them, never as fields or methods
  of the vessel.
- A factory that draws random numbers, reads the date, publishes events or needs
  the run's id takes the simulation service's factory (`rng()`,
  `currentDateTime()`, `eventManager()`, `runId()`) as an input, which its
  helper passes, so scenario code never names it.
- What runs when is said in the scenario, by wrapping a steppable in one of
  core's scheduling wrappers (`scheduledOnce`, `scheduledRepeating`,
  `scheduledByDateTime` and the rest); see "Scheduling" in `runtime-model.md`.
- Scenarios are serialized to/from YAML via SnakeYAML (`io/.../ScenarioLoader`,
  `ScenarioWriter`); `ScenarioLoader` restricts deserializable classes to
  configured prefixes (`uk.ac.ox.poseidon`, `java`, plus caller-supplied extras)
  as a safety measure — keep this in mind if adding factories in new packages
  that scenarios need to reference.
- **Every `Scenario` must remain serializable to YAML.** Anything hung off a
  `Scenario` (components, factories, nested config) has to survive a SnakeYAML
  round-trip — don't introduce fields/types that can't be represented that way.
- **YAML scenario files are output of their writer task**
  (`writePeterSnapperScenario`). To change one, change the Java that generates
  it and rerun the writer; a rename is a Java rename, then a rerun. Commit
  whatever the writer produces with the Java change: the YAML needs no review,
  and drift from earlier changes goes in the same commit. Never edit the
  checked-in file, by hand or by script, even when the result would be
  identical.
- **When building a `Scenario`, use the static helper methods of each package's
  `Factories` class, not `new SomeFactory(...)` or a factory's constructor
  directly.**
- **In scenario-building code, write a factory inline where it is used, and give
  it a local variable only when:**
  - **it is used more than once.** This is about identity, not tidiness:
    products are stored by factory identity, so two inline copies of the same
    expression build two different objects. When an object must be shared (e.g.
    one per-vessel memory that both a learner and the view read), a single named
    instance is what makes it shared, and inlining it would silently duplicate
    it;
  - **it is a root-level component** of the `Scenario`
    (`.component("name", ...)`);
  - **something outside the scenario needs to reach it**, such as the GUI or a
    calibration (by component name or property path).

When adding a new simulation component (a new kind of provider, allocator,
regulation, etc.), follow the existing pattern in the relevant package: a plain
class implementing the domain interface, plus a `*Factory` that YAML scenarios
instantiate, and a static helper method added to that package's `Factories`
class for programmatic construction.

## Factory class shape

`*Factory` classes are immutable: their fields are final and they have no
setters. Each has one constructor and a Lombok builder:

- an all-args constructor, used by the package's static `Factories` helper
  method;
- `@SuperBuilder(toBuilder = true)`: `ScenarioLoader` builds factories through
  it, and `toBuilder()` makes a changed copy.

Don't add other constructors to a factory class, except the hand-written builder
constructor some factories need.

Validate in the constructor, including that hand-written builder constructor:
the builder calls it, so construction and YAML loading reject the same inputs.

A scenario has one kind of part: a factory or a literal (a scalar, an enum, a
list or a map). Every builder property of a factory is a factory, a literal, or
a list or map of these, recursively; `FactoryPropertiesTest` checks it.

A factory resolves dependencies and constructs its product. Named runtime
calculations and extractors have concrete product classes, including when they
implement `Function` or another functional interface. Their behaviour and
behavioural documentation belong on the product; the factory carries the
construction and its pointer documentation. This applies to one-line extractors
too. Returned lambdas and method references are appropriate for forwarding to
behaviour already defined elsewhere, or for explicitly agreed plain-function
products: for example, `MemoryFactory` returns a function that always gives the
same memory entries.

When designing a new factory, favor composition over a new bespoke
implementation: check whether existing factories (in the same package or in
`core`, e.g. constant/random/temporal providers) can be combined or
parameterized to produce the desired behavior before writing a new `Factory`
class from scratch.

Compose at the level of the parts a scenario wires: each piece of a composition
should mean something on its own to a reader of scenario code, and be used, or
plausibly usable, in more than one composition. Don't split a part into internal
pieces that only one class uses, or that only forward; keep such a piece inside
the part, as a field or a private method.

Type parameters keep one meaning throughout: `S` the scope, `P` a factory's
product, `C` the context of a live value, `T` the thing a mapping maps (or,
outside the two shapes, the main type a class is about), `R` a result.

Not every field on a factory needs to be `Factory`-typed. Wrap a field in
`Factory<? super S, ? extends X>` only when its value must be _resolved against
the scope_ — a nested component (`ModelGrid`, a species list), something
computed at build time, or a value that legitimately varies per scope. A plain
literal that's just configuration — a `String`, `double`, `int`, `boolean`, an
enum — stays a plain field, exactly like `separator` on
`TimeIndexedBiomassGridsFromNetCdfFactory`. Wrapping a literal in
`Factory<String>` "for consistency" or "to be safe" is over-engineering: it adds
a layer of indirection nothing resolves differently through, and every call site
has to write `scope -> "value"` for no gain.

## Live parameters, with a plain-value overload

A building block reads its model parameters (a threshold, a duration, an amount
of time, a rate) each time it is used, rather than fixing them when it is built.
It then works the same whether the value is fixed, depends on the date, the
vessel or the state of the simulation, or is changed mid-run, and any value in
`core/providers` can feed it.

A live parameter is a function of a _context_. It comes in two shapes, each with
its factory type in `core`:

- a _value_ of the context, `Function<C, R>`, built by a `ValueFactory<S, C, R>`
  (a constant, the current date-time, a property of the vessel);
- a _mapping_ of something under the context, `BiFunction<C, T, R>`, built by a
  `MappingFactory<S, C, T, R>` (a comparison with a threshold, a cell's value, a
  property of a route).

The context is always the vessel the call is for. Whatever reads a vessel (a
reader of its state such as `currentCell()` or `vesselProperty(...)`, a route, a
cell checker, a time cost, a valuation, a picker) takes it on each call and
holds none, so one serves every vessel. A test relating it to another vessel,
such as a friendship condition, is a mapping of that other vessel, and reads
values off it with `other(...)`:
`condition(other(vesselProperty("homePort")), equal(vesselProperty("homePort")))`
holds when the other vessel's home port is the vessel's. A value without
`other(...)` reads the vessel the call is for, and leaving it out where a
mapping is expected does not compile. A component without a vessel (a grid built
cell by cell, a regulation judging an action, a gear fishing, a table's columns)
has its factory take the value with `Void` as its context, so that a value
reading a vessel is refused when the scenario is compiled, and convert it once,
when it builds, into the plain `Supplier`, `Predicate` or `Function` the
component holds (`VoidContextConversions`). Numbers are boxed: there are no
primitive shapes.

A yes/no is a value or a mapping with a `Boolean` result. A value or mapping
that reads nothing from its context has `Object` as its context, so it fits
wherever any context is given. A fixed value is `constant(x)` where a value is
expected and `always(x)` where a mapping is (a filter, a part of a key, a term
of a sum over routes); the compiler refuses the wrong one. A combinator that
makes sense in both shapes (`condition`, `allOf`, `anyOf`, `not`,
`combinedDuration`…) has one overload per shape; a helper method returning a
live value must therefore declare its shape type, or it matches no overload.
Such a combinator has one implementation, its mapping, so it means the same in
both shapes: its value overload builds the mapping over its value arguments,
each made a mapping of `Void` that ignores what it maps (`ValueAsMapping`), and
makes the result a value by giving it `null` as the thing mapped
(`MappingAsValue`).

A block that transforms what it is given takes it as its first argument, so a
call reads in the order data flows, with one overload per shape of that input:
`mapLookup(key, map)`, or `condition(subject, predicate)`, which states the
predicate of its subject. There is no generic composition: a mapping of one's
own becomes a block that takes its input, so the YAML says what it is.

The package's `Factories` class offers the live version plus an overload taking
a plain value that it wraps in `constant(...)`:

- `lessThan(double)` and `lessThan(ValueFactory)` in
  `core/predicates/comparable`;
- `dateTimeAfter(referenceDateTime, DurationFactory)` and
  `dateTimeAfter(referenceDateTime, ValueFactory)` in `core/providers/temporal`.

Use this shape for new building blocks even when no caller needs a varying value
yet: reading a `Constant` costs a call that the JIT inlines.

What counts as a plain value depends on the YAML:

- a `double`, `int`, `boolean`, `String` or enum is written to the YAML as it
  is, so the overload takes it directly;
- a `java.time` value, a `Quantity` or another object without a builder is not:
  the YAML would hold whatever its getters give, and loading it would fail. The
  overload takes its value factory instead (`DurationFactory`, `PeriodFactory`,
  `TimeFactory`…), which is what scenario code holds anyway (`hours(3)`).

Never write the overload as a generic `foo(T value)` for such types:
`foo(hours(3))` would then compile with `T = DurationFactory` and wrap the
factory itself as the value. Nor should it take a plain
`Factory<? super S, ? extends X>`: a value factory is a factory too, so the two
overloads would differ only in how specific they are. A concrete value-factory
class such as `DurationFactory` is unambiguous.

This doesn't apply to the configuration literals of the previous section (a
column name, a separator, a file path): they say how to build or read something,
not how the model behaves, and stay plain fields.

A live parameter is validated where it is read, on every call: a component
cannot validate it when built, since it cannot tell whether the value it was
given is fixed. This is cheap for a comparison — `EpsilonGreedyChooser` checks
that a vessel's epsilon is in `[0, 1]` on every destination pick — but not for
work such as sorting the times of day or building a per-species array, where the
cost is real and worth weighing before going live. A bad value then fails on
first use rather than when the scenario is built.

## Read mutable state at the point of use

A component that reads a field off a mutable object (a vessel, a trip, market
state) resolves it inside the method that needs it (`receive(event)`), not at
construction or registration. A value that can legitimately be absent when the
component is built, or corrected later in the object's life
(`Vessel.getHomePort()` is null until a later `FleetEvent` modification sets
it), would otherwise be cached too early, and the bug surfaces far from where
the value was captured. Failing loudly and early is no reason to resolve early.

## Model time precision

Model date-times, times of day and durations have whole-second precision.
Produce whole-second temporal values at their source. Reject supplied temporal
values with a nonzero nanosecond part rather than silently truncating or
rounding them. Validate fixed temporal arguments at construction and live
temporal values on every read, since a live value can become invalid after
construction. This rule applies to model time; MASON's internal scheduler time
remains an internal representation.

## Sharing data across simulations

A type meant to be read-only and shared globally (e.g. across every simulation
run from one loaded scenario) must not implement a mutable-by-contract interface
like `MutableGrid` — even if its own methods never mutate,
`MutableGrid.getField()` (or any analogous accessor) hands out a live mutable
handle to the backing structure, so the object is never actually immutable.
Follow the existing pattern instead: a small read-only sibling type that wraps
the mutable implementation without implementing its mutable interface (e.g.
`CarryingCapacityGrid` / `ImmutableBiomassGrid`, both
`extends DoubleGridWrapper` rather than implementing `BiomassGrid`).

Prefer Guava's immutable collection types (`ImmutableMap`, `ImmutableList`,
`ImmutableSet`, etc.) over mutable JDK collections (`HashMap`, `ArrayList`,
`HashSet`) for factory-produced results and other data that shouldn't change
after construction — this is already the convention for data shared across
simulations or handed out from a resolved `Factory` (e.g.
`FisheableBiomassGrids`'s use of `ImmutableMap`). Reach for mutable collections
only for genuinely local, mutable working state inside a method body.

## Determinism

A simulation's results depend only on its scenario and its seed: the same seed
gives the same results in one JVM or across JVMs, whatever ran before it. Code
that runs during a simulation therefore never iterates a collection whose order
depends on identity hash codes:

- a `HashMap` or `HashSet` keyed by a class without its own `equals`/`hashCode`
  (`Vessel`, `Market`, `Fleet`, most components) iterates in an order that
  changes from run to run, because identity hash codes depend on how many the
  thread has already handed out. Use a `LinkedHashMap` or `LinkedHashSet`
  (insertion order), a Guava immutable collection, or sort by a stable key. A
  hash collection that is only looked up, never iterated, is fine;
- Guava graphs: set `incidentEdgeOrder(ElementOrder.stable())`, since the
  default order of a node's neighbours is unordered (`SocialNetwork`);
- `Collectors.toMap`, `toSet` and `groupingBy` build hash collections: use
  Guava's `toImmutableMap`/`toImmutableSet` or pass an ordered supplier when the
  result is iterated.

Keys with value equality (`String`, `Species`, `Cell`, records, `@Value`
classes, enums) iterate in the same order in every run, so they keep runs
reproducible. Results should still not depend on that order: it follows the
keys' hash codes, which change whenever a key type or its fields do, and a
change that should leave results alone would then alter them. Where the order
matters, use the same remedies as above. Randomness comes only from the
simulation's random number generator. Each example scenario has a test that runs
the same seed twice in one JVM and compares the results
(`PeterSnapperScenarioTest.sameSeedGivesSameLandings`; SURIMI-POSEIDON's
`ScenarioReproducibilityTest`).
