package org.codehaus.modello.metadata;

/*
 * LICENSE
 */

import java.util.Collections;
import java.util.Map;

import org.codehaus.modello.Model;
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
    public Metadata getModelMetaData( Model model )
    {
        return null;
    }

    public Metadata getClassMetaData( ModelClass clazz )
    {
        return null;
    }

    public Metadata getFieldMetaData( ModelField field )
    {
        return null;
    }

    public Map getModelMap( Model model, Metadata metadata )
        throws ModelloException
    {
        return Collections.EMPTY_MAP;
    }

    public Map getClassMap( ModelClass clazz, Metadata metadata )
        throws ModelloException
    {
        return Collections.EMPTY_MAP;
    }

    public Map getFieldMap( ModelField field, Metadata metadata )
        throws ModelloException
    {
        return Collections.EMPTY_MAP;
    }
}
