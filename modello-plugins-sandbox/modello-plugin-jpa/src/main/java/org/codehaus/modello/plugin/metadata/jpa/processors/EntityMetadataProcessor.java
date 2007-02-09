/**
 * 
 */
package org.codehaus.modello.plugin.metadata.jpa.processors;

import java.io.PrintWriter;

import org.codehaus.modello.plugin.metadata.processor.MetadataProcessor;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorContext;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorException;
import org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata;
import org.dom4j.Document;

/**
 * Processes any {@link EntityProcessorMetadata} instances encountered.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id$
 * @plexus.component role="org.codehaus.modello.plugin.metadata.processor.MetadataProcessor"
 *                   role-hint="entity"
 */
public class EntityMetadataProcessor
    implements MetadataProcessor
{

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.MetadataProcessor#process(MetadataProcessorContext,
     *      org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata)
     */
    public void process( MetadataProcessorContext context, ProcessorMetadata metadata )
        throws MetadataProcessorException
    {
        // TODO Auto-generated method stub
        System.out.println( "Processing metadata : " + metadata.getKey() );
        
        // TODO Contribute to the Document here.
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.MetadataProcessor#validate(MetadataProcessorContext,
     *      org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata)
     */
    public boolean validate( MetadataProcessorContext context, ProcessorMetadata metadata )
    {
        // TODO Auto-generated method stub
        return true;
    }

}
