# Javadoc conventions

See `docs/agents/architecture.md` for the component/`Factory`/`Factories`-helper triplet pattern
this section documents.

The triplet gets documented in four different places, once each, not duplicated across them:

- **Component class** (the plain value/config class actually doing the work, e.g.
  `ConstantProvider`) carries the real behavior doc: what it does, semantics, edge cases. This is
  the one source of truth — put depth here, nowhere else.
- **`Factory` subclass** (the YAML bean) gets a one-line pointer only: `A
  {@link GlobalScopeFactory} counterpart of {@link Component}, built via
  {@link Factories#method(Factory)}.` — naming whichever `*ScopeFactory` the leaf class's ancestry
  is rooted in (`GlobalScopeFactory`, `SimulationScopeFactory`, `RelativeScopeFactory` in `core`;
  domain-specific ones like `VesselScopeFactory` in `agents`) in place of the generic `Factory`.
  When the leaf extends a `*ScopeFactory` directly, that's the one to name. When it instead
  extends an intermediate abstract template base that isn't itself a `*ScopeFactory` (e.g.
  `RelativeDateTimeFactory`, `AbstractQuantityFactory`), walk up to the `*ScopeFactory` that base
  is ultimately rooted in and name that instead — naming the template base defeats the point of
  the pointer, since the base carries no scope-semantics prose of its own to link to. The `built
  via` half is a real `{@link}`, clickable, not `{@code}`. How it's written depends on how many
  `Factories` methods build this component:
  - **Exactly one method builds this component, however many overloads that method has:** link
    straight to it — `{@link Factories#method(Factory)}` if it's overloaded (name the erasure of
    one overload's params to disambiguate; Javadoc renders the label from the signature, no custom
    label needed), or plain `{@link Factories#method()}` if it isn't. There's nothing to hide
    behind a class-level link when only one method reaches this component — link it directly. This
    is the common case; check it first before reaching for the fallbacks below.
  - **More than one overload of the same method name independently builds this exact component**
    (e.g. `between(double, double)` and `between(Factory, Factory)` both ultimately return a
    `BetweenFactory`): link the class with a custom label instead of picking one overload to stand
    for all of them: `{@link Factories Factories.method(...)}`.
  - **More than one distinct method *name* builds this component** (not overloads, e.g.
    `alwaysTrue()`/`alwaysFalse()` both returning `ConstantBooleanProviderFactory`): combine them
    in one class-level link's label: `{@link Factories Factories.alwaysTrue()/Factories.alwaysFalse()}`.

  Don't assume same-name overloads share a target or a distinct name doesn't — check what each
  overload actually returns (e.g. `constantDouble(double)` and `constantDouble(Factory)` return two
  *different* factory classes despite sharing a name, so each gets its own direct link; `constant(T)`
  and `constant(Factory)` both return the same `ConstantProviderFactory`, so that one stays a
  class-level link). No behavior explanation on the `Factory` subclass — scenario-building code
  never touches these directly (per `docs/agents/architecture.md`), so a reader here just needs to
  be routed to the component and to the helper that builds it.
- **Scope semantics are documented once, not per factory.** Each `*ScopeFactory` base class
  carries the real explanation of what that scope means and when to reach for it, on the class
  itself. Never restate that explanation in free text on a leaf `Factory` subclass — copies drift.
  The `{@link GlobalScopeFactory}` in the one-liner above is the pointer; no separate "Scope:" line
  is needed. Do the same on the corresponding `Factories` helper method's `@return`: `@return a
  {@link GlobalScopeFactory} for a {@link Component} that always returns {@code true}`.
- **Static `Factories` helper method** is the discoverability layer: this is what scenario-building
  code actually calls and what autocomplete surfaces. Document `@param`/`@return` for what the
  method takes and produces (the scope link folded into `@return` per above), plus `@see Component`
  for the full behavior — not a re-explanation.
- **`Factories` class itself** gets a short class-level Javadoc: one or two sentences naming what
  the grouped static methods produce (e.g. "Factories for `Provider`s that always return a fixed
  value.") — the entry point a reader lands on before drilling into individual methods.
- **Package `package-info.java`** (for any package containing a `Factories` class): a short
  blurb — one or two sentences on what the package's components do — plus a `{@link Factories}`
  pointer, so a reader landing on the package summary gets routed straight to the discoverability
  entry point instead of having to guess which class to open.

**Describe the category, don't enumerate the members**, on both the `Factories` class doc and
`package-info.java`. A package or `Factories` class is expected to grow new components over time;
a doc that lists today's classes by name (`{@link ObjectFactory}`, `{@link ListFactory}`, ...) goes
stale the moment a new one is added and nobody remembers to update the list. Say what *kind* of
thing the package holds ("factories for pass-through/collection literals, resolved components, and
scope-changing wrappers") rather than naming every current class. Naming one or two classes as
*examples* of the category is fine when it aids concreteness; naming all of them as an exhaustive
list is not — if the sentence would need editing every time a sibling class is added, it's
enumerating, not describing.

Don't write the same behavioral explanation twice across the triplet; every doc comment except the
component's and the `*ScopeFactory` base classes' should be a pointer, not prose.

**Every public and protected member of a class you touch needs to be documented** — not just the
triplet skeleton described above. Constants, public constructors, and any other public/protected
method or field get a real (if short) Javadoc comment. The one exception: a *trivial* `@Override`
method whose superclass/interface method is already documented doesn't need its own comment —
Java's standard doc-inheritance applies (the javadoc tool shows "Description copied from..."), so
`newInstance(scope)` overrides of `AbstractFactory`'s hook, and `get()`/`test()`/`apply()`-style
overrides of `Supplier`/`Predicate`/`Provider` etc., are already covered as long as the method
they override carries a doc comment somewhere up the hierarchy and the override itself does
nothing but implement the plain contract. If the override has genuinely special behavior worth
calling out — an edge case the inherited doc doesn't cover, a surprising choice, a deviation from
what a reader would assume from the interface alone — give it its own comment rather than leaning
on inheritance; the bar is "would a reader be misled by only reading the inherited doc," not
"is this technically an override." When auditing a package already marked done, check for this
too — it's easy to document the triplet shape and miss a public constant or constructor sitting
alongside it.

The pattern itself — why the triplet exists, why scenario code never calls `.get()` directly — is
already covered in `docs/agents/architecture.md`; don't restate it in per-package Javadoc. Package
`package-info.java` files should stick to that short blurb + `{@link Factories}` pointer, and
`@link` back to `Factory`/`Scenario` rather than re-explaining the general pattern.
