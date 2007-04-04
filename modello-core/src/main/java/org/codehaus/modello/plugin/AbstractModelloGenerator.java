package org.codehaus.modello.plugin;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.Version;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
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

        outputDirectory = new File( getParameter( parameters, ModelloParameterConstants.OUTPUT_DIRECTORY ) );

        String version = getParameter( parameters, ModelloParameterConstants.VERSION );

        generatedVersion = new Version( version );

        packageWithVersion = Boolean.valueOf(
            getParameter( parameters, ModelloParameterConstants.PACKAGE_WITH_VERSION ) ).booleanValue();
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

    protected boolean isClassInModel( String fieldType, Model model )
    {
        try
        {
            return model.getClass( fieldType, generatedVersion ) != null;
        }
        catch ( Exception e )
        {
        }

        return false;
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

    // ----------------------------------------------------------------------
    // Text utils
    // ----------------------------------------------------------------------

    protected boolean isEmpty( String string )
    {
        return string == null || string.trim().length() == 0;
    }

    // ----------------------------------------------------------------------
    // Parameter utils
    // ----------------------------------------------------------------------

    /**
     * @deprecated @{link Use getParameter( Properties, String )} instead
     */
    protected String getParameter( String name, Properties parameters )
    {
        return getParameter( parameters, name );
    }

    protected String getParameter( Properties parameters, String name )
    {
        String value = parameters.getProperty( name );

        if ( value == null )
        {
            throw new ModelloRuntimeException( "Missing parameter '" + name + "'." );
        }

        return value;
    }

    protected String getParameter( Properties parameters, String name, String defaultValue )
    {
        String value = parameters.getProperty( name );

        if ( value == null )
        {
            return defaultValue;
        }

        return value;
    }
}
