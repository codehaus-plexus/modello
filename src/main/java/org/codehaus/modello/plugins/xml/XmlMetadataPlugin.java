package org.codehaus.modello.plugins.xml;

/*
 * Copyright (c) 2004, Jason van Zyl
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

import java.util.Collections;
import java.util.Map;

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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 *
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
        return new XmlModelMetadata();
    }

    public ClassMetadata getClassMetadata( ModelClass clazz, Map data )
    {
        return new XmlClassMetadata();
    }

    public FieldMetadata getFieldMetadata( ModelField field, Map data )
    {
        XmlFieldMetadata metadata = new XmlFieldMetadata();

        String attribute = (String) data.get( "xml.attribute" );

        String tagName = (String) data.get( "xml.tagName" );

        metadata.setAttribute( Boolean.valueOf( attribute ).booleanValue() );

        metadata.setTagName( tagName );

        return metadata;
    }

    public AssociationMetadata getAssociationMetadata( ModelAssociation association, Map data )
    {
        XmlAssociationMetadata metadata = new XmlAssociationMetadata();

        String mapStyle = (String) data.get( "xml.mapStyle" );

        metadata.setMapStyle( mapStyle );

        return metadata;
    }

    // ----------------------------------------------------------------------
    // Metadata to Map
    // ----------------------------------------------------------------------

    public Map getFieldMap( ModelField field, FieldMetadata metadata )
    {
        return Collections.EMPTY_MAP;
    }
}
