package org.codehaus.modello.plugin;

/*
 * LICENSE
 */

import org.codehaus.modello.plugin.AbstractPluginManager;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class TestPluginManager
    extends AbstractPluginManager
{
    public TestPluginManager()
    {
        super( TestPlugin.class );
    }
}
