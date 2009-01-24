package org.codehaus.modello.plugins.xml.metadata;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.modello.metadata.AbstractMetadataPlugin;
import org.codehaus.modello.metadata.AssociationMetadata;
import org.codehaus.modello.metadata.ClassMetadata;
import org.codehaus.modello.metadata.FieldMetadata;
import org.codehaus.modello.metadata.MetadataPlugin;
import org.codehaus.modello.metadata.ModelMetadata;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class XmlMetadataPlugin
    extends AbstractMetadataPlugin
    implements MetadataPlugin
{
    // ----------------------------------------------------------------------
    // Map to Metadata
    // ----------------------------------------------------------------------

    public ModelMetadata getModelMetadata( Model model, Map data )
    {
        XmlModelMetadata metadata = new XmlModelMetadata();

        metadata.setNamespace( getString(  data, "xml.namespace" ) );

        metadata.setSchemaLocation( getString( data, "xml.schemaLocation" ) );

        return metadata;
    }

    public ClassMetadata getClassMetadata( ModelClass clazz, Map data )
    {
        XmlClassMetadata metadata = new XmlClassMetadata();

        metadata.setTagName( getTagName( data ) );

        return metadata;
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map data )
    {
        XmlFieldMetadata metadata = new XmlFieldMetadata();

        metadata.setAttribute( getBoolean( data, "xml.attribute", false ) );

        metadata.setTrim( getBoolean( data, "xml.trim", true ) );

        metadata.setTagName( getTagName( data ) );

        metadata.setAssociationTagName( getString( data, "xml.associationTagName" ) );

        metadata.setListStyle( getString( data, "xml.listStyle" ) );

        metadata.setFormat( getString( data, "xml.format" ) );

        return metadata;
    }

    public AssociationMetadata getAssociationMetadata( ModelAssociation association, Map data )
    {
        XmlAssociationMetadata metadata = new XmlAssociationMetadata();

        metadata.setMapStyle( getString( data, "xml.mapStyle" ) );

        metadata.setReference( getBoolean( data, "xml.reference", false ) );

        return metadata;
    }

    // ----------------------------------------------------------------------
    // Metadata to Map
    // ----------------------------------------------------------------------

    public Map getFieldMap( ModelField field, FieldMetadata metadata )
    {
        return Collections.EMPTY_MAP;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String getTagName( Map data )
    {
        return getString( data, "xml.tagName" );
    }
}
