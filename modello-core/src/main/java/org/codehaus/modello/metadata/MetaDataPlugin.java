package org.codehaus.modello.metadata;

/*
 * LICENSE
 */

import java.util.Map;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface MetaDataPlugin
{
    MetaData getModelMetaData( Model model, Map data );

    MetaData getClassMetaData( ModelClass clazz, Map data );

    MetaData getFieldMetaData( ModelField field, Map data );
}
