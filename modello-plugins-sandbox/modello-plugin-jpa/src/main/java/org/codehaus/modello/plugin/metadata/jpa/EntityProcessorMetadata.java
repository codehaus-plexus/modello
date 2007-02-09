/**
 * 
 */
package org.codehaus.modello.plugin.metadata.jpa;

import org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata;
import org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata;

/**
 * {@link ProcessorMetadata} extension for JPA <code>Entity</code> annotation.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id$
 * @plexus.component role="org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata"
 *                   role-hint="entity"
 */
public class EntityProcessorMetadata
    implements ClassMetadataProcessorMetadata
{

    public static final String KEY = "entity";

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata#getKey()
     */
    public String getKey()
    {
        return KEY;
    }

}
