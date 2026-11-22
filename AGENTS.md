# Agent guide for modello

Modello (`org.codehaus.modello:modello`, `pom` packaging, inheriting the `plexus` parent) is a
code-generation framework: one `.mdo` model produces Java classes, readers and writers for XML, YAML
and JSON, an XSD and documentation; Maven uses it for its own POM and `settings.xml` models. The
reactor has `modello-core`, `modello-maven-plugin`, `modello-plugins` (one module per generator),
`modello-test` and `modello-ant-task`. It is MIT-licensed, unlike most Codehaus Plexus projects.

## Build and test

- `mvn verify` builds and runs the unit tests. The invoker integration tests of
  `modello-maven-plugin` run only under `-Prun-its`.
- Run `mvn spotless:apply` on JDK 17 to 21 before pushing; CI fails on formatting otherwise.
- The code targets Java 8 (`javaVersion` 8 from the `plexus` parent), and the plugin's
  `prerequisites` require Maven 3.9.0. `mavenVersion` pins the Maven 3.9 line that the plugin and
  core are built against, so keep it on 3.9.x.
- CI calls the shared `codehaus-plexus/.github` build workflow with
  `verify javadoc:javadoc -Prun-its`, on the shared JDK and operating system matrix.
- The plugin tests compile the generator output together with the classes in
  `src/test/verifiers`; `modello-plugins/README.md` explains that layout.
- `src/main/mdo/modello.mdo` describes the model descriptor format. The `reporting` profile renders
  it into `modello.html` on the site, so change it together with the code that implements the format.

## Documentation updates

A change that users or contributors can notice ships with the documentation it affects, in the
same pull request. A refactor with no visible effect needs none.

- A goal added, renamed or removed in `modello-maven-plugin`: the goal list in
  `modello-maven-plugin/src/site/markdown/index.md`, and `usage.md` or the example page if the
  recommended configuration changes. Examples show the plugin version as
  `<version><!-- latest release --></version>` on the first declaration of `usage.md` and no version
  elsewhere; the pages are plain Markdown, not Velocity templates.
- A parameter added or changed on a goal: its Javadoc, which is the goal page. Document the default,
  the property name and `@since`.
- A generator changed or added in `modello-plugins`: that module's `src/site/markdown/index.md`
  and its `src/site/site.xml`.
- A model attribute or generated API that needs a change in users' models: `modello.mdo`, and
  `src/site/markdown/migration-guide.md` when it breaks existing models.
- Location tracking behavior: `src/site/markdown/location-tracking.md`.
- Ant task attributes or goals: `modello-ant-task/README.md`.
- The Java or Maven requirement, or how to build and release: `README.md`.
- A new convention, build gate or test fixture: this file, or `modello-plugins/README.md` for the
  verifier layout.

