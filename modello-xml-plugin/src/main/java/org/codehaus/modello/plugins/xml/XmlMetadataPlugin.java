package org.codehaus.modello.plugins.xml;

/*
 * LICENSE
 */

import java.util.Collections;
import java.util.Map;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.metadata.AbstractMetadataPlugin;
import org.codehaus.modello.metadata.Metadata;
import org.codehaus.modello.metadata.MetadataPlugin;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class XmlMetadataPlugin
    extends AbstractMetadataPlugin
    implements MetadataPlugin
{
    // ----------------------------------------------------------------------
    // Map to Metadata
    // ----------------------------------------------------------------------

    public Metadata getModelMetadata( Model model, Map data )
    {
        return new XmlMetadata();
    }

    public Metadata getClassMetadata( ModelClass clazz, Map data )
    {
        return new XmlMetadata();
    }

    public Metadata getFieldMetadata( ModelField field, Map data )
    {
        XmlMetadata metadata = new XmlMetadata();

        String attribute = (String) data.get( "attribute" );

        String tagName = (String) data.get( "tagName" );

        metadata.setAttribute( Boolean.valueOf( attribute ).booleanValue() );

        metadata.setTagName( tagName );

        return metadata;
    }

    // ----------------------------------------------------------------------
    // Metadata to Map
    // ----------------------------------------------------------------------

    public Map getModelMap( Model model, Metadata metadata )
    {
        return Collections.EMPTY_MAP;
    }

    public Map getClassMap( ModelClass clazz, Metadata metadata )
    {
        return Collections.EMPTY_MAP;
    }

    public Map getFieldMap( ModelField field, Metadata metadata )
    {
        return Collections.EMPTY_MAP;
    }
}
