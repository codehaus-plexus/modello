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
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugin.java.AbstractJavaModelloGenerator;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;

/**
 * Abstract class for plugins generating Java code for XML representation of the model.
 *
 * @author <a href="mailto:hboutemy@codehaus.org">Hervé Boutemy</a>
 */
public abstract class AbstractXmlJavaGenerator
    extends AbstractJavaModelloGenerator
{
    protected boolean strictXmlAttributes;

    protected void initialize( Model model, Properties parameters )
        throws ModelloException
    {
        super.initialize( model, parameters );

        strictXmlAttributes = model.getDefault( ModelDefault.STRICT_XML_ATTRIBUTES ).getBoolean();
    }

    protected String getFileName( String suffix )
    {
        String name = getModel().getName();

        return name + suffix;
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

    protected String getValue( String type, String initialValue, XmlFieldMetadata xmlFieldMetadata )
    {
        String textValue = initialValue;

        if ( "Date".equals( type ) )
        {
            String dateFormat = xmlFieldMetadata.getFormat();
            if ( xmlFieldMetadata.getFormat() == null )
            {
                dateFormat = DEFAULT_DATE_FORMAT;
            }

            if ( "long".equals( dateFormat ) )
            {
                textValue = "String.valueOf( " + textValue + ".getTime() )";
            }
            else
            {
                textValue =
                    "new java.text.SimpleDateFormat( \"" + dateFormat + "\", java.util.Locale.US ).format( " + textValue + " )";
            }
        }
        else if ( !"String".equals( type ) )
        {
            textValue = "String.valueOf( " + textValue + " )";
        }

        return textValue;
    }

    protected void writeDateParsingHelper( JSourceCode sc, String exception )
    {
        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "String effectiveDateFormat = dateFormat;" );

        sc.add( "if ( dateFormat == null )" );

        sc.add( "{" );
        sc.addIndented( "effectiveDateFormat = \"" + DEFAULT_DATE_FORMAT + "\";" );
        sc.add( "}" );

        sc.add( "if ( \"long\".equals( effectiveDateFormat ) )" );

        // parse date as a long
        sc.add( "{" );
        sc.indent();

        sc.add( "try" );

        sc.add( "{" );
        sc.addIndented( "return new java.util.Date( Long.parseLong( s ) );" );
        sc.add( "}" );

        sc.add( "catch ( NumberFormatException e )" );

        sc.add( "{" );
        sc.addIndented( "throw " + exception + ";" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "else" );

        // parse date as a SimpleDateFormat expression
        sc.add( "{" );
        sc.indent();

        sc.add( "try" );
        sc.add( "{" );
        sc.indent();

        sc.add( "DateFormat dateParser = new java.text.SimpleDateFormat( effectiveDateFormat, java.util.Locale.US );" );
        sc.add( "return dateParser.parse( s );" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "catch ( java.text.ParseException e )" );
        sc.add( "{" );
        sc.addIndented( "throw " + exception + ";" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return null;" );
    }
}
