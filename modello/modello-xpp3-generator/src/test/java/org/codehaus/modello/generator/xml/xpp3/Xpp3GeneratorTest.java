package org.codehaus.modello.generator.xml.xpp3;

/*
 * LICENSE
 */

import java.io.File;

import org.codehaus.modello.FileUtils;
import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloTest;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Xpp3GeneratorTest
    extends ModelloTest
{
    public Xpp3GeneratorTest()
    {
        super( "xpp3" );
    }

    public void testXpp3Generator()
        throws Exception
    {
        Modello modello = getModello();

        File modelFile = getTestFile( "src/test/resources/model.xml" );

        File generatedSources = getTestFile( "target/xpp3/sources" );

        File classes = getTestFile( "target/xpp3/classes" );

        FileUtils.deleteDirectory( generatedSources );

        FileUtils.deleteDirectory( classes );

        generatedSources.mkdirs();

        classes.mkdirs();

        addDependency( "modello", "modello-core", "1.0-SNAPSHOT" );

        addDependency( "xpp3", "xpp3", "1.1.3.3" );

        modello.work( modelFile , "java", generatedSources, "4.0.0", false );

        modello.work( modelFile, "xpp3", generatedSources, "4.0.0", false );

        compile( generatedSources, classes );

        verify( "org.codehaus.modello.generator.xml.xpp3.Xpp3Verifier", "xpp3" );
    }
}
