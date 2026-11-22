# Modello

[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.modello/modello-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.modello/modello-maven-plugin)
[![GitHub CI](https://github.com/codehaus-plexus/modello/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/codehaus-plexus/modello/actions/workflows/maven.yml?query=branch%3Amaster)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/org/codehaus/modello/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/org/codehaus/modello/README.md)
[![MIT License](https://img.shields.io/github/license/codehaus-plexus/modello.svg?label=License)](https://opensource.org/license/mit)

Code generation from a single model file. You describe your data model once in an `.mdo` file, and Modello
generates the Java classes, the readers and writers, an XSD and the documentation — all kept in step,
because they come from the same source.

Maven uses it for its own POM and `settings.xml` models.

## Status

Maintained. Generated code is consumed widely, so output compatibility is treated carefully.

## Using it

Modello is a build-time tool, so it goes in `<build><plugins>` rather than in your dependencies:

```xml
<plugin>
  <groupId>org.codehaus.modello</groupId>
  <artifactId>modello-maven-plugin</artifactId>
  <configuration>
    <version>1.0.0</version>
    <models>
      <model>src/main/mdo/my-model.mdo</model>
    </models>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>java</goal>
        <goal>xpp3-reader</goal>
        <goal>xpp3-writer</goal>
        <goal>xsd</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Check the badge above for the current version. Each goal is a generator; pick the ones you need.

## Generators

Java classes, plus readers and writers for XML (XPP3 and StAX; DOM4J readers and writers; SAX and JDOM
writers only), YAML (SnakeYaml) and JSON (Jackson), plus JSON Schema, XSD, xdoc documentation and
version converters. Velocity templates are supported
since 2.1.0 for anything not covered.

Each has its own page under [Plugins](https://codehaus-plexus.github.io/modello/modello-plugins/) on the site.

## Requirements

Java 8 or later, and Maven 3.9.0 or later to run the Maven plugin.

## Documentation

- [Project site](https://codehaus-plexus.github.io/modello/) — the model descriptor reference, generator pages and a [migration guide](https://codehaus-plexus.github.io/modello/migration-guide.html)
- [Release notes](https://github.com/codehaus-plexus/modello/releases)

## Licensing

Modello is licensed under the [MIT License](https://opensource.org/license/mit). The rest of
the Codehaus Plexus projects are Apache-2.0.

## Contributing

See [CONTRIBUTING.md](https://github.com/codehaus-plexus/.github/blob/master/CONTRIBUTING.md) and
[AGENTS.md](AGENTS.md). In short: `mvn verify` builds, and run `mvn spotless:apply` before pushing or CI
will fail on formatting. Integration tests run under `-Prun-its`.

Please report security vulnerabilities privately — see
[SECURITY.md](https://github.com/codehaus-plexus/.github/blob/master/SECURITY.md), not a public issue.
