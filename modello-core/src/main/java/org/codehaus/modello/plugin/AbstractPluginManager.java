package org.codehaus.modello.plugin;

/*
 * LICENSE
 */

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractPluginManager
    extends AbstractLogEnabled
    implements Initializable
{
    private Map plugins;

    private Class pluginClass;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        if ( plugins == null )
        {
            plugins = Collections.EMPTY_MAP;
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
