<?xml version="1.0"?>

<document>

  <properties>
    <title>Modello Dom4J Plugin</title>
    <author email="hboutemy_AT_apache_DOT_org">Hervé Boutemy</author>
  </properties>

  <body>

    <section name="Modello Dom4J Plugin">

      <p>Modello Dom4J Plugin generates XML readers and writers based on
         <a href="http://dom4j.sourceforge.net/dom4j-1.6.1/">Dom4J 1 API</a>.</p>

      <p>Notice: DOM content type is represented as
      <a href="http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/xml/Xpp3Dom.html">plexus-utils' Xpp3Dom</a> objects</p>

      <subsection name="dom4j-reader">
      <p><code>dom4j-reader</code> generator creates
        <code><i>my.model.package</i><b>.io.dom4j.</b><i>ModelName</i><b>Dom4JReader</b></code> class with following
        public methods:
      </p>
      <ul>
        <li><code>public <i>RootClass</i> read( Reader reader, boolean strict )<br/>
            &#160;&#160;&#160;&#160;throws IOException, DocumentException</code></li>

        <li><code>public <i>RootClass</i> read( Reader reader )<br/>
            &#160;&#160;&#160;&#160;throws IOException, DocumentException</code></li>

        <li><code>public <i>RootClass</i> read( URL url, boolean strict )<br/>
            &#160;&#160;&#160;&#160;throws IOException, DocumentException</code></li>

        <li><code>public <i>RootClass</i> read( URL url )<br/>
            &#160;&#160;&#160;&#160;throws IOException, DocumentException</code></li>
      </ul>
      </subsection>

      <subsection name="dom4j-writer">
      <p><code>dom4j-writer</code> generator creates
        <code><i>my.model.package</i><b>.io.dom4j.</b><i>ModelName</i><b>Dom4JWriter</b></code> class with following
        public methods:
      </p>

      <ul>
        <li><code>public void write( Writer writer, <i>RootClass</i> root )<br/>
            &#160;&#160;&#160;&#160;throws java.io.IOException</code></li>
      </ul>
      </subsection>

    </section>

  </body>

</document>
