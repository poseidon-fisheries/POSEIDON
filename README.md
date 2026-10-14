[![Java CI with Gradle](https://github.com/poseidon-fisheries/POSEIDON/actions/workflows/gradle.yml/badge.svg)](https://github.com/poseidon-fisheries/POSEIDON/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/poseidon-fisheries/POSEIDON/branch/main/graph/badge.svg)](https://codecov.io/gh/poseidon-fisheries/POSEIDON)
[![Javadoc](https://github.com/poseidon-fisheries/POSEIDON/actions/workflows/javadoc.yml/badge.svg)](https://poseidon-fisheries.github.io/POSEIDON/)

This is the code for POSEIDON, a fishery agent-based model.

The code is currently undergoing heavy refactoring as part of the [SURIMI](https://www.surimi-project.eu/) project.

If you want to see POSEIDON in action or follow the old tutorial, your best bet is the `poseidon-gui` repo:

https://github.com/poseidon-fisheries/poseidon-gui

## Development

Java sources are formatted with [Prettier](https://prettier.io/) (prettier-plugin-java) and carry
a GPLv3 license header, both enforced by Spotless: `./gradlew build` fails on unformatted files,
and `./gradlew spotlessApply` fixes them. Gradle downloads Node.js itself, so this needs no local
install. Optional local setup:

- Format on save in IntelliJ: run `npm install` once, then enable Settings → Languages &
  Frameworks → JavaScript → Prettier with "Run on save" and `**/*.java` in "Run for files".
- Check formatting before each push: `./gradlew spotlessInstallGitPrePushHook`. Spotless can't
  install it when POSEIDON is checked out as a submodule; there, write an executable
  `$(git rev-parse --git-path hooks)/pre-push` containing `#!/bin/sh` and
  `exec ./gradlew spotlessCheck`.
- Skip reformatting commits in `git blame`:
  `git config blame.ignoreRevsFile .git-blame-ignore-revs`. Git then refuses to blame on
  checkouts without that file (commits from before it was added); there, pass
  `--ignore-revs-file .git-blame-ignore-revs` to `git blame` instead.
