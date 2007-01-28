/**
 * 
 */
package org.codehaus.modello.plugin.metadata;

import java.util.Map;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.metadata.AbstractMetadataPlugin;
import org.codehaus.modello.metadata.AssociationMetadata;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.plexus.util.StringUtils;

/**
 * A {@link MetadataPlugin} extension that processes JPA specific metadata.  
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 * @plexus.component role="org.codehaus.modello.metadata.MetadataPlugin" 
 *                   role-hint="jpa"
 */
public class JpaMetadataPlugin
    extends AbstractMetadataPlugin
{

    public static final String IS_ENTITY = "jpa.isEntity";

    public static final String IS_EMBEDDABLE = "jpa.isEmbeddable";

    public static final String TABLE_NAME = "jpa.table";

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.metadata.MetadataPlugin#getAssociationMetadata(org.codehaus.modello.model.ModelAssociation, java.util.Map)
     */
    public AssociationMetadata getAssociationMetadata( ModelAssociation modelAssociation, Map data )
        throws ModelloException
    {
        // TODO Auto-generated method stub        
        return new JpaAssociationLevelMetadata();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.metadata.MetadataPlugin#getClassMetadata(org.codehaus.modello.model.ModelClass, java.util.Map)
     */
    public ClassMetadata getClassMetadata( ModelClass modelClass, Map data )
        throws ModelloException
    {
        JpaClassLevelMetadata metadata = new JpaClassLevelMetadata();

        // TODO: set up Jpa specific metadata here
        metadata.setEntity( getBoolean( data, IS_ENTITY, false ) );

        String tableName = (String) data.get( TABLE_NAME );

        if ( !StringUtils.isEmpty( tableName ) )
        {
            metadata.setTable( tableName );
        }

        return metadata;
    }

    /** 
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.metadata.MetadataPlugin#getFieldMetadata(org.codehaus.modello.model.ModelField, java.util.Map)
     */
    public FieldMetadata getFieldMetadata( ModelField model, Map data )
        throws ModelloException
    {
        // TODO Auto-generated method stub
        return new JpaFieldLevelMetdata();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.metadata.MetadataPlugin#getModelMetadata(org.codehaus.modello.model.Model, java.util.Map)
     */
    public ModelMetadata getModelMetadata( Model model, Map data )
        throws ModelloException
    {
        return new JpaModelMetadata();
    }

}
