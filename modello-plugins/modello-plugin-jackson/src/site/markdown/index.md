---
title: Modello Jackson Plugin
author: 
  - Simone Tripodi
---

# Modello Jackson Plugin

Modello Jackson Plugin generates YAML readers and writers based on [Jackson APIs](http://wiki.fasterxml.com/JacksonHome), plus reader delegates to be able to read multiple model versions.

## jackson-reader

`jackson-reader` generator creates `my.model.package.io.jackson.ModelNameJacksonReader` class with following public methods: 

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
## jackson-writer

`jackson-writer` generator creates `my.model.package.io.jackson.ModelNameJacksonWriter` class with following public methods: 

```java
public void write( OutputStream output, RootClass root )
    throws IOException

public void write( Writer writer, RootClass root )
    throws IOException
```
## jackson-extended-reader

`jackson-extended-reader` generator creates `my.model.package.io.jackson.ModelNameJacksonReaderEx` class with same public methods as `jackson-reader`, but with [location tracking enabled](../../location-tracking.html).

If source tracking is enabled in addition to location tracking, the public methods have an extra parameter which is the source tracker instance.

