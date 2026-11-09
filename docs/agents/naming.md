# Naming

Names are one of the strongest signals of whether a design is good: a name that is hard to find, or
that fits two things, usually means the parts are cut wrongly. Settle a name when it comes up.

## Read as English

Names read like proper English: code reads as sentences. Put verbs first for tasks and actions
(`startPurseSeinerTrip`, not `purseSeinerStartTrip`; `chooseNextPurseSeinerDestination`), and keep
noun phrases grammatical for things (`purseSeinerFishingLocationAtSeaChecker`). Predicates read as
statements (`vesselHasStartedTripToday`, `cellWasFishedThisTrip`). Check that the call site reads
naturally, for example `not(cellWasFishedThisTrip())`.

## Name the thing, not an adjective

Don't use a dangling adjective or participle as a noun (`keyed`, `shared`, `filtered`). Name the
thing itself (`keyedMemory`, `memory`, `sharedEvent`), even for a short-lived local variable or a
lambda parameter.

## Cell and location

Name anything that is a grid cell (`Int2D`) with "cell" (`getHomePortCell`, `fishingCell`). Keep
"location" for the general notion that could be a continuous coordinate or a cell. Third-party
APIs keep their own names (MASON's `setObjectLocation`).

## Function-valued fields

- A bare noun of the value is fine when the function reads a value off its context (`hourlyCost`:
  vessel → its hourly cost, like `mortalityRate` and `referenceDateTime`).
- When the function maps an argument to a result, a bare noun misleads (`getRoute()` suggests one
  `Route`): use a light role name (`routeFunction`, following `actionFunction`) or the domain
  concept's name (`routeCosts`).
- Don't mark a name as a function: the type and `.apply(...)` show it. When a component holds both
  a function and the value it got from it, the value takes the plain name and the function takes
  `…Source` (`TravelAlongPath`: `destinationSource` and `destination`). Factory fields and helper
  parameters, which only hold the function, keep the plain name. First, avoid holding the value at
  all when it need not be read before use.
- Keep similar concepts visibly distinct (the trip cost paid, the travel cost in the valuation, the
  route cost).

## Steppables and tasks

A name for a product called only for its side effects is in `architecture.md`, under "Factory class
shape".
