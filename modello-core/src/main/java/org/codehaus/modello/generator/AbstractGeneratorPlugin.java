package org.codehaus.modello.generator;

import java.io.File;
import java.util.Properties;

import org.codehaus.modello.AbstractLogEnabled;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.ModelloRuntimeException;

// Possibly a general package extension for things like reader/writer

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @version $Id$
 */
public abstract class AbstractGeneratorPlugin
    extends AbstractLogEnabled
    implements GeneratorPlugin
{/*
    private XStream xstream;
*/
    private Model model;

    private File outputDirectory;

    private Version modelVersion;

    private boolean packageWithVersion;

    protected void initialize( Model model, Properties parameters )
        throws ModelloException
    {
        this.model = model;

        outputDirectory = new File( (String) parameters.get( ModelloParameterConstants.OUTPUT_DIRECTORY ) );

        String version = (String) parameters.get( ModelloParameterConstants.VERSION );

        modelVersion = new Version( version, "model" );

        packageWithVersion = Boolean.valueOf( (String) parameters.get( ModelloParameterConstants.PACKAGE_WITH_VERSION ) ).booleanValue();
    }

    protected Model getModel()
    {
        return model;
    }

    protected Version getModelVersion()
    {
        return modelVersion;
    }

    protected boolean isPackageWithVersion()
    {
        return packageWithVersion;
    }

    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    protected boolean outputElement( String elementVersion, String elementName )
        throws ModelloRuntimeException
    {
        if ( elementVersion == null )
        {
            System.err.println( elementName + " has a null version!" );

            return false;
        }

        Version v = new Version( elementVersion, elementName );

        // 4.0.0 - 3.0.0
        // 4.0.0 - 3.0.0+

        if ( v.major == modelVersion.major )
        {
            return true;
        }
        else if ( ( v.major < modelVersion.major ) && v.modifier != null && v.modifier.equals( "+" ) )
        {
            return true;
        }

        return false;
    }

    public static class Version
    {
        short major;

        short minor;

        short micro;

        String majorString;

        String minorString;

        String microString;

        String modifier;

        public Version( String version, String elementName )
        {
            if ( version == null )
            {
                throw new ModelloRuntimeException( "Syntax error in the version field: Missing. Element name: " + elementName );
            }

            version = version.trim();

            if ( version.length() < 5 )
            {
                throw new ModelloRuntimeException( "Syntax error in the <version> field: The field must be at least 5 characters long. Was: '" + version + "'. Element name: " + elementName );
            }

            majorString = version.substring( 0, 1 );

            minorString = version.substring( 2, 3 );

            microString = version.substring( 4, 5 );

            try
            {
                major = Short.parseShort( majorString );

                minor = Short.parseShort( minorString );

                micro = Short.parseShort( microString );
            }
            catch ( NumberFormatException e )
            {
                throw new ModelloRuntimeException( elementName + " version is invalid!" );
            }

            if ( version.length() >= 6 )
            {
                modifier = version.substring( 5 );
            }
        }

        public String toString()
        {
            return "v" + majorString + minorString + microString;
        }

        public String toString( String prefix)
        {
            return prefix + majorString + minorString + microString;
        }
    }

    protected boolean isClassInModel( String fieldType, Model model )
    {
        return model.getClassNames().contains( fieldType );
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

    protected boolean isEmpty( String string )
    {
        return string == null || string.trim().length() == 0;
    }
}
