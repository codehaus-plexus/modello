package org.codehaus.modello.plugin.stax;

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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugins.xml.AbstractXmlJavaGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractStaxGenerator
    extends AbstractXmlJavaGenerator
{
    private Set/*<ModelClass>*/ parts;

    protected void initialize( Model model, Properties parameters )
        throws ModelloException
    {
        super.initialize( model, parameters );

        parts = null;
    }

    protected ModelField getReferenceIdentifierField( ModelAssociation association )
        throws ModelloException
    {
        XmlAssociationMetadata xmlAssocMetadata =
            (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

        ModelField referenceIdentifierField = null;
        if ( xmlAssocMetadata.isReference() )
        {
            String associationName = association.getName();

            ModelClass modelClass = association.getModelClass();
            if ( !isClassInModel( association.getTo(), modelClass.getModel() ) )
            {
                throw new ModelloException( "Can't use xml.reference on the '" + associationName +
                    "' association of '" + modelClass.getName() + "' because the target class '" + association.getTo() +
                    "' is not in the model" );
            }

            List identifierFields = association.getToClass().getIdentifierFields( getGeneratedVersion() );
            if ( identifierFields.size() == 1 )
            {
                referenceIdentifierField = (ModelField) identifierFields.get( 0 );
            }
            else
            {
                referenceIdentifierField = new DummyIdModelField();
                referenceIdentifierField.setName( "modello.refid" );
            }
        }
        return referenceIdentifierField;
    }

    protected boolean isAssociationPartToClass( ModelClass modelClass )
    {
        if ( parts == null )
        {
            parts = new HashSet();
            for ( Iterator i = modelClass.getModel().getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
            {
                ModelClass clazz = (ModelClass) i.next();

                for ( Iterator j = clazz.getFields( getGeneratedVersion() ).iterator(); j.hasNext(); )
                {
                    ModelField modelField = (ModelField) j.next();

                    if ( modelField instanceof ModelAssociation )
                    {
                        ModelAssociation assoc = (ModelAssociation) modelField;

                        XmlAssociationMetadata xmlAssocMetadata =
                            (XmlAssociationMetadata) assoc.getAssociationMetadata( XmlAssociationMetadata.ID );

                        if ( xmlAssocMetadata.isReference() )
                        {
                            parts.add( assoc.getToClass() );
                        }
                    }
                }
            }
        }
        return parts.contains( modelClass );
    }

    protected static String getValue( String type, String initialValue, XmlFieldMetadata xmlFieldMetadata )
    {
        String textValue = initialValue;

        if ( "Date".equals( type ) )
        {
            if ( xmlFieldMetadata.getFormat() == null )
            {
                textValue = "Long.toString( " + textValue + ".getTime() )";
            }
            else
            {
                textValue = "new java.text.SimpleDateFormat( \"" + xmlFieldMetadata.getFormat() +
                    "\", Locale.US ).format( " + textValue + " )";
            }
        }
        else if ( !"String".equals( type ) )
        {
            textValue = "String.valueOf( " + textValue + " )";
        }

        return textValue;

    }
}
