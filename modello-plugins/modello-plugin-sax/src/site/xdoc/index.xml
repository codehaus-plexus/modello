<?xml version="1.0"?>

<document>

  <properties>
    <title>Modello SAX Plugin</title>
    <author email="simonetripodi@apache.org">Simone Tripodi</author>
  </properties>

  <body>

    <section name="Modello SAX Plugin">

      <p>Modello SAX Plugin generates SAX writers based on
        <a href="http://docs.oracle.com/javase/1.4.2/docs/api/org/xml/sax/package-summary.html">SAX APIs</a>,
        plus reader delegates to be able to read multiple model versions.</p>

      <subsection name="jackson-writer">
      <p><code>sax-writer</code> generator creates
        <code><i>my.model.package</i><b>.io.sax.</b><i>ModelName</i><b>SAXWriter</b></code> class with following
        public methods:
      </p>

      <ul>
        <li><code>public void write( OutputStream output, <i>RootClass</i> root )<br/>
            &#160;&#160;&#160;&#160;throws SAXException, TransformerException</code></li>
        <li><code>public void write( Writer writer, <i>RootClass</i> root )<br/>
            &#160;&#160;&#160;&#160;throws SAXException, TransformerException, UnsupportedEncodingException</code></li>
        <li><code>public void write( org.xml.sax.ContentHandler, <i>RootClass</i> root )<br/>
            &#160;&#160;&#160;&#160;throws SAXException</code> (this method will take care to open/close the XML document)</li>
        <li><code>public void write( org.xml.sax.ContentHandler, <i>RootClass</i> root, boolean startDocument )<br/>
            &#160;&#160;&#160;&#160;throws SAXException</code> (this method will take care to open/close the XML document depending on the <code>startDocument</code> flag)</li>
      </ul>
      </subsection>

    </section>

  </body>

</document>
