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

import java.io.FileReader;

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
        Xpp3Dom dom = Xpp3DomBuilder.build( new FileReader( model ) );

        Model objectModel = (Model) xstream.fromXML( new Xpp3DomXMLReader( dom ) );

        objectModel.registerClassNames();

        return objectModel;
    }

    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    public abstract void generate()
        throws Exception;

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
}
