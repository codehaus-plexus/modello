package org.codehaus.modello.generator;

/*
 * LICENSE
 */

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.codehaus.modello.AbstractLogEnabled;
import org.codehaus.modello.LogEnabled;
import org.codehaus.modello.ModelloException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GeneratorPluginManager
    extends AbstractLogEnabled
{
    public final static String PROPERTY_IMPLEMENTATION = "modello.plugin.generator.implementation";

    public final static String PROPERTY_ID = "modello.plugin.generator.id";

    private Map generatorPlugins = new HashMap();

    public void initialize()
        throws ModelloException
    {
        getLogger().debug( "Loading generator plugins." );

        try
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            Enumeration e = classLoader.getResources( "META-INF/modello/generator.plugin.properties" );

            while ( e.hasMoreElements() )
            {
                URL url = (URL) e.nextElement();

                getLogger().info( "Loading " + url );

                Properties p = new Properties();

                p.load( url.openStream() );

                loadGenerator( p );
            }
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while loading the generators.", ex );
        }
    }

    public GeneratorPlugin getGeneratorPlugin( String generatorPluginId )
        throws ModelloException
    {
        GeneratorPlugin generator = (GeneratorPlugin) generatorPlugins.get( generatorPluginId );

        if ( generator == null )
        {
            throw new ModelloException( "No such generator: '" + generatorPluginId + "'." );
        }

        return generator;
    }

    public boolean hasGeneratorPlugin( String generatorId )
    {
        return generatorPlugins.containsKey( generatorId );
    }

    public Iterator getGeneratorPlugins()
    {
        return generatorPlugins.values().iterator();
    }

    private void loadGenerator( Properties p )
        throws ModelloException
    {
        GeneratorPlugin generator;

        String id = p.getProperty( PROPERTY_ID );

        if ( id == null || id.trim().length() == 0 )
        {
            throw new ModelloException( "Missing property from metadata plugin configuration: " + PROPERTY_ID );
        }

        String implementation = p.getProperty( PROPERTY_IMPLEMENTATION );

        if ( implementation == null || implementation.trim().length() == 0 )
        {
            throw new ModelloException( "Missing property from metadata plugin configuration: " + PROPERTY_IMPLEMENTATION );
        }

        Object obj;

        try
        {
            obj = getClass().forName( implementation ).newInstance();
        }
        catch( ClassNotFoundException ex )
        {
            throw new ModelloException( "Could not find the generator implementation class: '" + implementation + "'.", ex );
        }
        catch( InstantiationException ex )
        {
            throw new ModelloException( "Could not instanciate the generator implementation class: '" + implementation + "'.", ex );
        }
        catch( IllegalAccessException ex )
        {
            throw new ModelloException( "Could not access the generator implementation class: '" + implementation + "'.", ex );
        }

        if ( !(obj instanceof GeneratorPlugin ) )
        {
            throw new ModelloException( "A Modello generator must implement the ModelloPlugin interface." );
        }

        generator = (GeneratorPlugin) obj;

        if ( generator instanceof LogEnabled )
        {
            ((LogEnabled) generator).setLogger( getLogger() );
        }

        generatorPlugins.put( id, generator );        
    }
}
