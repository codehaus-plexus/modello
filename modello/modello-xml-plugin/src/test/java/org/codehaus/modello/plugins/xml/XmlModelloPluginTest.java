package org.codehaus.modello.plugins.xml;

/*
 * LICENSE
 */

import java.util.List;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.ModelloGeneratorTest;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class XmlModelloPluginTest
    extends ModelloGeneratorTest
{
    public XmlModelloPluginTest()
    {
        super( "xml" );
    }

    public void testXmlPlugin()
        throws Exception
    {
        Modello modello = getModello();

        Model model = modello.getModel( getTestFile( "src/test/resources/model.mdo" ) );

        List classes = model.getClasses();

        assertEquals( 2, classes.size() );

        ModelClass clazz = (ModelClass) classes.get( 0 );

        assertEquals( "Model", clazz.getName() );

        assertEquals( 3, clazz.getFields().size() );

        ModelField extend = clazz.getField( "extend" );

        assertTrue( extend.hasMetaData( XmlMetaData.ID ) );

        XmlMetaData xml = (XmlMetaData) extend.getMetaData( XmlMetaData.ID );

        assertNotNull( xml );

        assertTrue( xml.isAttribute() );

        ModelField parent = clazz.getField( "parent" );

        try
        {
            parent.getMetaData( "foo" );

            fail( "Expected ModelloException" );
        }
        catch( ModelloRuntimeException ex )
        {
            // expected
        }

        ModelField builder = clazz.getField( "builder" );

        assertTrue( builder.hasMetaData( XmlMetaData.ID ) );

        xml = (XmlMetaData) builder.getMetaData( XmlMetaData.ID );

        assertNotNull( xml );

        assertEquals( "build", xml.getTagName() );
    }
}
