<?xml version="1.0"?>

<document>

  <properties>
    <title>Modello Jackson Plugin</title>
    <author email="simonetripodi@apache.org">Simone Tripodi</author>
  </properties>

  <body>

    <section name="Modello Jackson Plugin">

      <p>Modello Jackson Plugin generates YAML readers and writers based on
        <a href="http://wiki.fasterxml.com/JacksonHome">Jackson APIs</a>,
        plus reader delegates to be able to read multiple model versions.</p>

      <subsection name="jackson-reader">
      <p><code>jackson-reader</code> generator creates
        <code><i>my.model.package</i><b>.io.jackson.</b><i>ModelName</i><b>JacksonReader</b></code> class with following
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

      <subsection name="jackson-writer">
      <p><code>jackson-writer</code> generator creates
        <code><i>my.model.package</i><b>.io.jackson.</b><i>ModelName</i><b>JacksonWriter</b></code> class with following
        public methods:
      </p>

      <ul>
        <li><code>public void write( OutputStream output, <i>RootClass</i> root )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>
        <li><code>public void write( Writer writer, <i>RootClass</i> root )<br/>
            &#160;&#160;&#160;&#160;throws IOException</code></li>
      </ul>
      </subsection>

      <subsection name="jackson-extended-reader">
      <p><code>jackson-extended-reader</code> generator creates
      <code><i>my.model.package</i><b>.io.jackson.</b><i>ModelName</i><b>JacksonReaderEx</b></code> class with same public methods
      as <code>jackson-reader</code>, but with <a href="../../location-tracking.html">location tracking enabled</a>.</p>
      <p>If source tracking is enabled in addition to location tracking, the public methods have an extra parameter which
      is the source tracker instance.</p>
      </subsection>

    </section>

  </body>

</document>
