<?xml version="1.0"?>

<document>

  <properties>
    <title>Modello StAX Plugin</title>
    <author email="hboutemy_AT_apache_DOT_org">Hervé Boutemy</author>
  </properties>

  <body>

    <section name="Modello StAX Plugin">

      <p>Modello StAX Plugin generates XML readers and writers based on
        <a href="http://docs.oracle.com/javase/6/docs/api/javax/xml/stream/package-summary.html">StAX API</a>,
        plus reader delegates to be able to read multiple model versions.</p>

      <p>Notice: DOM content type can be represented either as
      <a href="http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/xml/Xpp3Dom.html">plexus-utils' Xpp3Dom</a>
      or, since Modello 1.6, standard
      <a href="http://docs.oracle.com/javase/1.4.2/docs/api/org/w3c/dom/Element.html">org.w3c.dom.Element</a> objects</p>

      <subsection name="stax-reader">
      <p><code>stax-reader</code> generator creates
        <code><i>my.model.package</i><b>.io.stax.</b><i>ModelName</i><b>StaxReader</b></code> class with following
        public methods:
      </p>
      <ul>
        <li><code>public <i>RootClass</i> ( Reader reader, boolean strict )<br/>
            &#160;&#160;&#160;&#160;throws IOException, XMLStreamException</code></li>

        <li><code>public <i>RootClass</i> read( Reader reader )<br/>
            &#160;&#160;&#160;&#160;throws IOException, XMLStreamException</code></li>

        <li><code>public <i>RootClass</i> read( String filePath, boolean strict )<br/>
            &#160;&#160;&#160;&#160;throws IOException, XMLStreamException</code></li>

        <li><code>public <i>RootClass</i> read( String filePath )<br/>
            &#160;&#160;&#160;&#160;throws IOException, XMLStreamException</code></li>
      </ul>

      <p>In addition, if multiple model reader versions are generated (each in its own package), it creates a delegate
        <code><i>my.model.package</i><b>.io.xpp3.</b><i>ModelName</i><b>StaxReaderDelegate</b></code> class with
        following public methods:
      </p>
      <ul>
        <li><code>public Object read( File f, boolean strict )<br/>
            &#160;&#160;&#160;&#160;throws IOException, XMLStreamException</code></li>

        <li><code>public Object read( File f )<br/>
            &#160;&#160;&#160;&#160;throws IOException, XMLStreamException</code></li>
      </ul>
      <p>Depending on the model version found in the XML content, the returned <code>Object</code> will be of the right
        version package.
      </p>
      </subsection>

      <subsection name="stax-writer">
      <p><code>stax-writer</code> generator creates
        <code><i>my.model.package</i><b>.io.stax.</b><i>ModelName</i><b>StaxWriter</b></code> class with following
        public methods:
      </p>

      <ul>
        <li><code>public void write( Writer writer, <i>RootClass</i> root )<br/>
            &#160;&#160;&#160;&#160;throws IOException, XMLStreamException</code></li>
      </ul>
      </subsection>

    </section>

  </body>

</document>
