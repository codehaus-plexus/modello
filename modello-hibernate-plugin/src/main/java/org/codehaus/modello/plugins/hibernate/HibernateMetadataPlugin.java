package org.codehaus.modello.plugins.hibernate;

/*
 * LICENSE
 */

import java.util.Collections;
import java.util.Map;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelAssociation;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.metadata.AbstractMetadataPlugin;
import org.codehaus.modello.metadata.AssociationMetadata;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.metadata.ModelMetadata;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class HibernateMetadataPlugin
    extends AbstractMetadataPlugin
    implements MetadataPlugin
{
    // ----------------------------------------------------------------------
    // Map to Metadata
    // ----------------------------------------------------------------------

    public ModelMetadata getModelMetadata( Model model, Map data )
    {
        return new HibernateModelMetadata();
    }

    public ClassMetadata getClassMetadata( ModelClass clazz, Map data )
    {
        return new HibernateClassMetadata();
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map data )
    {
        HibernateFieldMetadata metadata = new HibernateFieldMetadata();

        metadata.setLength( (String) data.get( "length" ) );

        String id = (String) data.get( "id" );
        System.out.println( field.getModelClass().getName() + ":" + field.getName() + ":id=" + id );

        if ( true )
        {
//            throw new RuntimeException( "fuck" );
        }

        if ( id != null )
        {
            metadata.setId( Boolean.valueOf( id ).booleanValue() );
        }

        metadata.setGenerator( (String) data.get( "generator" ) );

        return metadata;
    }

    public AssociationMetadata getAssociationMetadata( ModelAssociation association, Map data )
    {
        return new HibernateAssociationMetadata();
    }

    // ----------------------------------------------------------------------
    // Metadata to Map
    // ----------------------------------------------------------------------

    public Map getFieldMap( ModelField field, FieldMetadata metadata )
    {
        // TODO: implement
        return Collections.EMPTY_MAP;
    }
}
