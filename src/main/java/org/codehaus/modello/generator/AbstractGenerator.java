package org.codehaus.modello.generator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.alias.DefaultClassMapper;
import com.thoughtworks.xstream.alias.DefaultNameMapper;
import com.thoughtworks.xstream.objecttree.reflection.JavaReflectionObjectFactory;
import com.thoughtworks.xstream.xml.xpp3.Xpp3Dom;
import com.thoughtworks.xstream.xml.xpp3.Xpp3DomBuilder;
import com.thoughtworks.xstream.xml.xpp3.Xpp3DomXMLReader;
import com.thoughtworks.xstream.xml.xpp3.Xpp3DomXMLReaderDriver;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;

import java.io.StringReader;
import java.io.IOException;
import java.io.FileInputStream;

/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public abstract class AbstractGenerator
{
    private XStream xstream;

    private String model;

    private String outputDirectory;

    protected AbstractGenerator( String model, String outputDirectory )
    {
        this.model = model;

        this.outputDirectory = outputDirectory;

        xstream = new XStream( new JavaReflectionObjectFactory(), new DefaultClassMapper( new DefaultNameMapper() ), new Xpp3DomXMLReaderDriver() );

        xstream.alias( "model", Model.class );

        xstream.alias( "class", ModelClass.class );

        xstream.alias( "field", ModelField.class );
    }

    protected Model getModel()
        throws Exception
    {
        String modelContents = fileRead( model );

        modelContents = replace( modelContents, "<description>", "<description><![CDATA[" );

        modelContents = replace( modelContents, "</description>", "]]></description>" );

        Xpp3Dom dom = Xpp3DomBuilder.build( new StringReader( modelContents ) );

        Model objectModel = (Model) xstream.fromXML( new Xpp3DomXMLReader( dom ) );

        objectModel.initialize();

        return objectModel;
    }

    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    public abstract void generate()
        throws Exception;

    protected boolean isClassInModel( String fieldType, Model model )
    {
        return model.getClassNames().contains( fieldType );
    }

    protected boolean isMap( String fieldType )
    {
        if ( fieldType.equals( "java.util.Map" ) )
        {
            return true;
        }
        else if ( fieldType.equals( "java.util.Properties" ) )
        {
            return true;
        }

        return false;
    }

    protected boolean isCollection( String fieldType )
    {
        if ( fieldType.equals( "java.util.List" ) )
        {
            return true;
        }
        else if ( fieldType.equals( "java.util.SortedSet" ) )
        {
            return true;
        }

        return false;
    }

    protected String capitalise( String str )
    {
        return new StringBuffer( str.length() )
            .append( Character.toTitleCase( str.charAt( 0 ) ) )
            .append( str.substring( 1 ) )
            .toString();
    }

    protected String singular( String name )
    {
        if ( name.endsWith( "ies" ) )
        {
            return name.substring( 0, name.length() - 3 ) + "y";
        }
        else if ( name.endsWith( "es" ) && name.endsWith( "ches" ) )
        {
            return name.substring( 0, name.length() - 2 );
        }
        else if ( name.endsWith( "s" ) )
        {
            return name.substring( 0, name.length() - 1 );
        }

        return name;
    }

    protected String fileRead( String fileName ) throws IOException
    {
        StringBuffer buf = new StringBuffer();

        FileInputStream in = new FileInputStream( fileName );

        int count;
        byte[] b = new byte[512];
        while ( ( count = in.read( b ) ) > 0 )  // blocking read
        {
            buf.append( new String( b, 0, count ) );
        }

        in.close();

        return buf.toString();
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

    public static String uncapitalise( String str )
    {
        return new StringBuffer( str.length() )
            .append( Character.toLowerCase( str.charAt( 0 ) ) )
            .append( str.substring( 1 ) )
            .toString();
    }
}
