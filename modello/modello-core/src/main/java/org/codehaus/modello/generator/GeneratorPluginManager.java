package org.codehaus.modello.generator;

/*
 * LICENSE
 */

import org.codehaus.modello.plugin.AbstractPluginManager;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class GeneratorPluginManager
    extends AbstractPluginManager
{
    public GeneratorPluginManager()
    {
        super( GeneratorPlugin.class );
    }

    public GeneratorPlugin getGeneratorPlugin( String name )
    {
        return (GeneratorPlugin) getPlugin( name );
    }
}
