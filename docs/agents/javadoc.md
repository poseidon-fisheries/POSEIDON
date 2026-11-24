# Javadoc conventions

## Layout

Prettier leaves Javadoc as written, so lay it out by hand:

- Wrap at 80 columns. A single `{@link …}` too long to fit gets a line of its
  own.
- Start each new paragraph with `<p>` on its own line.
- Write lists as `<ul>`/`<ol>` with one `<li>` per item, and keep `<pre>` blocks
  verbatim.
- Align the descriptions of consecutive `@param` tags, and hang continuation
  lines under the description:

  ```java
     * @param currentDisposition    the disposition so far, refined further by
     *                              this step
     * @param availableCapacityInKg how much hold capacity remains
     * @return the disposition after this step, starting from {@code grossCatch}
     *         entirely retained
  ```

## The component/`Factory`/`Factories` triplet

The pattern itself is in `architecture.md`. Each part of the triplet is
documented in one place, and every doc comment except the component's and the
`*ScopeFactory` base classes' is a pointer, not prose:

- **The component class** (the class doing the work, e.g. `Constant`) carries
  the behaviour: what it does, its semantics, its edge cases. Put depth here,
  nowhere else.
- **The `Factory` subclass** (the YAML bean) gets a one-line pointer and no
  behaviour:
  `A {@link GlobalScopeFactory} counterpart of {@link Component}, built via {@link Factories#method(Factory)}.`
  Name the `*ScopeFactory` the class's ancestry is rooted in
  (`GlobalScopeFactory`, `SimulationScopeFactory`, `RelativeScopeFactory` in
  `core`; `VesselScopeFactory` in `fishing`), walking past any intermediate
  template base that is not itself one (`RelativeDateTimeFactory`,
  `AbstractQuantityFactory`). The "built via" half is a real `{@link}`, not
  `{@code}`, and depends on what builds the component:
  - **One method, however many overloads** (the common case, so check it first):
    link straight to it. Name one overload's erased parameters if it is
    overloaded (`{@link Factories#method(Factory)}`); Javadoc renders the label.
  - **Several overloads of one name each build it**: link the class with a
    label, `{@link Factories Factories.method(...)}`.
  - **Several method names build it**: combine them in one label,
    `{@link Factories Factories.tank(...)/Factories.fullTank(...)}`.

  Check what each overload actually returns. `allOf(MappingFactory...)` returns
  an `AllOfFactory` and `allOf(ValueFactory...)` a `MappingAsValueFactory`
  holding one: both build an `AllOf`, so `AllOf` and `AllOfFactory` get a
  class-level link, while `MappingAsValueFactory`, built by the value overload
  of every combinator, names that category rather than a method. `constant(T)`
  and `constant(Factory)` both return the same `ConstantFactory`, so it gets a
  class-level link too.

- **Scope semantics** are explained once, on each `*ScopeFactory` base class,
  never restated on a leaf factory: the `{@link GlobalScopeFactory}` in the
  pointer is enough, with no separate "Scope:" line. The helper's `@return`
  links the same way:
  `@return a {@link GlobalScopeFactory} for a {@link Component} that always returns {@code true}`.
- **The static `Factories` helper method** is what scenario code calls and
  autocomplete surfaces. Document `@param` and `@return`, plus `@see Component`
  for the behaviour.
- **The `Factories` class** gets one or two sentences naming what its methods
  produce ("Factories for `Provider`s that always return a fixed value.").
- **`package-info.java`**, in any package with a `Factories` class: one or two
  sentences on what the package's components do, a `{@link Factories}` pointer,
  and links back to `Factory` or `Scenario` rather than an explanation of the
  pattern.

On the `Factories` class and in `package-info.java`, describe the category
rather than list the members: a list of today's classes goes stale when the next
one is added. Naming one or two as examples is fine; if the sentence would need
editing whenever a sibling class is added, it is a list.

## Every public and protected member

Every public or protected member of a class you touch gets a short Javadoc
comment: constants, constructors, fields and methods, including the protected
constructor or helper field of an abstract base meant for subclassing.
Package-private and private members stay undocumented unless their behaviour is
not obvious.

The exception is a trivial override of a documented method, such as
`newInstance(scope)` or a plain `get()`, `test()` or `apply()`: Javadoc inherits
the comment. An override with an edge case, a surprising choice or a deviation
from its interface gets its own comment. The bar is whether a reader of the
inherited doc alone would be misled.

## Javadoc stands alone

Javadoc and comments never point to a design document, an invariant number or a
section number: design notes are not part of POSEIDON and may not survive, while
code outlives them. State the rule and its reason in the comment itself. The
same goes for commit messages.
