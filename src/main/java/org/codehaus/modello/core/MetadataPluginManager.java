package org.codehaus.modello.core;

/*
 * LICENSE
 */

import java.util.Iterator;

import org.codehaus.modello.metadata.MetadataPlugin;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface MetadataPluginManager
{
    String ROLE = MetadataPluginManager.class.getName();

    Iterator getPlugins();

    MetadataPlugin getMetadataPlugin( String metadataId );

    boolean hasMetadataPlugin( String metadataId );
}
