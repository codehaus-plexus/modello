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
     * Key for {@link MetadataProcessor} lookups in a Plexus container where the
     * processor extensions can be distinguished based on <code>role-hint</code>.
     */
    public static String ROLE = MetadataProcessor.class.getName();

    /**
     * Checks the current metadata for validity.
     * 
     * @param context
     * @param metadata {@link ProcessorMetadata} to check for validity.
     * @return <code>true</code> if the metadata is valid, else
     *         <code>false</code>.
     */
    public boolean validate( MetadataProcessorContext context, ProcessorMetadata metadata );

    /**
     * Processes metadata and adds the results to the context.
     * 
     * @param context
     * @param metadata {@link ProcessorMetadata} to process.
     * @throws MetadataProcessorException if there was an error processing the
     *             metadata.
     */
    public void process( MetadataProcessorContext context, ProcessorMetadata metadata )
        throws MetadataProcessorException;

}
