package org.codehaus.modello.generator.snakeyaml;

import java.io.StringReader;
import java.io.StringWriter;

import org.codehaus.modello.test.model.Model;
import org.codehaus.modello.test.model.io.snakeyaml.MavenSnakeYamlReader;
import org.codehaus.modello.test.model.io.snakeyaml.MavenSnakeYamlWriter;
import org.codehaus.modello.verifier.Verifier;

import org.junit.jupiter.api.Assertions;

public class SnakeYamlVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Model model = new Model();
        model.setName( "modello IT" );

        StringWriter yaml = new StringWriter();
        new MavenSnakeYamlWriter().write( yaml, model );

        // the dumper uses the line break of the platform
        String actual = yaml.toString().replace( "\r\n", "\n" );

        // YAML directive with the version, document separator, then the model
        String expected = "%YAML 1.1\n---\n\"name\": \"modello IT\"\n";
        Assertions.assertEquals( expected, actual );

        Model read = new MavenSnakeYamlReader().read( new StringReader( yaml.toString() ) );
        Assertions.assertEquals( "modello IT", read.getName() );
    }
}
