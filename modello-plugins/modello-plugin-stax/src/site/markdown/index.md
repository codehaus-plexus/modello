---
title: Modello StAX Plugin
author: 
  - Hervé Boutemy
---

# Modello StAX Plugin

Modello StAX Plugin generates XML readers and writers based on [StAX API](https://docs.oracle.com/javase/8/docs/api/javax/xml/stream/package-summary.html), plus reader delegates to be able to read multiple model versions.

Notice: DOM content type can be represented either as [plexus-xml's Xpp3Dom](https://codehaus-plexus.github.io/plexus-xml/apidocs/org.codehaus.plexus.util.xml/org/codehaus/plexus/util/xml/Xpp3Dom.html) or, since Modello 1.6, standard [org.w3c.dom.Element](https://docs.oracle.com/javase/8/docs/api/org/w3c/dom/Element.html) objects

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

By default the generated reader checks that the model version of the document matches the version of the reader. This applies only to models with a `versionDefinition`. Set the `versionCheck` parameter of the `stax-reader` goal (property `modello.stax.versionCheck`) to `false` to turn the check off.

## stax-writer

`stax-writer` generator creates `my.model.package.io.stax.ModelNameStaxWriter` class with following public methods: 

```java
public void write( Writer writer, RootClass root )
    throws IOException, XMLStreamException
```