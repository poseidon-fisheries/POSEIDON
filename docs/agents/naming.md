# Naming

Names are one of the strongest signals of whether a design is good: a name that
is hard to find, or that fits two things, usually means the parts are cut
wrongly.

## Read as English

Names read like proper English: code reads as sentences. Put verbs first for
tasks and actions (`startPurseSeinerTrip`, not `purseSeinerStartTrip`;
`chooseNextPurseSeinerDestination`), and keep noun phrases grammatical for
things (`purseSeinerFishingLocationAtSeaChecker`). Tests read as statements
whose subject is the thing tested, never what it is tested against: left unsaid
(`lessThan(threshold)`, `withinTimeWindow(timeWindow)`) or named
(`dateTimeIsBefore(referenceDateTime)`, `vesselIsDockedAt(port)`,
`cellWasFishedThisTrip()`). Read the call with the thing tested in front: "a
date-time is within the time window", "the vessel is docked at the port"; the
sentence must be true exactly when the test is. Then check that the call site
reads naturally, for example `not(cellWasFishedThisTrip())`.
[architecture.md](architecture.md) says why tests are built without their
subject.

## Name the thing, not an adjective

Name the thing itself, as a noun or a noun with its qualifier (`keyedMemory`,
`memory`, `sharedEvent`, `keptCatch`), even for a short-lived local variable or
a lambda parameter. An adjective or participle on its own (`keyed`, `shared`,
`filtered`, `current`, `kept`, `remaining`) is a dangling name in any context.
Judge related names together and preserve their balance when renaming one.
Checkstyle catches known cases in `config/checkstyle/checkstyle.xml`; the prose
rule applies to every name, including those outside that list.

## Units

A quantity held as a bare number names its unit (`freeCapacityInKg`,
`getTotalBiomassInKg`). A quantity whose type carries the unit
(`Quantity<Mass>`, `Biomass`, `Duration`) takes a plain name (`capacity`).

## Mapping arguments

The second `apply` parameter of a mapping names its input. When its declared
type is a concrete domain type, name it after that type (`dateTime`, `date`,
`cell`, `vessel`). When the type is a type variable or `Object`, name it
`argument`.

## Naming audit and Checkstyle upkeep

Before finishing an implementation or review, inventory every new or renamed
identifier and check each against all the naming rules. Include lambda
parameters, pattern variables and test helpers. A naming audit is complete only
when every identifier in that inventory has been checked. Skip manual checks
only for the exact cases tooling enforces, rather than the wider rule.

Rename an identifier with the LSP rename tool, or by searching its whole file,
so every use moves with it.

For each naming violation found while writing or reviewing code, check whether
Checkstyle catches it. If a missed case is a reusable naming failure likely to
recur, search its identifier uses in POSEIDON and SURIMI-POSEIDON and assess
false positives. Add an exact-name restriction where it fits, rename affected
identifiers in the same change, and run the relevant Checkstyle checks. Scope
the restriction to the kinds of identifier where the word is unsuitable: a word
used as a verb can still fit a method name. Keep unusual or contextual cases in
the manual audit; explain when a proposed ban would reject valid names. Use the
published Checkstyle checks and their options.

## Cell and location

Name anything that is a grid cell (`Int2D`) with "cell" (`getHomePortCell`,
`fishingCell`). Keep "location" for the general notion that could be a
continuous coordinate or a cell. Third-party APIs keep their own names (MASON's
`setObjectLocation`).

## Fields

Name a field after its full domain type in lower camel case (`OptionValues` →
`optionValues`, `CatchCategoriser` → `catchCategoriser`), with a role qualifier
when two fields of a class have the same type. A random number generator is
`rng` everywhere: fields, parameters, local variables and factory helpers.

## Function-valued fields

- A bare noun of the value is fine when the function reads a value off its
  context (`hourlyCost`: vessel → its hourly cost, like `mortalityRate` and
  `referenceDateTime`).
- When the function maps an argument to a result, a bare noun misleads
  (`getRoute()` suggests one `Route`): use a light role name (`routeFunction`,
  following `actionFunction`) or the domain concept's name (`routeCosts`).
- Don't mark a name as a function: the type and `.apply(...)` show it. When a
  component holds both a function and the value it got from it, the value takes
  the plain name and the function takes `…Source` (`TravelAlongPath`:
  `destinationSource` and `destination`). Factory fields and helper parameters,
  which only hold the function, keep the plain name. First, avoid holding the
  value at all when it need not be read before use.
- Keep similar concepts visibly distinct (the trip cost paid, the travel cost in
  the valuation, the route cost).

## Factories and their products

A factory is named after its product: `Product` is built by `ProductFactory`. An
explicitly agreed plain-function product may use the name of its role. When a
factory starts building something else, rename it to match.

A name says what its product is used for, as a guideline rather than a rule. A
product used for what it gives back (a grid, a price, a vessel function) takes a
noun naming that thing. A product called only for its side effects, such as a
`Steppable` or a task, should not take a plain noun (`vesselsAtPort`), which
reads as if it gave back the things it names. It takes a verb when it is a
one-off action (`createVesselsAtPort`, `landCatches`), and the name of the role
that acts when it is an ongoing process (`fixedCostCollector`,
`biomassDiffuser`). A noun can still fit a wrapper that arranges other
steppables or tasks (`steppableSequence`), or a product that steps and also
gives something back (a table it fills).
