package org.codehaus.modello.core;

/*
 * LICENSE
 */

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.plugin.AbstractPluginManager;
import org.codehaus.modello.plugin.ModelloGenerator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultGeneratorPluginManager
    extends AbstractPluginManager
    implements GeneratorPluginManager
{
    public ModelloGenerator getGeneratorPlugin( String generatorId )
    {
        ModelloGenerator generator = (ModelloGenerator) getPlugin( generatorId );

        if ( generator == null )
        {
            throw new ModelloRuntimeException( "No such generator plugin: '" + generatorId + "'." );
        }

        return generator;
    }

    public boolean hasGeneratorPlugin( String generatorId )
    {
        return hasPlugin( generatorId );
    }
}
