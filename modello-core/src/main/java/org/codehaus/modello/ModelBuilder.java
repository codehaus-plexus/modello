package org.codehaus.modello;

/*
 * LICENSE
 */

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.alias.DefaultClassMapper;
import com.thoughtworks.xstream.alias.DefaultNameMapper;
import com.thoughtworks.xstream.objecttree.reflection.JavaReflectionObjectFactory;
import com.thoughtworks.xstream.xml.xpp3.Xpp3Dom;
import com.thoughtworks.xstream.xml.xpp3.Xpp3DomBuilder;
import com.thoughtworks.xstream.xml.xpp3.Xpp3DomXMLReader;
import com.thoughtworks.xstream.xml.xpp3.Xpp3DomXMLReaderDriver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelBuilder
    extends AbstractLogEnabled
{
    private XStream xstream;

    private static ModelBuilder instance;

    private Map metaDataClasses;

    public void initialize( PluginManager pluginManager )
    {
        xstream = new XStream( new JavaReflectionObjectFactory(), new DefaultClassMapper( new DefaultNameMapper() ), new Xpp3DomXMLReaderDriver() );

        xstream.alias( "model", Model.class );

        xstream.alias( "class", ModelClass.class );

        xstream.alias( "field", ModelField.class );

        xstream.alias( "codeSegment", CodeSegment.class );

        metaDataClasses = new HashMap();

        for ( Iterator it = pluginManager.getPlugins(); it.hasNext(); )
        {
            ModelloPlugin plugin = (ModelloPlugin) it.next();

            Class clazz = plugin.initializeXStream( xstream );

            metaDataClasses.put( clazz.getName(), plugin.getId() );
        }
    }

    public Model getModel( String modelFile )
        throws ModelloException
    {
        String modelContents = fileRead( modelFile );
    
        modelContents = replace( modelContents, "<description>", "<description><![CDATA[" );
    
        modelContents = replace( modelContents, "</description>", "]]></description>" );

        Xpp3Dom dom;

        try
        {
            dom = Xpp3DomBuilder.build( new StringReader( modelContents ) );
        }
        catch( Exception ex )
        {
            throw new ModelloException( "Exception while unmarshalling the model.", ex );
        }

        Model objectModel = (Model) xstream.fromXML( new Xpp3DomXMLReader( dom ) );

        objectModel.initialize( metaDataClasses );

        return objectModel;
    }

    protected String fileRead( String fileName )
        throws ModelloException
    {
        try
        {
            StringBuffer buf = new StringBuffer();

            FileInputStream in = new FileInputStream( fileName );

            int count;

            byte[] b = new byte[512];

            while ( ( count = in.read( b ) ) > 0 )
            {
                buf.append( new String( b, 0, count ) );
            }

            in.close();

            return buf.toString();
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Error while reading model.", ex );
        }
    }

    public static String replace( String text, String repl, String with )
    {
        return replace( text, repl, with, -1 );
    }

    public static String replace( String text, String repl, String with, int max )
    {
        if ( text == null || repl == null || with == null || repl.length() == 0 )
        {
            return text;
        }

        StringBuffer buf = new StringBuffer( text.length() );

        int start = 0, end = 0;

        while ( ( end = text.indexOf( repl, start ) ) != -1 )
        {
            buf.append( text.substring( start, end ) ).append( with );

            start = end + repl.length();

            if ( --max == 0 )
            {
                break;
            }
        }

        buf.append( text.substring( start ) );

        return buf.toString();
    }
}
