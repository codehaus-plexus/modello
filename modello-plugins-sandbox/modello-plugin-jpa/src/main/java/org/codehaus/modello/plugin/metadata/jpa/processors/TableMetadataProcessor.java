/**
 * 
 */
package org.codehaus.modello.plugin.metadata.jpa.processors;

import org.codehaus.modello.plugin.metadata.processor.MetadataProcessor;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorException;
import org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since
 * @version $Id$
 * @plexus.component role="org.codehaus.modello.plugin.metadata.processor.MetadataProcessor"
 *                   role-hint="table"
 */
public class TableMetadataProcessor
    implements MetadataProcessor
{

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.MetadataProcessor#process(java.lang.Object,
     *      org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata)
     */
    public void process( Object context, ProcessorMetadata metadata )
        throws MetadataProcessorException
    {
        // TODO Auto-generated method stub
        System.out.println( "Processing metadata : " + metadata.getKey() );

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.MetadataProcessor#validate(java.lang.Object,
     *      org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata)
     */
    public boolean validate( Object context, ProcessorMetadata metadata )
    {
        // TODO Auto-generated method stub
        System.out.println( "Validating metadata : " + metadata.getKey() );
        return false;
    }

}
