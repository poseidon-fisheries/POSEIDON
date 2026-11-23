# Lombok usage

Lombok (`buildlogic.java-common-conventions`, all modules) is used pervasively
to cut constructor/ accessor/equality boilerplate. Reach for it on _any_ class
where these apply, not just the shapes below — a plain class with
`private final` dependency fields should get `@RequiredArgsConstructor` rather
than a hand-written constructor, and a class that needs
`equals`/`hashCode`/`toString` should get `@Value`/`@Data`/`@EqualsAndHashCode`
rather than hand-rolled ones. Prefer Lombok wherever it can generate the code:
write a constructor, accessor or `equals`/`hashCode`/`toString` by hand only
when it does something Lombok's wouldn't. Four recurring shapes cover almost
every case — match the shape to the class's role:

- **Injected dependencies / no mutation** (most non-factory classes: services,
  handlers, predicates, action functions): `@RequiredArgsConstructor` on the
  class, `private final` fields, annotated `@NonNull` when the constructor
  should null-check them.
- **Immutable value objects** (small data carriers with no setters): `@Value` —
  final fields, generated getters/`equals`/`hashCode`/`toString`. Add
  hand-written constructors alongside it only when a field needs
  derived/defaulted state. Use `@Value` rather than a Java `record`: MASON's GUI
  inspectors find properties through JavaBean getters (`getX()`), which a
  record's accessors (`x()`) aren't. This holds for every value object, not only
  those the GUI reaches; a record is used only where a deconstruction pattern
  needs one.
- **`Factory` YAML beans** (`*Factory` classes per the Factory/Scenario pattern
  — see `docs/agents/architecture.md`): immutable, always the same five —
  `@Getter @ToString @AllArgsConstructor @EqualsAndHashCode(callSuper = true) @SuperBuilder(toBuilder = true)`,
  with `final` fields. The one exception is a factory that checks its own fields
  when built: it writes by hand the protected builder constructor
  `@SuperBuilder` would generate, with the checks in it, in place of
  `@AllArgsConstructor`, and helpers build it through its builder
  (`VesselRegisterFactory`). A class without fields has `@NoArgsConstructor`
  instead of `@AllArgsConstructor`. `ScenarioLoader` builds every factory
  through its builder, so a factory class without one fails to load
  (`FactoryBuildersTest` checks them all); `@SuperBuilder` needs every class in
  the hierarchy to have it, abstract bases included, and `toBuilder` makes
  changed copies. `ScenarioWriter` writes the properties the getters give, so
  every getter must be one the builder sets (`FactoryBuildersTest` checks that
  too): a value derived from the class, such as a quantity's type, is a method
  not named as a getter. A field with an initialiser needs `@Builder.Default`,
  or the builder ignores the initialiser. A `List` or `Map` field is an
  `ImmutableList` or `ImmutableMap` with `@Singular`, so the builder copies what
  it is given; the static helpers copy their arguments into one.
  `callSuper = true` makes equality include the superclass's fields. The other
  scenario nodes are immutable beans with a builder and `toBuilder` too, checked
  by the same test. Vessel components (`FactoryComponent`, `VaryingComponent`)
  are global factories with a plain `@Builder(toBuilder = true)` and identity
  equality, since they are the keys a vessel keeps its components under.
- **`Scenario`**: `@Getter @Builder(toBuilder = true)`, built fluently; its
  `with…` methods derive changed copies through `toBuilder`. Its all-arguments
  constructor is hand-written, to check component names.

Use `@Getter(AccessLevel.NONE)` on individual fields to suppress just that one
accessor rather than dropping `@Getter` for a hand-written class. Suppress an
accessor to hide it or to replace it with one that does more; a hand-written
replacement identical to Lombok's is left to Lombok.
