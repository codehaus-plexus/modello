---
title: Modello StAX Plugin
author: 
  - Hervé Boutemy
---

# Modello StAX Plugin

Modello StAX Plugin generates XML readers and writers based on [StAX API](http://docs.oracle.com/javase/6/docs/api/javax/xml/stream/package-summary.html), plus reader delegates to be able to read multiple model versions.

Notice: DOM content type can be represented either as [plexus-utils' Xpp3Dom](http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/xml/Xpp3Dom.html) or, since Modello 1.6, standard [org.w3c.dom.Element](http://docs.oracle.com/javase/1.4.2/docs/api/org/w3c/dom/Element.html) objects

## stax-reader

`stax-reader` generator creates `my.model.package.io.stax.ModelNameStaxReader` class with following public methods: 

```java
public RootClass ( Reader reader, boolean strict )
    throws IOException, XMLStreamException

public RootClass read( Reader reader )
    throws IOException, XMLStreamException

public RootClass read( String filePath, boolean strict )
    throws IOException, XMLStreamException

public RootClass read( String filePath )
    throws IOException, XMLStreamException
```
In addition, if multiple model reader versions are generated (each in its own package), it creates a delegate `my.model.package.io.xpp3.ModelNameStaxReaderDelegate` class with following public methods: 

- `public Object read( File f, boolean strict )  
    &nbsp;&nbsp;&nbsp;&nbsp;throws IOException, XMLStreamException`

- `public Object read( File f )  
    &nbsp;&nbsp;&nbsp;&nbsp;throws IOException, XMLStreamException`

Depending on the model version found in the XML content, the returned `Object` will be of the right version package. 

## stax-writer

`stax-writer` generator creates `my.model.package.io.stax.ModelNameStaxWriter` class with following public methods: 

```java
public void write( Writer writer, RootClass root )
    throws IOException, XMLStreamException
```