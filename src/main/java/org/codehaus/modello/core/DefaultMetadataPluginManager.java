package org.codehaus.modello.core;

/*
 * LICENSE
 */

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.plugin.AbstractPluginManager;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultMetadataPluginManager
    extends AbstractPluginManager
    implements MetadataPluginManager
{
    public MetadataPlugin getMetadataPlugin( String metadataId )
    {
        MetadataPlugin metadata = (MetadataPlugin) getPlugin( metadataId );

        if ( metadata == null )
        {
            throw new ModelloRuntimeException( "No such metadata plugin: '" + metadataId + "'." );
        }

        return metadata;
    }

    public boolean hasMetadataPlugin( String metadataId )
    {
        return hasPlugin( metadataId );
    }
}
