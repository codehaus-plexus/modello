---
title: Modello Velocity Plugin
author: Guillaume Nodet
---

# Modello Velocity Plugin

Modello Velocity Plugin generates files from the Modello model using [Velocity templates](https://velocity.apache.org/engine/2.3/vtl-reference.html).

# Velocity Processing

The plugin is configured with a list of `template` files to evaluate, rfelative to `velocityBasedir` (which defaults to Maven's `${project.basedir}`).

During template evaluation, `#MODELLO-VELOCITY#SAVE-OUTPUT-TO {relative path to file}` pseudo macro is available to send the rendered content to a file.

The Velocity context contains some variables related to the Modello model context that you can use:

| Variable | Type | Description |
|---|---|---|
| parameters configured in the plugin | `String` | The parameters values configured in the plugin as `{key}={value}`. |
| `version` | `String` | The version of the model being used. |
| `model` | [`Model`](../../modello-core/apidocs/org/codehaus/modello/model/package-summary.html) | The Modello model. |
| `Helper` | [`Helper`](apidocs/org/codehaus/modello/plugin/velocity/Helper.html) | A helper tool with classical functions useful to generate content from a Modello model API. |
| `template` | `String` | the template that is being evaluated. |
