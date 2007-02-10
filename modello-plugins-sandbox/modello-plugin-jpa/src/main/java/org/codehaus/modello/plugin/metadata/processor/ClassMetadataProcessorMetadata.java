/**
 * 
 */
package org.codehaus.modello.plugin.metadata.processor;

import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.model.ModelClass;

/**
 * {@link ProcessorMetadata} extension that wraps up contextual information
 * supposed to be processed by a corresponding {@link MetadataProcessor}
 * implmentation.
 * 
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

    /**
     * Sets the {@link ModelClass} instance that is associated to this instance
     * of {@link ClassMetadataProcessorMetadata}.
     * 
     * @param modelClass
     */
    public void setModelClass( ModelClass modelClass );

    /**
     * Returns the {@link ModelClass} instance that is associated to this
     * instance of {@link ClassMetadataProcessorMetadata}.
     * 
     * @return
     */
    public ModelClass getModelClass();

    /**
     * Sets the package name to which the class belongs to.
     * 
     * @param packageName name of the package under which the class is located
     */
    public void setPackageName( String packageName );

    /**
     * Return the name of the package to which the class belongs to.
     * 
     * @return name of the package under which the class is located
     */
    public String getpackageName();

}
