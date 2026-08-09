---
title: Modello Model Version Converter Plugin
author: 
  - Hervé Boutemy
---

# Modello Model Version Converter Plugin

Modello Model Version Converter Plugin generates code to transform a model between two versions.

## converters

The source model version to convert from is in `modello.version` parameter: noted `A.B.C`.

The target model version is the greatest of `modello.all.versions` parameter: noted `X.Y.Z`.

`converters` generator creates `my.model.package.vA_B_C.converter.VersionConverter` interface and a `BasicVersionConverter` class implementing the interface to convert a class model from version `A.B.C` to `X.Y.Z`. If versions are different, model classes are supposed to be available in their corresponding java packages with version. If versions are equals, model classes are supposed to be available in the java package both with and without version, the package with version being the source model and the package without version being the target. 

For every class in the source model, a method is generated in the interface and an automatic implementation generated in the class with following signature:

```java
public my.model.package[.vX_Y_Z].ModelClass convertModelClass( my.model.package.vA_B_C.ModelClass )
```
In addition, if source and target versions are equals, a `my.model.package.converter.ConverterTool` is generated to automate reading a file, detect its model version (using StAX reader delegate generated code), and convert it to the target model version in the java package without version using generated `BasicVersionConverter` class:

```java
public my.model.package.RootClass convertFromFile( File f )
    throws IOException, XMLStreamException
```