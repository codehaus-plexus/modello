package org.codehaus.modello.plugins.xml;

/*
 * LICENSE
 */

import java.io.FileReader;
import java.util.List;

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.ModelloTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class XmlModelloPluginTest
    extends ModelloTest
{
    public void testConfiguration()
        throws Exception
    {
        Object object = lookup( MetadataPlugin.ROLE, "xml" );

        assertNotNull( object );

        assertTrue( object instanceof XmlMetadataPlugin );
    }

    public void testXmlPlugin()
        throws Exception
    {
        ModelloCore modello = getModelloCore();

        Model model = modello.loadModel( new FileReader( getTestPath( "src/test/resources/model.mdo" ) ) );

        List classes = model.getClasses( new Version( "4.0.0" ) );

        assertEquals( 2, classes.size() );

        ModelClass clazz = (ModelClass) classes.get( 0 );

        assertEquals( "Model", clazz.getName() );

        assertEquals( 3, clazz.getFields( new Version( "4.0.0" ) ).size() );

        ModelField extend = clazz.getField( "extend", new Version( "4.0.0" ) );

        assertTrue( extend.hasMetadata( XmlFieldMetadata.ID ) );

        XmlFieldMetadata xml = (XmlFieldMetadata) extend.getMetadata( XmlFieldMetadata.ID );

        assertNotNull( xml );

        assertTrue( xml.isAttribute() );

        ModelField parent = clazz.getField( "parent", new Version( "4.0.0" ) );

        try
        {
            parent.getMetadata( "foo" );

            fail( "Expected ModelloException" );
        }
        catch( ModelloRuntimeException ex )
        {
            // expected
        }

        ModelField builder = clazz.getField( "builder", new Version( "4.0.0" ) );

        assertTrue( builder.hasMetadata( XmlFieldMetadata.ID ) );

        xml = (XmlFieldMetadata) builder.getMetadata( XmlFieldMetadata.ID );

        assertNotNull( xml );

        assertEquals( "build", xml.getTagName() );
    }
}
