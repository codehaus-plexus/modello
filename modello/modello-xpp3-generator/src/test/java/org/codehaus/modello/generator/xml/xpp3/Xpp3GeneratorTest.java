package org.codehaus.modello.generator.xml.xpp3;

/*
 * LICENSE
 */

import java.io.File;
import java.util.List;

import org.codehaus.modello.FileUtils;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloTest;
import org.codehaus.modello.plugins.xml.XmlMetaData;

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

        Model model = modello.getModel( getTestFile( "src/test/resources/model.xml" ) );

        List classesList = model.getClasses();

        assertEquals( 20, classesList.size() );

        ModelClass clazz = (ModelClass) classesList.get( 0 );

        assertEquals( "Model", clazz.getName() );

        ModelField extend = clazz.getField( "extend" );

        assertTrue( extend.hasMetaData( XmlMetaData.ID ) );

        XmlMetaData xml = (XmlMetaData) extend.getMetaData( XmlMetaData.ID );

        assertNotNull( xml );

        assertTrue( xml.isAttribute() );

        assertEquals( "extender", xml.getTagName() );

        ModelField build = clazz.getField( "build" );

        assertTrue( build.hasMetaData( XmlMetaData.ID ) );

        xml = (XmlMetaData) build.getMetaData( XmlMetaData.ID );

        assertNotNull( xml );

        assertEquals( "builder", xml.getTagName() );

        File modelFile = getTestFile( "src/test/resources/model.xml" );

        File generatedSources = getTestFile( "target/xpp3/sources" );

        File classes = getTestFile( "target/xpp3/classes" );

        FileUtils.deleteDirectory( generatedSources );

        FileUtils.deleteDirectory( classes );

        generatedSources.mkdirs();

        classes.mkdirs();

        addDependency( "modello", "modello-core", "1.0-SNAPSHOT" );

        addDependency( "modello", "modello-xml-metadata", "1.0-SNAPSHOT" );

        addDependency( "xpp3", "xpp3", "1.1.3.3" );

        modello.work2( model , "java", generatedSources, "4.0.0", false );

        modello.work2( model, "xpp3", generatedSources, "4.0.0", false );

        compile( generatedSources, classes );

        verify( "org.codehaus.modello.generator.xml.xpp3.Xpp3Verifier", "xpp3" );
    }
}
