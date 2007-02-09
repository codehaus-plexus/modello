/**
 * 
 */
package org.codehaus.modello.plugin.metadata.jpa.processors;

import org.codehaus.modello.plugin.metadata.processor.MetadataProcessor;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorFactory;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorInstantiationException;
import org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since
 * @version $Id$
 * @plexus.component role="org.codehaus.modello.plugin.metadata.processor.MetadataProcessorFactory"
 *                   role-hint="jpa"
 */
public class JpaMetadataProcessorFactory
    extends AbstractLogEnabled
    implements MetadataProcessorFactory, Contextualizable
{

    private PlexusContainer container;

    /**
     * {@inheritDoc}
     * <p>
     * Makes the {@link PlexusContainer} instance available for Component
     * lookups.
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable#contextualize(org.codehaus.plexus.context.Context)
     */
    public void contextualize( Context context )
        throws ContextException
    {
        this.container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.MetadataProcessorFactory#createMetadataProcessor(org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata)
     */
    public MetadataProcessor createMetadataProcessor( ProcessorMetadata processorMetadata )
        throws MetadataProcessorInstantiationException
    {
        try
        {
            getLogger().debug( "Looking up MetadataProcessor for key: " + processorMetadata.getKey() );

            MetadataProcessor processor = (MetadataProcessor) container.lookup( MetadataProcessor.ROLE,
                                                                                processorMetadata.getKey() );
            return processor;
        }
        catch ( ComponentLookupException e )
        {
            throw new MetadataProcessorInstantiationException( "Unable to lookup MetadataProcessor for key: "
                + processorMetadata.getKey(), e );
        }
    }
}
