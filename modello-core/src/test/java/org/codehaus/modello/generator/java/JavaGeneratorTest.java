package org.codehaus.modello.generator.java;

/*
 * LICENSE
 */

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JavaGeneratorTest
    extends ModelloGeneratorTest
{
    private String modelFile = "src/test/resources/models/maven.mdo";

    public JavaGeneratorTest()
    {
        super( "java" );
    }

    public void testJavaGenerator()
        throws Throwable
    {
        File generatedSources = new File( getTestPath( "target/java/source" ) );

        File classes = new File( getTestPath( "target/java/classes" ) );

        generatedSources.mkdirs();

        classes.mkdirs();

        ModelloCore modello = getModelloCore();

//        modello.work( getTestFile( modelFile ), "java", generatedSources, "4.0.0", false );

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, generatedSources.getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );

        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );

        Model model = modello.loadModel( new FileReader( getTestPath( modelFile ) ) );

        modello.generate( model, "java", parameters );

        compile( generatedSources, classes );

        verify( "org.codehaus.modello.generator.java.JavaVerifier", "java" );
    }
}
