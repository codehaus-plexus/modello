---
title: Modello SnakeYaml Plugin
author: 
  - Simone Tripodi
---

# Modello SnakeYaml Plugin

Modello SnakeYaml Plugin generates YAML readers and writers based on [SnakeYaml APIs](https://code.google.com/p/snakeyaml/), plus reader delegates to be able to read multiple model versions.

## snakeyaml-reader

`snakeyaml-reader` generator creates `my.model.package.io.snakeyaml.ModelNameSnakeYamlReader` class with following public methods: 

```java
public RootClass ( Reader reader, boolean strict )
    throws IOException

public RootClass read( Reader reader )
    throws IOException

public RootClass read( InputStream input, boolean strict )
    throws IOException

public RootClass read( InputStream input )
    throws IOException
```
## snakeyaml-writer

`snakeyaml-writer` generator creates `my.model.package.io.snakeyaml.ModelNameSnakeYamlWriter` class with following public methods: 

```java
public void write( OutputStream output, RootClass root )
    throws IOException

public void write( Writer writer, RootClass root )
    throws IOException
```
## snakeyaml-extended-reader

`snakeyaml-extended-reader` generator creates `my.model.package.io.snakeyaml.ModelNameSnakeYamlReaderEx` class with same public methods as `snakeyaml-reader`, but with [location tracking enabled](../../location-tracking.html).

If source tracking is enabled in addition to location tracking, the public methods have an extra parameter which is the source tracker instance.

