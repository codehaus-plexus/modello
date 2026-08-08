---
title: Modello SAX Plugin
author: Simone Tripodi
---

# Modello SAX Plugin

Modello SAX Plugin generates SAX writers based on
[SAX APIs](http://docs.oracle.com/javase/1.4.2/docs/api/org/xml/sax/package-summary.html),
plus reader delegates to be able to read multiple model versions.

## sax-writer

The `sax-writer` generator creates a `my.model.package.io.sax.ModelNameSAXWriter` class — where
`my.model.package` and `ModelName` come from your model — with the following public methods:

```java
public void write( OutputStream output, RootClass root )
        throws SAXException, TransformerException

public void write( Writer writer, RootClass root )
        throws SAXException, TransformerException, UnsupportedEncodingException

// opens and closes the XML document
public void write( ContentHandler handler, RootClass root )
        throws SAXException

// opens and closes the XML document depending on the startDocument flag
public void write( ContentHandler handler, RootClass root, boolean startDocument )
        throws SAXException
```
