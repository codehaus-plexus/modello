package org.codehaus.modello.plugins.xml;

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

import java.util.List;

import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.XmlWriterUtil;

/**
 * Abstract class for plugins working on XML representation of the model, without having any need to generate
 * Java code.
 *
 * @author <a href="mailto:hboutemy@codehaus.org">Hervé Boutemy</a>
 */
public abstract class AbstractXmlGenerator
    extends AbstractModelloGenerator
{
    protected void initHeader( XMLWriter w )
    {
        XmlWriterUtil.writeComment( w, getHeader() );
    }

    /**
     * Resolve XML tag name for a class. Note: only root class needs such a resolution.
     *
     * @param modelClass the model class
     * @return the XML tag name for the class
     */
    protected String resolveTagName( ModelClass modelClass )
    {
        return XmlModelHelpers.resolveTagName( modelClass );
    }

    /**
     * Resolve XML tag name for a field.
     *
     * @param modelField the model field
     * @param xmlFieldMetadata the XML metadata of the field
     * @return the XML tag name for the field
     */
    protected String resolveTagName( ModelField modelField, XmlFieldMetadata xmlFieldMetadata )
    {
        return XmlModelHelpers.resolveTagName( modelField, xmlFieldMetadata );
    }

    /**
     * Resolve XML tag name for an item in an association with many multiplicity.
     *
     * @param fieldTagName the XML tag name of the field containing the association
     * @param xmlAssociationMetadata the XML metadata of the association
     * @return the XML tag name for items
     */
    protected String resolveTagName( String fieldTagName, XmlAssociationMetadata xmlAssociationMetadata )
    {
        return XmlModelHelpers.resolveTagName( fieldTagName, xmlAssociationMetadata );
    }

    protected boolean hasContentField( List<ModelField> modelFields )
    {
        return ( getContentField( modelFields ) != null );
    }

    /**
     * Get the field which type is <code>Content</code> if any.
     *
     * @param modelFields the fields to check
     * @return the field, or <code>null</code> if no field is <code>Content</code>
     */
    protected ModelField getContentField( List<ModelField> modelFields )
    {
        return XmlModelHelpers.getContentField( modelFields );
    }

    /**
     * Gets all fields that are not marked as XML attribute.
     *
     * @param modelFields The collection of model fields from which to extract the XML attributes, must not be
     *            <code>null</code>.
     * @return The list of XML attributes fields, can be empty but never <code>null</code>.
     */
    protected List<ModelField> getXmlAttributeFields( List<ModelField> modelFields )
    {
        return XmlModelHelpers.getXmlAttributeFields( modelFields );
    }

    /**
     * Return the XML fields of this class, with proper XML order and no XML transient fields.
     *
     * @param modelClass current class
     * @param version the version of the class to use
     * @return the list of XML fields of this class
     */
    protected List<ModelField> getFieldsForXml( ModelClass modelClass, Version version )
    {
        return XmlModelHelpers.getFieldsForXml( modelClass, version );
    }
}
