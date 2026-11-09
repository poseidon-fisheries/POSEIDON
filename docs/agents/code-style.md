# Code style

The short rules (`final` everywhere, Prettier, one `return` per method) are in `AGENTS.md`. This
document holds their detail and the rules that only some code reaches.

## Formatting

Formatting is Prettier's (prettier-plugin-java, options in `.prettierrc`), applied by
`./gradlew spotlessApply`.

- Prettier leaves comments as they are: wrap them by hand within 80 columns.
- It moves a comment placed at the start of a lambda body down the method chain, so put such a
  comment above the call that takes the lambda.
- SURIMI-POSEIDON duplicates this setup: `.prettierrc`, `package.json`, the Spotless block and
  license header in `build.gradle.kts`, the `nodejs`/`node-gradle`/`spotless` versions in
  `libs.versions.toml`, the Node.js repository in `settings.gradle.kts`, and the `buildscript`
  block in `build.gradle.kts` that pins node-gradle's Jackson 2 with the `jackson2` version. The
  `checkstyle` version in `libs.versions.toml` is duplicated too. Change both repos together.

## Lambdas and scenario classes

Prefer lambdas to anonymous classes, and method references to lambdas, wherever they fit. For a
`Listener`, which has two abstract methods, use `new SimpleListener<>(EventClass.class, consumer)`.

Scenario classes (such as `PeterSnapperScenario`, or `NorthwesternMedScenario` in SURIMI-POSEIDON)
are the exception: they stay readable for people new to Java, with no method references or other
clever tricks; safety nets belong in tests, not in call-site syntax. Their locals are declared
`final var`, so a scenario reads as a list of named parts, not a list of types. If a `var` does not
compile because Java cannot infer a type, the fault lies in the factory helper, not the scenario: a
helper whose type parameter appears only in its return type leaves it unbound. Fix the helper so
its arguments or its name fix the type (as `exponentialMovingAverageCellValues` does), never by
giving the scenario local an explicit type.

Mechanism (wrapping, scoping, readers) belongs in factories and framework code, never on the
scenario author. Judge a factory helper by how its call site reads to a novice: plain calls with
strings and simple values (`vesselProperty("gear.code")`) beat method references (`Vessel::getGear`),
even when the clever version buys compile-time safety, and a call site that needs type witnesses is
not simple. Put the safety net in a test that builds the scenario.

## `Factories` helper names

Name a `Factories` helper after the class its factory builds, in lowerCamelCase (`GreedyPicker` →
`greedyPicker`). A helper may deviate only for a stated reason, such as `epsilonGreedyDestination`,
a specialised helper that spares scenarios type witnesses; deviations are the exception, not the
rule. A helper named after a role rather than its class (`neighbourhoodGridExplorer` building a
`NeighbourhoodCellPicker`) is a mismatch to fix.

## Streams

Lean towards streams rather than loops and Guava's collection views (`Maps.transformValues`,
`Lists.transform`): build an immutable map with `collect(toImmutableMap(...))`, for instance, rather
than `ImmutableMap.copyOf(Maps.transformValues(...))`, and build a value rather than accumulating
into a mutable one. This is a preference, not a rule. A loop is fine where it reads better: filling
or transposing an array by index, a state machine such as a character scanner, or a cursor API like
StAX. If a stream needs nested `IntStream`s or index arithmetic to do what a plain loop says
directly, keep the loop. A loop is also fine on a hot path where a profile shows a stream's
overhead; code that runs once, such as a constructor, is not a hot path.

## Single exit

A method or lambda has at most one `return`, as its last statement, and a void one has none;
Checkstyle's `ReturnCount` check enforces this.

- Guard clauses throw (`checkArgument`, `checkState`) rather than return.
- For a chain of conditions, assign a `final` local in an `if`/`else if`/`else` chain, or use a
  switch expression, and return it once. Where a choice between two values fits a single ternary,
  prefer the ternary to an `if`/`else`; never nest ternaries.
- Don't carry "done" out of a loop in a flag: write a search as a stream (`anyMatch`,
  `filter(...).findFirst()`), and a loop that stops on an event as recursion over a `switch`.
- In a `forEach` lambda, invert the condition rather than `return` to skip an element.
- A loop that runs a fixed, small number of times is written out as its steps.
- When a change alters what a method does, rewrite the method as the new sequence of steps rather
  than wrapping the old body.
- On a hot path, such as the buckets, market quotes, catch valuation or the legality check's
  predicate combinators (anything run once per option the legality check tests), keep a loop with
  early returns where a stream would cost measurably, and mark the method
  `@SuppressWarnings("checkstyle:ReturnCount")`, so the exception is stated where it is made.
