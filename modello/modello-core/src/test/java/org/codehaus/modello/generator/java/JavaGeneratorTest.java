package org.codehaus.modello.generator.java;

/*
 * LICENSE
 */

import java.io.File;

import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloTest;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JavaGeneratorTest
    extends ModelloTest
{
    private String modelFile = "model.xml";

    public JavaGeneratorTest()
    {
        super( "java" );
    }

    public void testJavaGenerator()
        throws Exception
    {
        File generatedSources = getTestFile( "target/java/source" );

        File classes = getTestFile( "target/java/classes" );

        generatedSources.mkdirs();

        classes.mkdirs();

        Modello modello = getModello();

        modello.work( getTestFile( modelFile ), "java", generatedSources, "4.0.0", false );

        compile( generatedSources, classes );

        verify( "org.codehaus.modello.generator.java.JavaVerifier", "java" );
    }
}
