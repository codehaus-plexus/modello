/**
 * 
 */
package org.codehaus.modello.plugin.metadata.processor;

/**
 * {@link MetadataProcessor} factory creates and returns an instance of an
 * appropriate {@link MetadataProcessor}. How the instances are created (or
 * looked up) is internal to the factory and abstract away from the factory's
 * client.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id$
 */
public interface MetadataProcessorFactory
{

    /**
     * Creates and returns a {@link MetadataProcessor} for the specified key.
     * 
     * @param processorMetadata
     * @return {@link MetadataProcessor} instance for the passed in
     *         {@link ProcessorMetadata}.
     * @throws MetadataProcessorInstantiationException if there was an error
     *             creating an appropriate {@link MetadataProcessor}.
     */
    public MetadataProcessor createProcessor( ProcessorMetadata processorMetadata )
        throws MetadataProcessorInstantiationException;

}
