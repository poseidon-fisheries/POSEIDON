# Naming

Names are one of the strongest signals of whether a design is good: a name that
is hard to find, or that fits two things, usually means the parts are cut
wrongly. Settle a name when it comes up.

## Read as English

Names read like proper English: code reads as sentences. Put verbs first for
tasks and actions (`startPurseSeinerTrip`, not `purseSeinerStartTrip`;
`chooseNextPurseSeinerDestination`), and keep noun phrases grammatical for
things (`purseSeinerFishingLocationAtSeaChecker`). Predicates read as statements
(`vesselHasStartedTripToday`, `cellWasFishedThisTrip`). Check that the call site
reads naturally, for example `not(cellWasFishedThisTrip())`.

## Name the thing, not an adjective

Name the thing itself (`keyedMemory`, `memory`, `sharedEvent`) rather than
leaving an adjective or participle dangling (`keyed`, `shared`, `filtered`),
even for a short-lived local variable or a lambda parameter. An adjective can
stand alone when the immediate context supplies an unmistakable noun: for
example, `previous` and `current` in an interval comparison. Judge related names
together and preserve their balance when renaming one. Checkstyle catches known
cases in `config/checkstyle/checkstyle.xml`; the prose rule applies to every
name, including those outside that list.

## Naming audit and Checkstyle upkeep

Before finishing an implementation or review, inventory every new or renamed
identifier and check each against all the naming rules. Include lambda
parameters, pattern variables and test helpers. A naming audit is complete only
when every identifier in that inventory has been checked. Skip manual checks
only for the exact cases tooling enforces, rather than the wider rule.

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
