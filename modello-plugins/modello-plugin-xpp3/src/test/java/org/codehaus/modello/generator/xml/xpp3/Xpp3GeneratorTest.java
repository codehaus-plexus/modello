package org.codehaus.modello.generator.xml.xpp3;

/*
 * LICENSE
 */

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.FileUtils;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.plugins.xml.XmlMetadata;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Xpp3GeneratorTest
    extends ModelloGeneratorTest
{
    public Xpp3GeneratorTest()
    {
        super( "xpp3" );
    }

    public void testXpp3Generator()
        throws Throwable
    {
        ModelloCore modello = getModelloCore();

        Model model = modello.loadModel( new FileReader( getTestPath( "src/test/resources/model.xml" ) ) );

        List classesList = model.getClasses();

        assertEquals( 20, classesList.size() );

        ModelClass clazz = (ModelClass) classesList.get( 0 );

        assertEquals( "Model", clazz.getName() );

        ModelField extend = clazz.getField( "extend" );

        assertTrue( extend.hasMetadata( XmlMetadata.ID ) );

        XmlMetadata xml = (XmlMetadata) extend.getMetadata( XmlMetadata.ID );

        assertNotNull( xml );

        assertTrue( xml.isAttribute() );

        assertEquals( "extender", xml.getTagName() );

        ModelField build = clazz.getField( "build" );

        assertTrue( build.hasMetadata( XmlMetadata.ID ) );

        xml = (XmlMetadata) build.getMetadata( XmlMetadata.ID );

        assertNotNull( xml );

        assertEquals( "builder", xml.getTagName() );

        File generatedSources = new File( getTestPath( "target/xpp3/sources" ) );

        File classes = new File( getTestPath( "target/xpp3/classes" ) );

        FileUtils.deleteDirectory( generatedSources );

        FileUtils.deleteDirectory( classes );

        generatedSources.mkdirs();

        classes.mkdirs();

        addDependency( "modello", "modello-core", "1.0-SNAPSHOT" );

        addDependency( "modello", "modello-xml-plugin", "1.0-SNAPSHOT" );

        addDependency( "xpp3", "xpp3", "1.1.3.3" );

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, generatedSources.getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );

//        modello.work2( model , "java", generatedSources, "4.0.0", false );

        modello.generate( model, "java", parameters );

//        modello.work2( model, "xpp3", generatedSources, "4.0.0", false );

        modello.generate( model, "xpp3-writer", parameters );

        modello.generate( model, "xpp3-reader", parameters );

        compile( generatedSources, classes );

        verify( "org.codehaus.modello.generator.xml.xpp3.Xpp3Verifier", "xpp3" );
    }
}
