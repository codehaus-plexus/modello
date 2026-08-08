<?xml version="1.0"?>

<document>

  <properties>
    <title>Modello SnakeYaml Plugin</title>
    <author email="simonetripodi@apache.org">Simone Tripodi</author>
  </properties>

  <body>

    <section name="Modello SnakeYaml Plugin">

      <p>Modello SnakeYaml Plugin generates YAML readers and writers based on
        <a href="https://code.google.com/p/snakeyaml/">SnakeYaml APIs</a>,
        plus reader delegates to be able to read multiple model versions.</p>

      <subsection name="snakeyaml-reader">
      <p><code>snakeyaml-reader</code> generator creates
        <code><i>my.model.package</i><b>.io.snakeyaml.</b><i>ModelName</i><b>SnakeYamlReader</b></code> class with following
        public methods:
      </p>
      <ul>
        <li><code>public <i>RootClass</i> ( Reader reader, boolean strict )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>

        <li><code>public <i>RootClass</i> read( Reader reader )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>

        <li><code>public <i>RootClass</i> read( InputStream input, boolean strict )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>

        <li><code>public <i>RootClass</i> read( InputStream input )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>
      </ul>
      </subsection>

      <subsection name="snakeyaml-writer">
      <p><code>snakeyaml-writer</code> generator creates
        <code><i>my.model.package</i><b>.io.snakeyaml.</b><i>ModelName</i><b>SnakeYamlWriter</b></code> class with following
        public methods:
      </p>

      <ul>
        <li><code>public void write( OutputStream output, <i>RootClass</i> root )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>
        <li><code>public void write( Writer writer, <i>RootClass</i> root )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>
      </ul>
      </subsection>

      <subsection name="snakeyaml-extended-reader">
      <p><code>snakeyaml-extended-reader</code> generator creates
      <code><i>my.model.package</i><b>.io.snakeyaml.</b><i>ModelName</i><b>SnakeYamlReaderEx</b></code> class with same public methods
      as <code>snakeyaml-reader</code>, but with <a href="../../location-tracking.html">location tracking enabled</a>.</p>
      <p>If source tracking is enabled in addition to location tracking, the public methods have an extra parameter which
      is the source tracker instance.</p>
      </subsection>

    </section>

  </body>

</document>
