package org.codehaus.modello;

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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PluginManager
    extends AbstractLogEnabled
{
    public final static String PROPERTY_IMPLEMENTATION = "modello.plugin.implementation";

    private Map plugins = new HashMap();

    public PluginManager()
    {
    }

    public void initialize()
        throws ModelloException
    {
        getLogger().debug( "Loading plugins." );

        try
        {
            Enumeration e = getClass().getClassLoader().getResources( "META-INF/modello/plugin.properties" );
    
            while ( e.hasMoreElements() )
            {
                URL url = (URL) e.nextElement();
    
                Properties p = new Properties();
    
                p.load( url.openStream() );
    
                loadPlugin( p );
            }
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while loading the plugins.", ex );
        }
    }

    public ModelloPlugin getPlugin( String pluginId )
        throws ModelloException
    {
        ModelloPlugin plugin = (ModelloPlugin) plugins.get( pluginId );

        if ( plugin == null )
        {
            throw new ModelloException( "No such plugin: '" + pluginId + "'." );
        }

        return plugin;
    }

    public boolean hasPlugin( String pluginId )
    {
        return plugins.containsKey( pluginId );
    }

    public Iterator getPlugins()
    {
        return plugins.values().iterator();
    }

    private void loadPlugin( Properties p )
        throws ModelloException
    {
        ModelloPlugin plugin;

        String implementation = p.getProperty( PROPERTY_IMPLEMENTATION );

        Object obj;

        try
        {
            obj = getClass().forName( implementation ).newInstance();
        }
        catch( ClassNotFoundException ex )
        {
            throw new ModelloException( "Could not find the plugin implementation class: '" + implementation + "'.", ex );
        }
        catch( InstantiationException ex )
        {
            throw new ModelloException( "Could not instanciate the plugin implementation class: '" + implementation + "'.", ex );
        }
        catch( IllegalAccessException ex )
        {
            throw new ModelloException( "Could not access the plugin implementation class: '" + implementation + "'.", ex );
        }

        if ( !(obj instanceof ModelloPlugin ) )
        {
            throw new ModelloException( "A Modello plugin must implement the ModelloPlugin interface." );
        }

        plugin = (ModelloPlugin) obj;

        plugin.setLogger( getLogger() );

        plugins.put( plugin.getId(), plugin );        
    }
}
