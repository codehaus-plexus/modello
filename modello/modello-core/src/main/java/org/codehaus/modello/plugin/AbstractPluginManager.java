package org.codehaus.modello.plugin;

/*
 * LICENSE
 */

import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.modello.AbstractLogEnabled;
import org.codehaus.modello.LogEnabled;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractPluginManager
    extends AbstractLogEnabled
{
    private Class pluginClass;

    private Map plugins = new HashMap();

    public AbstractPluginManager( Class pluginClass )
    {
        this.pluginClass = pluginClass;
    }

    public void initialize()
        throws ModelloException
    {
        XStream xStream = new XStream();

        xStream.alias( "plugin-set", PluginSet.class );

        xStream.alias( "plugin", Plugin.class );

        try
        {
            Enumeration e = getClass().getClassLoader().getResources( "META-INF/modello/plugins.xml" );

            while ( e.hasMoreElements() )
            {
                URL url = (URL) e.nextElement();

                PluginSet pluginSet = (PluginSet) xStream.fromXML( new InputStreamReader( url.openStream() ) );

                loadPlugins( pluginSet );
            }
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while loading the plugin descriptor.", ex );
        }
    }

    private void loadPlugins( PluginSet pluginSet )
        throws ModelloException
    {
        if ( pluginSet == null || pluginSet.getPlugins() == null )
        {
            return;
        }

        for( Iterator it = pluginSet.getPlugins().iterator(); it.hasNext(); )
        {
            Plugin plugin = (Plugin) it.next();

            String id = plugin.getId();

            if ( id == null || id.trim().length() == 0 )
            {
                throw new ModelloException( "Missing element: 'id' from plugin descriptor." );
            }

            String implementation = plugin.getImplementation();

            if ( implementation == null || implementation.trim().length() == 0 )
            {
                throw new ModelloException( "Missing element: 'implementation' from plugin descriptor, id=" + id );
            }

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

            if ( !(pluginClass.isAssignableFrom( obj.getClass() ) ) )
            {
                continue;
//                throw new ModelloException( "The plugin " + obj.getClass().getName() + " must implement the " + pluginClass.getName() + " interface." );
            }

            if ( obj instanceof LogEnabled )
            {
                ((LogEnabled) obj).setLogger( getLogger() );
            }

            plugins.put( id, obj );
        }
    }

    public Iterator getPlugins()
    {
        return plugins.values().iterator();
    }

    public Object getPlugin( String name )
    {
        Object plugin = plugins.get( name );

        if ( plugin == null )
        {
            throw new ModelloRuntimeException( "No such plugin: " + name );
        }

        return plugin;
    }

    public boolean hasPlugin( String name )
    {
        return plugins.containsKey( name );
    }
}
