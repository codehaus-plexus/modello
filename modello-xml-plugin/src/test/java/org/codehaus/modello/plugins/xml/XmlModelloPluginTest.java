package org.codehaus.modello.plugins.xml;

/*
 * LICENSE
 */

import java.io.File;
import java.util.List;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.ModelloTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class XmlModelloPluginTest
    extends ModelloTestCase
{
    public void testXmlPlugin()
        throws Exception
    {
        Modello modello = new Modello();

        modello.initialize();

        File output = getTestFile( "target/output" );

        output.mkdirs();

//      Model model = modello.work( getTestPath( "src/test/resources/model.mdo" ), "xml", output.getAbsolutePath(), "1.0.0", true );
        Model model = modello.getModel( getTestPath( "src/test/resources/model.mdo" ) );

        List classes = model.getClasses();

        assertEquals( 1, classes.size() );

        ModelClass clazz = (ModelClass) classes.get( 0 );

        assertEquals( "Model", clazz.getName() );

        assertEquals( 2, clazz.getFields().size() );

        ModelField extend = clazz.getField( "extend" );

        assertTrue( extend.hasMetaData( XmlMetaData.ID ) );

        XmlMetaData xml = (XmlMetaData) extend.getMetaData( XmlMetaData.ID );

        assertNotNull( xml );

        assertTrue( xml.isAttribute() );

        ModelField parent = clazz.getField( "parent" );

        try
        {
            parent.getMetaData( "foo" );

            fail( "Expected ModelloRuntimeException." );
        }
        catch( ModelloRuntimeException ex )
        {
            // expected
        }
    }
}
