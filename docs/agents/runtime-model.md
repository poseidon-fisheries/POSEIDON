# Runtime model

How the framework turns a scenario into a running simulation. The rules for
writing factories and scenarios are in `architecture.md`.

## Product stores

`AbstractFactory` (`core/.../AbstractFactory.java`) builds a factory's product
once per owner and keeps it in a `ProductStore` keyed by factory identity: the
simulation's store for a simulation-level factory, the vessel's for a
vessel-level one, and the factory's own store for a global one. Sharing is
declared by two things only: the factory's scope, and referencing the same
factory object from several places. Two distinct factory objects never share a
product, however equal their values, so a copy made by derivation builds its
own. A simulation's store ends when the simulation finishes, and refuses any
later build. Ending a store first runs the teardowns of the registrations made
for its products, the last made first.

## Levels and scopes

A factory's **level** says which owner its product belongs to: global (`Scope`),
a simulation (`SimulationScope`) or a vessel (`VesselScope`). A level base class
declares it (`GlobalScopeFactory`, `SimulationScopeFactory`,
`VesselScopeFactory`); a `RelativeScopeFactory` derives it. `get` refuses a
scope wider than the factory's level, and widens a narrower one by constructing
a scope of exactly that level over the same owners, so a build receives nothing
a narrower scope offers, and its registrations go to the store that holds its
product.

## Simulations

`Scenario.startNewSimulation(...)` builds a `Simulation` by resolving every
component factory against a fresh `SimulationScope`. `Simulation` extends
MASON's `SimState` and owns the `TemporalSchedule`, an `EventManager`, and the
list of resolved components; `finalProcesses` run at `finish()`.

## Vessel components

A **vessel component** (its gear, engine, fuel tank, hold, behaviour, option
values, learners, recorders) is made once, with `vesselComponent(factory)`, or
varying by one vessel attribute, and the component is what consumers take:
`fishing(gear, hold, fuelTank, ...)`, `refuel(fuelTank, ...)`,
`componentProperty(hold, "availableCapacityInKg")`. A consumer reads what the
component built for the vessel when it uses it, with `gear.of(vessel)` (or
`findIn` where absence is normal). A component is also a value of a vessel (a
global `ValueFactory` whose product reads the vessel's component), so it goes
wherever a vessel value is expected: a picker takes the option values' component
as the option values of the vessel it picks for.

The vessel builds the components its vessel specification lists, each under a
`VesselComponentScope` carrying a store of its own, so each has a lifetime of
its own, and keeps what each built under it. Components are compared by
identity, as the keys they are. What holds the working state of one component
(tasks, the behaviour, holds, learners, listener wrappers) extends
`VesselComponentScopeFactory`; what a vessel knows across components (memories,
option values) is vessel-level.

A varying component
(`varyingBy("main_fishing_gear", none(), when("PS", purseSeine), when("OTB", bottomTrawl))`)
maps values of one required vessel attribute to variants: a factory or
deliberate absence, with an optional default. Duplicate keys and mixed key types
fail when the component is constructed. Missing attributes, wrong attribute
types on a miss, and unlisted values without a default fail when the vessel is
given the component. With no listed values, there is no key type to check, but
the attribute is still required. `Variant` is sealed to `FactoryVariant` and
`NoComponent`; absence is not a factory. A varying component reads an attribute
only, never another component, and a component property drawn by `oneValueFrom`
is drawn when its component is built.

Only the vessel builds what a component's factory or variants build: building
the component itself builds only its reader. Before a simulation starts, the
description's `ScenarioCheck`s run over every node: a component some consumer
uses but no vessel specification lists fails then, naming the consumer.

## Vessel creation

The **vessel specification**
(`vesselSpecification(fleet, behaviour, namedComponent(...)...)`, one call whose
trailing named components keep the order they are built in) is what a vessel of
the scenario is made of, and the only thing that creates vessels: a creation
source (`createVesselsAtPort`, the vessel register) asks it to create each
vessel from an id, an `Optional<String>` home-port code and attributes. The
specification holds no attribute names: each source states the home port itself,
and a vessel's label is its id. The vessel resolves the code against its port
grid once, when it is given, and keeps a non-null `Optional<Port>` and
`Optional<Cell>`, present only for a modelled port; no code, or a code naming no
port, leaves them empty and the vessel absent. A creation source supplies the
code and never a `Port`, so the port grid is the only route to a home port.
`createVesselsAtPort` rejects, when it is built, a port its vessels' port grid
does not hold (that very port, not one with the same code), then supplies the
port's code. The `homePort()` value gives a plain `Port` and fails, naming the
vessel, if there is none, as do `HomePortCatchValuation` and
`RouteViaDestination`: they run inside a present vessel's behaviour.

Creation runs in a fixed order: the vessel joins the fleet with an account,
takes its home port and attributes (which place it at its home port, if it has
one), and builds its components, the behaviour first, then the others as listed;
only then does the fleet announce its entry to its entry listeners
(`addEntryListener`), so a listener only ever sees a complete vessel, before its
first step.

## Statements, rebuilds and exits

A source states things about an existing vessel through the fleet: an
`AttributeUpdate` (values for the attributes it names; a missing value removes
one; unnamed attributes are untouched), a `HomePortUpdate` (an optional code; an
empty one clears the home port, and no statement leaves it unchanged), or its
exit. The two updates are separate, so a source can state either alone;
statements and fleet submission are package-private. A statement joins the
vessel's queue and takes effect at the start of the vessel's next step in which
its behaviour is not running, never inside the call that made it; one queued on
a vessel with no step pending schedules one at once. Scheduling wrappers, and so
statements and creations, run at ordering 0, and vessel steps at ordering 1:
every statement due at an instant is queued before any vessel steps then, and a
vessel takes its first step at the instant it is created. A vessel's account
changes at once, from any call.

The vessel compares what a source states with its current state, since another
source may already have applied it: once the queue is applied, it rebuilds every
component that varies by an attribute whose value actually changed, even if the
same variant is picked: their stores end in the reverse of the build order, then
they are built in that order, each under a fresh store; every other component,
the vessel's own store (its memories) and its account stay. A hold that still
holds catch refuses to have its store ended, so a rebuild only ever drops an
empty hold.

Once the queue is applied, a vessel is absent exactly when it has no modelled
home port; see "Whereabouts". An exit ends the queue: the vessel checks it is
not at sea, ends its component stores in the reverse of the build order, then
its own store; it leaves the vessel field and becomes absent, announcing
`BecameAbsentEvent` if it was docked when the queue started; then it leaves the
fleet, which announces the exit to its exit listeners (`addExitListener`) and
keeps the id with its exit date, so that a later statement about it is an error
and ids are never reused.

## Whereabouts

A vessel's `Whereabouts` are exactly one of `Absent`, `Docked` (holding the
port) or `AtSea`; the vessel field agrees, and an absent vessel is off it.
Absent is neither docked nor at sea: it has no position, so `getCell`,
`getPoint` and `getCoordinate` fail on it, `setCurrentCell` requires at sea, and
`isActive()` means present and having a behaviour. Every transition is checked
against the current whereabouts: undock (docked to at sea, `UndockingEvent`),
dock (at sea to docked, `DockingEvent`), docked to absent (`BecameAbsentEvent`)
and absent to docked (`BecamePresentEvent`). At sea to absent is an error, and
absent to at sea has no path. `isDocked()`, `isDockedAt(port)` and
`getPortDockedAt()` read the whereabouts; the last fails unless docked.

