/**
 * 
 */
package org.codehaus.modello.plugin.metadata.processor;

/**
 * {@link MetadataProcessor} factory creates and returns an instance of an
 * appropriate {@link MetadataProcessor} extension.
 * <p>
 * How the instances are created (or looked up) is internal to the factory and
 * abstracted away from the clients.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id$
 */
public interface MetadataProcessorFactory
{

    /**
     * Role key used for looking up the {@link MetadataProcessorFactory}
     * extensions in the Plexus Container.
     */
    public static final String ROLE = MetadataProcessorFactory.class.getName();

    /**
     * Creates and returns a {@link MetadataProcessor} for the specified key.
     * 
     * @param processorMetadata
     * @return {@link MetadataProcessor} instance for the passed in
     *         {@link ProcessorMetadata}.
     * @throws MetadataProcessorInstantiationException if there was an error
     *             creating an appropriate {@link MetadataProcessor}.
     */
    public MetadataProcessor createMetadataProcessor( ProcessorMetadata processorMetadata )
        throws MetadataProcessorInstantiationException;

}
