---
title: Modello Dom4J Plugin
author: 
  - Hervé Boutemy
---

# Modello Dom4J Plugin

Modello Dom4J Plugin generates XML readers and writers based on [Dom4J 1 API](https://dom4j.github.io/).

Notice: DOM content type is represented as [plexus-xml's Xpp3Dom](https://codehaus-plexus.github.io/plexus-xml/apidocs/org.codehaus.plexus.util.xml/org/codehaus/plexus/util/xml/Xpp3Dom.html) objects

## dom4j-reader

`dom4j-reader` generator creates `my.model.package.io.dom4j.ModelNameDom4JReader` class with following public methods: 

```java
public RootClass read( Reader reader, boolean strict )
    throws IOException, DocumentException

public RootClass read( Reader reader )
    throws IOException, DocumentException

public RootClass read( URL url, boolean strict )
    throws IOException, DocumentException

public RootClass read( URL url )
    throws IOException, DocumentException
```
## dom4j-writer

`dom4j-writer` generator creates `my.model.package.io.dom4j.ModelNameDom4JWriter` class with following public methods: 

```java
public void write( Writer writer, RootClass root )
    throws java.io.IOException
```