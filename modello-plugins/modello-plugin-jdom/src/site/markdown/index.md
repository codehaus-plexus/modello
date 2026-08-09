---
title: Modello JDOM Plugin
author: 
  - Hervé Boutemy
---

# Modello JDOM Plugin

Modello JDOM Plugin generates XML writers based on [JDOM 1 API](http://jdom.org/). It attempts to preserve formatting of not changed elements and even with the changed ones it does some tricks to keep the formatting in line with the rest.

Notice: DOM content type is represented as [plexus-utils' Xpp3Dom](http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/xml/Xpp3Dom.html) objects

## jdom-writer

`jdom-writer` generator creates `my.model.package.io.jdom.ModelNameJDOMWriter` class with following public methods: 

```java
public void write( RootClass root, Document document, OutputStream stream )
    throws IOException

public void write( RootClass root, Document document, OutputStreamWriter writer )
    throws IOException

public void write( RootClass root, Document document, Writer writer, Format jdomFormat )
    throws IOException
```