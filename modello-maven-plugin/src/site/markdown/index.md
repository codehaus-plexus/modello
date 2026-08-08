---
title: Introduction
author: 
  - Denis Cabasson
  - Hervé Boutemy
date: 1 January 2009
---

# Modello Maven Plugin

This plugin makes use of the [Modello](https://codehaus-plexus.github.io/modello/) project.

## Goals Overview

- [modello:xsd](./xsd-mojo.html) Generates an XML Schema from the Modello model.
- [modello:xdoc](./xdoc-mojo.html) Generates standard documentation for the Modello model, in xdoc format.
- [modello:java](./java-mojo.html) Generates Java beans from the Modello model.
- [modello:xpp3-writer](./xpp3-writer-mojo.html) Generates an XML Pull Parser writer from the Modello model.
- [modello:xpp3-reader](./xpp3-reader-mojo.html) Generates an XML Pull Parser reader from the Modello model.
- [modello:xpp3-extended-reader](./xpp3-extended-reader-mojo.html) Generates an XML Pull Parser reader from the Modello model that records line/column number metadata and eventual source in the parsed model.
- [modello:xpp3-extended-writer](./xpp3-extended-writer-mojo.html) Generates an XML Pull Parser writer from the Modello model that writes line/column number and source info as comments on each line.
- [modello:dom4j-writer](./dom4j-writer-mojo.html) Generates a DOM4J writer from the Modello model.
- [modello:dom4j-reader](./dom4j-reader-mojo.html) Generates a DOM4J reader from the Modello model.
- [modello:stax-writer](./stax-writer-mojo.html) Generates a StAX writer from the Modello model.
- [modello:stax-reader](./stax-reader-mojo.html) Generates a StAX reader from the Modello model.
- [modello:jdom-writer](./jdom-writer-mojo.html) Generates a [jdom](http://www.jdom.org/) writer from the model that is capable of preserving element ordering and comments.
- [modello:jackson-writer](./jackson-writer-mojo.html) Generates a JSON writer based on Jackson Streaming APIs from the Modello model.
- [modello:jackson-reader](./jackson-reader-mojo.html) Generates a JSON reader based on Jackson Streaming APIs from the Modello model.
- [modello:jackson-extended-reader](./jackson-extended-reader-mojo.html) Generates a JSON reader based on Jackson Streaming APIs from the. Modello model that records line/column number metadata in the parsed model.
- [modello:snakeyaml-writer](./snakeyaml-writer-mojo.html) Generates a YAML writer based on SnakeYaml Streaming APIs from the Modello model.
- [modello:snakeyaml-reader](./snakeyaml-reader-mojo.html) Generates a YAML reader based on SnakeYaml Streaming APIs from the Modello model.
- [modello:snakeyaml-extended-reader](./snakeyaml-extended-reader-mojo.html) Generates a YAML reader based on SnakeYaml Streaming APIs from the. Modello model that records line/column number metadata in the parsed model.
- [modello:velocity](./velocity-mojo.html) Creates files from the model using Velocity templates.
- [modello:converters](./converters-mojo.html) Generates classes that can convert between different versions of the model.

## Usage

General instructions on how to use the Modello Plugin can be found on the [usage page](./usage.html). Some more specific use cases are described in the examples given below.

In case you still have questions regarding the plugin's usage, please have a look at the [FAQ](./faq.html) and feel free to contact the [user mailing list](./mailing-lists.html). The posts to the mailing list are archived and could already contain the answer to your question as part of an older thread. Hence, it is also worth browsing/searching the [mail archive](./mailing-lists.html).

If you feel like the plugin is missing a feature or has a defect, you can fill a feature request or bug report in our [issue tracker](./issue-management.html). When creating a new issue, please provide a comprehensive description of your concern. Especially for fixing bugs it is crucial that the developers can reproduce your problem. For this reason, entire debug logs, POMs or most preferably little demo projects attached to the issue are very much appreciated. Of course, patches are welcome, too. Contributors can check out the project from our [source repository](./scm.html).

## Examples

To provide you with better understanding of some usages of the Modello Plugin, you can take a look at the following example:

- [Including multiple models](./examples/multi-model.html)

