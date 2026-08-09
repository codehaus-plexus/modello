# Plugin tests and the `src/test/verifiers` tree

Each plugin here is a code generator, so its tests cannot simply call the code they are testing and
assert on the result. They have to run the generator, compile what it produced, and then exercise the
compiled classes. That happens in three steps, driven by
`AbstractModelloJavaGeneratorTest` in the `modello-test` module:

1. the test calls `modello.generate(...)` to produce java sources under `target/generated/sources`;
2. `compileGeneratedSources(...)` compiles those sources **together with** one directory of
   `src/test/verifiers`, chosen by `verifierId`;
3. `verifyCompiledGeneratedSources(...)` loads the verifier through a classloader with no parent and
   calls its `verify()` method reflectively.

## Why `src/test/verifiers` is not a source root

**It cannot be compiled at build time.** A verifier asserts against the classes the generator emits —
`modello-plugin-java/src/test/verifiers/features/FeaturesJavaVerifier.java` alone imports 21 of them —
and none of those classes exist until step 1 has run. Registering the tree with `build-helper` would
only move the failure earlier.

That has a consequence worth knowing before you touch anything here:

> **Tooling that works from Maven source roots does not see this tree.**

Formatters, static analysis, IDE indexing and automated refactoring all skip it, and they skip it
*silently* — a run reports success having read none of these files. A JUnit 4 to JUnit 5 migration hit
exactly this: the recipe reported success and had rewritten none of the 29 verifiers. Whatever you are
running across the codebase, check afterwards whether it reached this directory, and expect to finish
the job by hand.

## Working on a verifier

- The directory name is the `verifierId`, and it defaults to the test's name, so a verifier is found
  only if its directory matches. There is no compile-time link between the two.
- A verifier is plain code with assertions, not a JUnit test class. Surefire never sees it; it runs
  only because a generator test asks for it by fully-qualified name.
- Because nothing else compiles this tree, a mistake here surfaces as a test error at runtime rather
  than as a build failure. After changing a verifier, run the module's tests — and if you are changing
  many at once, corrupt one assertion on purpose first and confirm the corresponding test goes red.
  Several verifier directories are only reachable from one test each, so it is easy to edit a file
  that nothing exercises.
- Run `mvn install` or `mvn verify`, not `mvn test`. Under `mvn test` the downstream modules see
  `modello-test` as `target/classes`, where the `META-INF/maven/.../pom.properties` that
  `getModelloVersion()` reads does not exist yet; it is written when the jar is packaged.

