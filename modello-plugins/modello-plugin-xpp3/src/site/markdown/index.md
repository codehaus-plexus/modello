---
title: Modello XPP3 Plugin
author: 
  - Hervé Boutemy
---

# Modello XPP3 Plugin

Modello XPP3 Plugin generates XML readers and writers based on XPP3 API (XML Pull Parser) provided by [plexus-utils](http://plexus.codehaus.org/plexus-utils/).

Notice: DOM content type can be represented either as [plexus-utils' Xpp3Dom](http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/xml/Xpp3Dom.html) or, since Modello 1.6, standard [org.w3c.dom.Element](http://docs.oracle.com/javase/1.4.2/docs/api/org/w3c/dom/Element.html) objects

## xpp3-reader

`xpp3-reader` generator creates `my.model.package.io.xpp3.ModelNameXpp3Reader` class with following public methods: 

```java
public RootClass read( Reader reader, boolean strict )
    throws IOException, XmlPullParserException

public RootClass read( Reader reader )
    throws IOException, XmlPullParserException

public RootClass read( InputStream in, boolean strict )
    throws IOException, XmlPullParserException

public RootClass read( InputStream in )
    throws IOException, XmlPullParserException
```
- `public void setAddDefaultEntities( boolean addDefaultEntities )`

- `public boolean getAddDefaultEntities()`

## xpp3-writer

`xpp3-writer` generator creates `my.model.package.io.xpp3.ModelNameXpp3Writer` class with following public methods: 

```java
public void write( Writer writer, RootClass root )
    throws IOException
```
## xpp3-extended-reader

`xpp3-extended-reader` generator creates `my.model.package.io.xpp3.ModelNameXpp3ReaderEx` class with same public methods as `xpp3-reader`, but with [location tracking enabled](../../location-tracking.html).

If source tracking is enabled in addition to location tracking, the public methods have an extra parameter which is the source tracker instance.

## xpp3-extended-writer

`xpp3-extended-writer` generator creates `my.model.package.io.xpp3.ModelNameXpp3WriterEx` class with same public methods as `xpp3-writer`, but it adds location tracking information on each written field as comments. 

