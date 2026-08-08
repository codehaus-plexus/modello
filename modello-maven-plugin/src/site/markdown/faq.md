---
title: Frequently Asked Questions
---

# Frequently Asked Questions

1. [Where can I find documentation about Modello?](#question1)
2. [Is Modello really used anywhere?](#question2)

<a name="question1"></a>

### Where can I find documentation about Modello?

The [Modello model descriptor](https://codehaus-plexus.github.io/modello/modello.html) is described and
documented with Modello.

<a name="question2"></a>

### Is Modello really used anywhere?

Modello is used extensively in Maven. You can find examples of models:

- [Maven's POM](https://maven.apache.org/ref/current/maven-model/maven.html): the mdo file for this model
  is [`maven.mdo`](https://github.com/apache/maven/blob/master/api/maven-api-model/src/main/mdo/maven.mdo)
  (Maven 4; the Maven 3 version is on the
  [`maven-3.9.x` branch](https://github.com/apache/maven/blob/maven-3.9.x/maven-model/src/main/mdo/maven.mdo))
- [Maven's settings](https://maven.apache.org/ref/current/maven-settings/settings.html): the mdo file for
  this model is
  [`settings.mdo`](https://github.com/apache/maven/blob/master/api/maven-api-settings/src/main/mdo/settings.mdo)
- [Maven assembly plugin](https://maven.apache.org/plugins/maven-assembly-plugin/): uses both an
  [assembly model](https://github.com/apache/maven-assembly-plugin/blob/master/src/main/mdo/assembly.mdo)
  and an
  [assembly component model](https://github.com/apache/maven-assembly-plugin/blob/master/src/main/mdo/assembly-component.mdo)
- [Doxia site model](https://github.com/apache/maven-doxia-sitetools/blob/master/doxia-site-model/src/main/mdo/site.mdo),
  the model behind `site.xml`
