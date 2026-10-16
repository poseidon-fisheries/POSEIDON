# Lombok usage

Lombok (`buildlogic.java-common-conventions`, all modules) is used pervasively to cut constructor/
accessor/equality boilerplate. Reach for it on *any* class where these apply, not just the shapes
below — a plain class with `private final` dependency fields should get `@RequiredArgsConstructor`
rather than a hand-written constructor, and a class that needs `equals`/`hashCode`/`toString` should
get `@Value`/`@Data`/`@EqualsAndHashCode` rather than hand-rolled ones. Four recurring shapes cover
almost every case — match the shape to the class's role:

- **Injected dependencies / no mutation** (most non-factory classes: services, handlers, predicates,
  action functions): `@RequiredArgsConstructor` on the class, `private final` fields, annotated
  `@NonNull` when the constructor should null-check them.
- **Immutable value objects** (small data carriers with no setters): `@Value` — final fields,
  generated getters/`equals`/`hashCode`/`toString`. Add hand-written constructors alongside it only
  when a field needs derived/defaulted state.
- **`Factory` YAML beans** (`*Factory` classes per the Factory/Scenario pattern — see
  `docs/agents/architecture.md`): always the same five — `@Data @NoArgsConstructor
  @AllArgsConstructor @EqualsAndHashCode(callSuper = true) @SuperBuilder(toBuilder = true)`.
  `ScenarioLoader` builds every factory through its builder, so a factory class without one fails
  to load (`FactoryBuildersTest` checks them all); `@SuperBuilder` needs every class in the
  hierarchy to have it, abstract bases included, and `toBuilder` makes changed copies. A field
  with an initialiser needs `@Builder.Default`, or the builder ignores the initialiser. The setters
  are still used by code that changes factories in place, which is going. `callSuper = true` makes
  equality include the superclass's fields. Don't add other constructors — see "Factory class
  shape" in `docs/agents/architecture.md`.
- **Builder-style config objects** (e.g. `Scenario`, `SimulationStartOptions`): `@Getter @Setter
  @Builder @NoArgsConstructor` — mutable via setters or built fluently via `@Builder`.

Use `@Getter(AccessLevel.NONE)` (or `@Setter(AccessLevel.NONE)`) on individual fields to suppress
just that one accessor rather than dropping `@Data`/`@Getter` for a hand-written class.

## Sharing data across simulations

A type meant to be read-only and shared globally (e.g. across every simulation run from one loaded scenario) must not implement a mutable-by-contract interface like `MutableGrid` — even if its own methods never mutate, `MutableGrid.getField()` (or any analogous accessor) hands out a live mutable handle to the backing structure, so the object is never actually immutable. Follow the existing pattern instead: a small read-only sibling type that wraps the mutable implementation without implementing its mutable interface (e.g. `CarryingCapacityGrid` / `ImmutableBiomassGrid`, both `extends DoubleGridWrapper` rather than implementing `BiomassGrid`).

## Collections

Prefer Guava's immutable collection types (`ImmutableMap`, `ImmutableList`, `ImmutableSet`, etc.) over mutable JDK collections (`HashMap`, `ArrayList`, `HashSet`) for factory-produced results and other data that shouldn't change after construction — this is already the convention for data shared across simulations or handed out from a resolved `Factory` (e.g. `FisheableBiomassGrids`'s use of `ImmutableMap`). Reach for mutable collections only for genuinely local, mutable working state inside a method body.
