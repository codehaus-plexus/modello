package org.codehaus.modello.metadata;

/*
 * LICENSE
 */

import java.util.Map;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface MetaDataPlugin
{
    MetaData getModelMetaData( Model model, Map data )
        throws ModelloException;

    MetaData getClassMetaData( ModelClass clazz, Map data )
        throws ModelloException;

    MetaData getFieldMetaData( ModelField field, Map data )
        throws ModelloException;

    Map getModelMap( Model model, MetaData metaData )
        throws ModelloException;

    Map getClassMap( ModelClass clazz, MetaData metaData )
        throws ModelloException;

    Map getFieldMap( ModelField field, MetaData metaData )
        throws ModelloException;
}
