package org.codehaus.modello.plugin;

/*
 * LICENSE
 */

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import org.codehaus.modello.BaseElement;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.Version;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @version $Id$
 */
public abstract class AbstractModelloGenerator
    extends AbstractLogEnabled
    implements ModelloGenerator
{
    private Model model;

    private File outputDirectory;

    private Version generatedVersion;

    private boolean packageWithVersion;

    protected void initialize( Model model, Properties parameters )
        throws ModelloException
    {
        this.model = model;

        outputDirectory = new File( getParameter( ModelloParameterConstants.OUTPUT_DIRECTORY, parameters ) );

        String version = getParameter( ModelloParameterConstants.VERSION, parameters );

        generatedVersion = new Version( version );

        packageWithVersion = Boolean.valueOf( getParameter( ModelloParameterConstants.PACKAGE_WITH_VERSION, parameters ) ).booleanValue();
    }

    protected Model getModel()
    {
        return model;
    }

    protected Version getGeneratedVersion()
    {
        return generatedVersion;
    }

    protected boolean isPackageWithVersion()
    {
        return packageWithVersion;
    }

    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    protected boolean outputElement( BaseElement element )
        throws ModelloRuntimeException
    {
        return generatedVersion.inside( element.getElementVersion() );
    }

    protected boolean isClassInModel( String fieldType, Model model )
    {
        return model.getClass( fieldType ) != null;
    }

    protected boolean isMap( String fieldType )
    {
        if ( fieldType == null )
        {
            return false;
        }

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
        if ( fieldType == null )
        {
            return false;
        }

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
        if ( str == null || str.length() == 0 )
        {
            return str;
        }

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

    public static String uncapitalise( String str )
    {
        if ( str == null || str.length() == 0 )
        {
            return str;
        }

        return new StringBuffer( str.length() )
            .append( Character.toLowerCase( str.charAt( 0 ) ) )
            .append( str.substring( 1 ) )
            .toString();
    }

    protected String getBasePackageName()
    {
        StringBuffer sb = new StringBuffer();

        sb.append( model.getPackageName() );

        if ( isPackageWithVersion() )
        {
            sb.append( "." );

            sb.append( getGeneratedVersion().toString() );
        }

        return sb.toString();
    }

    protected void addModelImports( JClass jClass )
        throws ModelloException
    {
        for ( Iterator i = getModel().getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            if ( outputElement( modelClass ) )
            {
                jClass.addImport( getBasePackageName() + "." + modelClass.getName() );
            }
        }
    }

    // ----------------------------------------------------------------------
    // Text utils
    // ----------------------------------------------------------------------

    protected boolean isEmpty( String string )
    {
        return string == null || string.trim().length() == 0;
    }

    private String getParameter( String name, Properties parameters )
    {
        String value = parameters.getProperty( name );

        if ( value == null )
        {
            throw new ModelloRuntimeException( "Missing parameter '" + name + "'." );
        }

        return value;
    }
}
