package org.codehaus.modello.metadata;

/*
 * LICENSE
 */

import java.util.Collections;
import java.util.Map;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelAssociation;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractMetadataPlugin
    extends AbstractLogEnabled
    implements MetadataPlugin
{
    public Map getModelMap( Model model, ModelMetadata metadata )
        throws ModelloException
    {
        return Collections.EMPTY_MAP;
    }

    public Map getClassMap( ModelClass clazz, ClassMetadata metadata )
        throws ModelloException
    {
        return Collections.EMPTY_MAP;
    }

    public Map getFieldMap( ModelField field, FieldMetadata metadata )
        throws ModelloException
    {
        return Collections.EMPTY_MAP;
    }

    public Map getAssociationMap( ModelAssociation association, AssociationMetadata metadata )
        throws ModelloException
    {
        return Collections.EMPTY_MAP;
    }
}
