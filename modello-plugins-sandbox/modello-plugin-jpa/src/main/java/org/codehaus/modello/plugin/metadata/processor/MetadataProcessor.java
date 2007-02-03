/**
 * 
 */
package org.codehaus.modello.plugin.metadata.processor;

/**
 * MetadataProcessor extensions are reponsible for processing Plugin specific
 * metadata.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 */
public interface MetadataProcessor
{

    /**
     * Checks the current metadata for validity.
     * 
     * @param context
     * @param metadata {@link ProcessorMetadata} to check for validity.
     * @return <code>true</code> if the metadata is valid, else
     *         <code>false</code>.
     */
    public boolean validate( Object context, ProcessorMetadata metadata );

    /**
     * Processes metadata and adds the results to the context.
     * 
     * @param context
     * @param metadata {@link ProcessorMetadata} to process.
     * @throws MetadataProcessorException if there was an error processing the
     *             metadata.
     */
    public void process( Object context, ProcessorMetadata metadata )
        throws MetadataProcessorException;

}
