package org.codehaus.modello.plugins.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;

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

/**
 * Helper methods to deal with XML representation of the model.
 *
 * @author <a href="mailto:hboutemy@codehaus.org">Hervé Boutemy</a>
 */
class XmlModelHelpers
{
    /**
     * Resolve XML tag name for a class. Note: only root class needs such a resolution.
     *
     * @param modelClass the model class
     * @return the XML tag name for the class
     */
    static String resolveTagName( ModelClass modelClass )
    {
        XmlClassMetadata xmlClassMetadata = (XmlClassMetadata) modelClass.getMetadata( XmlClassMetadata.ID );

        String tagName;
        if ( ( xmlClassMetadata == null ) || ( xmlClassMetadata.getTagName() == null ) )
        {
            tagName = AbstractModelloGenerator.uncapitalise( modelClass.getName() );
        }
        else
        {
            // tag name is overridden by xml.tagName attribute
            tagName = xmlClassMetadata.getTagName();
        }
        return tagName;
    }

    /**
     * Resolve XML tag name for a field.
     *
     * @param modelField the model field
     * @param xmlFieldMetadata the XML metadata of the field
     * @return the XML tag name for the field
     */
    static String resolveTagName( ModelField modelField, XmlFieldMetadata xmlFieldMetadata )
    {
        String tagName;
        if ( ( xmlFieldMetadata == null ) || ( xmlFieldMetadata.getTagName() == null ) )
        {
            tagName = modelField.getName();
        }
        else
        {
            // tag name is overridden by xml.tagName attribute
            tagName = xmlFieldMetadata.getTagName();
        }
        return tagName;
    }

    /**
     * Resolve XML tag name for an item in an association with many multiplicity.
     *
     * @param fieldTagName the XML tag name of the field containing the association
     * @param xmlAssociationMetadata the XML metadata of the association
     * @return the XML tag name for items
     */
    static String resolveTagName( String fieldTagName, XmlAssociationMetadata xmlAssociationMetadata )
    {
        String tagName;
        if ( ( xmlAssociationMetadata == null ) || ( xmlAssociationMetadata.getTagName() == null ) )
        {
            tagName = AbstractModelloGenerator.singular( fieldTagName );
        }
        else
        {
            // tag name is overridden by xml.tagName attribute
            tagName = xmlAssociationMetadata.getTagName();
        }
        return tagName;
    }

    /**
     * Get the field which type is <code>Content</code> if any.
     *
     * @param modelFields the fields to check
     * @return the field, or <code>null</code> if no field is <code>Content</code>
     */
    static ModelField getContentField( List<ModelField> modelFields )
    {
        if ( modelFields == null )
        {
            return null;
        }
        for ( ModelField field : modelFields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( xmlFieldMetadata.isContent() )
            {
                return field;
            }
        }
        return null;
    }

    /**
     * Gets all fields that are not marked as XML attribute.
     *
     * @param modelFields The collection of model fields from which to extract the XML attributes, must not be
     *            <code>null</code>.
     * @return The list of XML attributes fields, can be empty but never <code>null</code>.
     */
    static List<ModelField> getXmlAttributeFields( List<ModelField> modelFields )
    {
        List<ModelField> xmlAttributeFields = new ArrayList<ModelField>();

        for ( ModelField field : modelFields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            if ( xmlFieldMetadata.isAttribute() )
            {
                xmlAttributeFields.add( field );
            }
        }

        return xmlAttributeFields;
    }

    /**
     * Return the XML fields of this class, with proper XML order and no XML transient fields.
     *
     * @param modelClass current class
     * @param version the version of the class to use
     * @return the list of XML fields of this class
     */
    static List<ModelField> getFieldsForXml( ModelClass modelClass, Version version )
    {
        List<ModelClass> classes = new ArrayList<ModelClass>();

        // get the full inheritance
        while ( modelClass != null )
        {
            classes.add( modelClass );

            String superClass = modelClass.getSuperClass();
            if ( superClass != null )
            {
                // superClass can be located outside (not generated by modello)
                modelClass = modelClass.getModel().getClass( superClass, version, true );
            }
            else
            {
                modelClass = null;
            }
        }

        List<ModelField> fields = new ArrayList<ModelField>();

        for ( int i = classes.size() - 1; i >= 0; i-- )
        {
            modelClass = classes.get( i );

            Iterator<ModelField> parentIter = fields.iterator();

            fields = new ArrayList<ModelField>();

            for ( ModelField field : modelClass.getFields( version ) )
            {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

                if ( xmlFieldMetadata.isTransient() )
                {
                    // just ignore xml.transient fields
                    continue;
                }

                if ( xmlFieldMetadata.getInsertParentFieldsUpTo() != null )
                {
                    // insert fields from parent up to the specified field
                    boolean found = false;

                    while ( !found && parentIter.hasNext() )
                    {
                        ModelField parentField = parentIter.next();

                        fields.add( parentField );

                        found = parentField.getName().equals( xmlFieldMetadata.getInsertParentFieldsUpTo() );
                    }

                    if ( !found )
                    {
                        // interParentFieldsUpTo not found
                        throw new ModelloRuntimeException( "parent field not found: class " + modelClass.getName() + " xml.insertParentFieldUpTo='" + xmlFieldMetadata.getInsertParentFieldsUpTo() + "'" );
                    }
                }

                fields.add( field );
            }

            // add every remaining fields from parent class
            while ( parentIter.hasNext() )
            {
                fields.add( parentIter.next() );
            }
        }

        return fields;
    }
}
