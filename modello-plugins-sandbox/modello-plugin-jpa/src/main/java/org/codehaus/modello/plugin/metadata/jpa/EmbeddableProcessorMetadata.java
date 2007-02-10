/**
 * 
 */
package org.codehaus.modello.plugin.metadata.jpa;

import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata;
import org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata;

/**
 * {@link ProcessorMetadata} extension for JPA <code>Embeddable</code>
 * annotation.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id$
 * @plexus.component role="org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata"
 *                   role-hint="embeddable"
 */
public class EmbeddableProcessorMetadata
    implements ClassMetadataProcessorMetadata
{

    public static final String KEY = "embeddable";

    private ModelClass modelClass;

    private String packageName;

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata#getKey()
     */
    public String getKey()
    {
        return KEY;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata#getModelClass()
     */
    public ModelClass getModelClass()
    {
        return this.modelClass;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata#setModelClass(org.codehaus.modello.model.ModelClass)
     */
    public void setModelClass( ModelClass modelClass )
    {
        this.modelClass = modelClass;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata#getpackageName()
     */
    public String getpackageName()
    {
        return this.packageName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata#setPackageName(java.lang.String)
     */
    public void setPackageName( String packageName )
    {
        this.packageName = packageName;
    }

}
