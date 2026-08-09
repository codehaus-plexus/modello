<?xml version="1.0"?>

<document>

  <properties>
    <title>Modello JDOM Plugin</title>
    <author email="hboutemy_AT_apache_DOT_org">Hervé Boutemy</author>
  </properties>

  <body>

    <section name="Modello JDOM Plugin">

      <p>Modello JDOM Plugin generates XML writers based on <a href="http://jdom.org/">JDOM 1 API</a>.
        It attempts to preserve formatting of not changed elements and even with the changed ones it does
        some tricks to keep the formatting in line with the rest.</p>

      <p>Notice: DOM content type is represented as
      <a href="http://plexus.codehaus.org/plexus-utils/apidocs/org/codehaus/plexus/util/xml/Xpp3Dom.html">plexus-utils' Xpp3Dom</a> objects</p>

      <subsection name="jdom-writer">
      <p><code>jdom-writer</code> generator creates
        <code><i>my.model.package</i><b>.io.jdom.</b><i>ModelName</i><b>JDOMWriter</b></code> class with following
        public methods:
      </p>

      <ul>
        <li><code>public void write( <i>RootClass</i> root, Document document, OutputStream stream )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>

        <li><code>public void write( <i>RootClass</i> root, Document document, OutputStreamWriter writer )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>

        <li><code>public void write( <i>RootClass</i> root, Document document, Writer writer, Format jdomFormat )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>
      </ul>
      </subsection>

    </section>

  </body>

</document>
