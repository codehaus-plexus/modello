package org.codehaus.modello.metadata;

/*
 * LICENSE
 */

import org.codehaus.modello.AbstractLogEnabled;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractMetaDataPlugin
    extends AbstractLogEnabled
    implements MetaDataPlugin
{
    public MetaData getModelMetaData( Model model )
    {
        return null;
    }

    public MetaData getClassMetaData( ModelClass clazz )
    {
        return null;
    }

    public MetaData getFieldMetaData( ModelField field )
    {
        return null;
    }
}
