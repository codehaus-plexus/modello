package org.codehaus.modello.metadata;

/*
 * LICENSE
 */

import java.util.Map;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelAssociation;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface MetadataPlugin
{
    String ROLE = MetadataPlugin.class.getName();

    ModelMetadata getModelMetadata( Model model, Map data )
        throws ModelloException;

    ClassMetadata getClassMetadata( ModelClass clazz, Map data )
        throws ModelloException;

    FieldMetadata getFieldMetadata( ModelField field, Map data )
        throws ModelloException;

    AssociationMetadata getAssociationMetadata( ModelAssociation association, Map data )
        throws ModelloException;

    Map getModelMap( Model model, ModelMetadata metadata )
        throws ModelloException;

    Map getClassMap( ModelClass clazz, ClassMetadata metadata )
        throws ModelloException;

    Map getFieldMap( ModelField field, FieldMetadata metadata )
        throws ModelloException;

    Map getAssociationMap( ModelAssociation association, AssociationMetadata metadata )
        throws ModelloException;
}
