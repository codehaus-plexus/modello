/**
 * 
 */
package org.codehaus.modello.plugin.metadata.processor;

import org.codehaus.modello.metadata.ClassMetadata;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id: ClassMetadataProcessorMetadata.java 794 2007-02-03 22:03:26Z
 *          rahul $
 */
public interface ClassMetadataProcessorMetadata
    extends ClassMetadata, ProcessorMetadata
{

    /**
     * Key to lookup {@link ClassMetadataProcessorMetadata} components.
     */
    public static final String ROLE = ClassMetadataProcessorMetadata.class.getName();

}
