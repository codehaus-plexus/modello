package org.codehaus.modello.metadata;

/*
 * LICENSE
 */

import org.codehaus.modello.AbstractLogEnabled;
import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;

import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractMetaDataPlugin
    extends AbstractLogEnabled
    implements MetaDataPlugin
{
    public Map getModelMap( Model model, MetaData metaData )
        throws ModelloException
    {
        return null;
    }

    public Map getClassMap( ModelClass clazz, MetaData metaData )
        throws ModelloException
    {
        return null;
    }

    public Map getFieldMap( ModelField field, MetaData metaData )
        throws ModelloException
    {
        return null;
    }
}
