# Testing

Tests use JUnit 5 (`useJUnitPlatform()`), Mockito, jqwik (property-based
testing), and AssertJ for every assertion: never JUnit's `Assertions`.

- Pick the most specific AssertJ assertion for what the code promises. For a
  collection whose order the code does not promise, use
  `containsExactlyInAnyOrder`; `isEqualTo` against a `Set.of(...)` can end up
  checking iteration order, which turns a harmless change of order into a
  failure.
- Separate a test's phases with a blank line. Write no section-marker comments
  (`// Arrange`, `// Act`, `// Assert`, `// Given`, `// When`, `// Then`) and no
  comments that restate the next line (`// Assert the method returns true`).
- `SimulationScope` and `VesselScope` hold their simulation and vessel through
  `WeakReference`s. In tests, keep the mocked `Simulation` or `Vessel` in a
  field: one that only the scope references
  (`new SimulationScope(mock(Simulation.class))`, or a local variable not used
  afterwards) can be garbage-collected mid-test, and the test then fails at
  random with a `NullPointerException`.
