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
public interface MetadataPlugin
{
    String ROLE = MetadataPlugin.class.getName();

    Metadata getModelMetadata( Model model, Map data )
        throws ModelloException;

    Metadata getClassMetadata( ModelClass clazz, Map data )
        throws ModelloException;

    Metadata getFieldMetadata( ModelField field, Map data )
        throws ModelloException;

    Map getModelMap( Model model, Metadata metadata )
        throws ModelloException;

    Map getClassMap( ModelClass clazz, Metadata metadata )
        throws ModelloException;

    Map getFieldMap( ModelField field, Metadata metadata )
        throws ModelloException;
}
