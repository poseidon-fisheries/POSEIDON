# AGENTS.md

Longer topic-specific guidance lives in `docs/agents/`. Read the relevant doc
before the work it covers:

- `docs/agents/modules.md` — before adding a class to a module or a dependency
  between modules.
- `docs/agents/architecture.md` — before writing or changing a factory, a
  `Factories` helper or scenario code, or working with model time values.
- `docs/agents/runtime-model.md` — before changing `core` or `fishing`
  internals.
- `docs/agents/javadoc.md` — before writing or editing Javadoc.
- `docs/agents/lombok.md` — before writing a class or changing its Lombok
  annotations.
- `docs/agents/code-style.md` — before writing or reviewing Java.
- `docs/agents/testing.md` — before writing or editing a test.
- `docs/agents/naming.md` — before naming or reviewing any identifier.
- `docs/agents/conceptual-correctness.md` — before fixing a design problem or
  giving missing state a default.

## What this is

POSEIDON is an agent-based model of fisheries, built on the
[MASON](https://cs.gmu.edu/~eclab/projects/mason/) discrete-event simulation
toolkit. It's a Gradle multi-module Java project (Java 25 toolchain), developed
by the University of Oxford. This `main` branch is an ongoing rewrite/redesign
of the original POSEIDON model.

## Build & test commands

`./gradlew build` runs all tests, SpotBugs, Checkstyle and `spotlessCheck`. Test
one module with `./gradlew :core:test`, one class or method with
`--tests "uk.ac.ox.poseidon.core.ScenarioTest"`.

- Shared build configuration (dependencies, toolchain, compiler flags, test and
  analysis wiring) lives in the convention plugins in
  `buildSrc/src/main/kotlin/`, not in the modules' build files.
- `build` runs `spotlessCheck`, which fails on any Java or Markdown file
  `spotlessApply` would change: run `./gradlew spotlessApply` after editing
  either. For license-header years, see "Licensing".
- Compilation runs every javac lint category except `serial` and treats warnings
  as errors (`-Werror -Xlint:all,-serial`), so an unchecked or deprecation
  warning fails the build, not just linting.
- Checkstyle (`config/checkstyle/checkstyle.xml`) fails the build on any
  violation; the checks code most often trips are listed in
  `docs/agents/code-style.md`.
- Test tasks are auto-configured with a Mockito Java agent (`-javaagent`,
  `-Xshare:off`): keep it when editing
  `buildlogic.java-common-conventions.gradle.kts`.
- jdtls (the LSP tool) can report type-inference errors that javac accepts;
  `./gradlew compileJava` is authoritative.
- SpotBugs exclusions live in `spotbugs_exclude.xml` at the repo root; consult
  it before assuming a finding is a false positive.

## Merging

Merge branches into `main` by fast-forward only (`git merge --ff-only`), never
with a merge commit. If `main` has moved on, rebase the branch onto it first.

## Licensing

All source files carry a GNU GPLv3 header (University of Oxford copyright);
`./gradlew spotlessApply` adds it to new Java files. Spotless keeps whatever
year a header has, so when you modify a file, end its year range at the current
year yourself: with `YYYY` the current year, `2024` and `2024-2025` both become
`2024-YYYY`, and `YYYY` stays.