A vessel is created absent, and placed docked at its home port, silently, if its
home-port code names a port in the port grid. Becoming absent and becoming
present follow from the statements: a vessel whose code matches no port, or
whose home port is cleared, is absent; one whose home port becomes a modelled
port again becomes present, docked there. A move between modelled home ports
leaves the vessel where it is and announces nothing: whether and when it travels
to its new home port is up to the scenario's behaviour. The vessel compares its
whereabouts before and after its whole queue, announces at most one net change
after the rebuild, and checks the at-sea error on the net result. Both events
carry the vessel, the date and the port, go on the vessel's own event manager
(which forwards to the fleet's), and are distinct from docking events, so trip
and effort listeners never read them as departures or arrivals. The date is the
step's, which `StepScheduler` passes to the vessel; the vessel holds no clock.
Both `DockingEvent` and `BecamePresentEvent` are a `PortStayStartEvent`, which
`CurrentPortStayStartDateTime` listens for to restart the port stay.

A fleet-wide listener (`addEntryListener`) sees a vessel's whereabouts at entry
as its starting state: creation announces nothing. The events give every change
after that, including the exit of a docked vessel, whose `BecameAbsentEvent`
reaches fleet-level listeners before the fleet's exit announcement.

The vessel register
(`scheduledByDateTime(vesselRegister(table, vesselSpecification, columns, eventCodes))`)
turns each row into dated statements. `columns` (`vesselRegisterColumns(...)`)
names the vessel id, event code, event date and home-port columns and lists the
attribute columns; `eventCodes` (`vesselRegisterEventCodes(...)`) lists the
creation, change and exit codes. The register reads the home-port code from its
column and states it as the home port; only listed columns become attributes, so
the home-port column is one only if listed. Creation supplies the row's
attributes and code. Each later change row is compared with that vessel's
previous register row, not with the vessel's live state: changed attributes
yield an `AttributeUpdate`, a changed code a `HomePortUpdate`, unchanged parts
nothing, and becoming missing is a change. So a row changing only the length
cannot undo a home port another source set. The register folds each vessel's
rows dated at or before the start into one creation at the start, home port
included, and a vessel that exited by then is never created, its exit recorded
with the fleet. A code matching no port in the grid, or a missing one, leaves
the vessel absent.

## Registration

A build registers through its scope, never directly. In a vessel scope,
`addListener` registers on the vessel's event manager and
`register(registry, item)` registers in a `VesselRegistry` (the fishing event
sharer), which takes the vessel from the scope and returns its own teardown; the
scope gives each teardown to the vessel's store. In a simulation scope,
`addListener` registers on the simulation's event manager with no teardown: the
simulation's store ends only at the finish, which takes nothing back. The global
scope offers no way to register.

## Simulation services

The simulation's services are inputs: its random number generator (`rng()`), its
clock (`currentDateTime()`, `hasDateTime()`, `startingDateTime()`), its event
manager (`eventManager()`) and the run's id (`runId()`). A scope does not offer
the simulation to a build: `SimulationScope`'s accessor is package-private, and
the only readers, all in `core`, are the services' own factories, the store
routing (`SimulationScopeFactory`, `PerSimulationFactory`), the scope's
registration methods and the `SchedulingScope`. A service is simulation-level,
so a relative factory that takes one is at least simulation-level.

## Scheduling

Scheduling is not something a scope offers. Only scheduling factories schedule:
core's scheduling wrappers (`scheduledOnce`, `scheduledRepeating`,
`scheduledByDateTime` and the rest, in `core/.../schedule/Factories.java`) and
the fleet factories. They extend `SchedulingFactory`, whose build receives a
`SchedulingScope` offering the simulation's schedule. A
`SimulationScopeFactory`'s build always receives a plain `SimulationScope`, and
a wrapper's input is typed `Factory<? super SimulationScope, ...>`, which no
factory typed in `SchedulingScope` is, so the steppable a wrapper schedules
cannot schedule anything itself.
