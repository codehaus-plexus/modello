<?xml version="1.0"?>

<document>

  <properties>
    <title>Modello Model Version Converter Plugin</title>
    <author email="hboutemy_AT_apache_DOT_org">Hervé Boutemy</author>
  </properties>

  <body>

    <section name="Modello Model Version Converter Plugin">

      <p>Modello Model Version Converter Plugin generates code to transform a model between two versions.</p>

      <subsection name="converters">
      <p>The source model version to convert from is in <code>modello.version</code> parameter: noted
        <code>A.B.C</code>.</p>
      <p>The target model version is the greatest of <code>modello.all.versions</code> parameter: noted
        <code>X.Y.Z</code>.</p>

      <p><code>converters</code> generator creates
        <code><i>my.model.package</i><b>.vA_B_C.converter.VersionConverter</b></code> interface and a
        <code><b>BasicVersionConverter</b></code> class implementing the interface to convert a class model from version
        <code>A.B.C</code> to <code>X.Y.Z</code>. If versions are different, model classes are supposed to be available
        in their corresponding java packages with version. If versions are equals, model classes are supposed to be
        available in the java package both with and without version, the package with version being the source model
        and the package without version being the target.
      </p>
      <p>For every class in the source model, a method is generated in the interface and an automatic implementation
        generated in the class with following signature:</p>
      <ul>
        <li><code>public <i>my.model.package</i>[.vX_Y_Z].<i>ModelClass</i> convert<i>ModelClass</i>(
          <i>my.model.package</i>.vA_B_C.<i>ModelClass</i> )</code></li>
      </ul>

      <p>In addition, if source and target versions are equals, a
        <code><i>my.model.package</i><b>.converter.ConverterTool</b></code> is generated to automate reading a file,
        detect its model version (using StAX reader delegate generated code), and convert it to the target model version
        in the java package without version using generated <code><b>BasicVersionConverter</b></code> class:</p>
      <ul>
        <li><code>public <i>my.model.package</i>.<i>RootClass</i> convertFromFile( File f )<br />
            &#160;&#160;&#160;&#160;throws IOException, XMLStreamException</code></li>
      </ul>
      </subsection>

    </section>

  </body>

</document>
