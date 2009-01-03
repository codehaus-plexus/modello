package org.codehaus.modello.plugin.xdoc;

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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.VersionRange;
import org.codehaus.modello.plugin.xdoc.metadata.XdocFieldMetadata;
import org.codehaus.modello.plugins.xml.AbstractXmlGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:emmanuel@venisse.net">Emmanuel Venisse</a>
 * @version $Id$
 */
public class XdocGenerator
    extends AbstractXmlGenerator
{
    private static final VersionRange DEFAULT_VERSION_RANGE = new VersionRange( "0.0.0+" );

    private Version firstVersion = DEFAULT_VERSION_RANGE.getFromVersion();

    private Version version = DEFAULT_VERSION_RANGE.getFromVersion();

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        if ( parameters.getProperty( ModelloParameterConstants.FIRST_VERSION ) != null )
        {
            firstVersion = new Version( parameters.getProperty( ModelloParameterConstants.FIRST_VERSION ) );
        }

        if ( parameters.getProperty( ModelloParameterConstants.VERSION ) != null )
        {
            version = new Version( parameters.getProperty( ModelloParameterConstants.VERSION ) );
        }

        try
        {
            generateXdoc( parameters );
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating XDoc.", ex );
        }
    }

    private void generateXdoc( Properties parameters )
        throws IOException
    {
        Model objectModel = getModel();

        File directory = getOutputDirectory();

        if ( isPackageWithVersion() )
        {
            directory = new File( directory, getGeneratedVersion().toString() );
        }

        if ( !directory.exists() )
        {
            directory.mkdirs();
        }

        // we assume parameters not null
        String xdocFileName = parameters.getProperty( ModelloParameterConstants.OUTPUT_XDOC_FILE_NAME );

        File f = new File( directory, objectModel.getId() + ".xml" );

        if ( xdocFileName != null )
        {
            f = new File( directory, xdocFileName );
        }

        Writer writer = WriterFactory.newXmlWriter( f );

        XMLWriter w = new PrettyPrintXMLWriter( writer );

        writer.write( "<?xml version=\"1.0\"?>\n" );

        w.startElement( "document" );

        w.startElement( "properties" );

        w.startElement( "title" );

        w.writeText( objectModel.getName() );

        w.endElement();

        w.endElement();

        // Body

        w.startElement( "body" );

        // Descriptor with links

        w.startElement( "section" );

        w.addAttribute( "name", objectModel.getName() );

        w.startElement( "p" );

        if ( objectModel.getDescription() != null )
        {
            w.writeMarkup( objectModel.getDescription() );
        }
        else
        {
            w.writeText( "No description." );
        }

        w.endElement();

        w.startElement( "source" );

        StringBuffer sb = new StringBuffer();

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );
        sb.append( getModelClassDescriptor( root, null, 0 ) );

        w.writeMarkup( "\n" + sb );

        w.endElement();

        // Element descriptors
        // Traverse from root so "abstract" models aren't included
        writeElementDescriptor( w, root, null, new HashSet() );

        w.endElement();

        w.endElement();

        w.endElement();

        writer.flush();

        writer.close();
    }

    private void writeElementDescriptor( XMLWriter w, ModelClass modelClass, ModelAssociation association,
                                         Set written )
    {
        writeElementDescriptor( w, modelClass, association, written, true );
    }

    private void writeElementDescriptor( XMLWriter w, ModelClass modelClass, ModelAssociation association,
                                         Set written, boolean recursive )
    {
        written.add( modelClass );

        String tagName = resolveTagName( modelClass, association );

        w.startElement( "a" );

        w.addAttribute( "name", "class_" + tagName );

        w.endElement();

        w.startElement( "subsection" );

        w.addAttribute( "name", tagName );

        w.startElement( "p" );

        if ( modelClass.getDescription() != null )
        {
            w.writeMarkup( modelClass.getDescription() );
        }
        else
        {
            w.writeMarkup( "No description." );
        }

        w.endElement();

        ModelField contentField = getContentField( getFieldsForClass( modelClass ) );

        if (contentField != null)
        {
            w.startElement( "p" );
            w.startElement( "b" );
            w.writeText( "Element Content: " );
            w.writeMarkup( contentField.getDescription() );
            w.endElement();
            w.endElement();
        }
        List attributeFields = new ArrayList( getAttributeFieldsForClass( modelClass ) );
        List elementFields = new ArrayList( getFieldsForClass( modelClass ) );
        elementFields.removeAll( attributeFields );
        generateFieldsTable( w, elementFields, true );
        generateFieldsTable( w, attributeFields, false );

        w.endElement();

        for ( Iterator iter = getFieldsForClass( modelClass ).iterator(); iter.hasNext(); )
        {
            ModelField f = (ModelField) iter.next();

            if ( isInnerAssociation( f ) && recursive )
            {
                ModelAssociation assoc = (ModelAssociation) f;
                ModelClass fieldModelClass = getModel().getClass( assoc.getTo(), getGeneratedVersion() );

                if ( !written.contains( f.getName() ) )
                {
                    if ( ( modelClass.getName().equals( fieldModelClass.getName() ) )
                        && ( modelClass.getPackageName().equals( fieldModelClass.getPackageName() ) ) )
                    {
                        writeElementDescriptor( w, fieldModelClass, assoc, written, false );
                    }
                    else
                    {
                        writeElementDescriptor( w, fieldModelClass, assoc, written );
                    }
                }
            }
        }
    }

    private void generateFieldsTable( XMLWriter w, List fields, boolean elementFields )
    {

        if ( fields == null || fields.isEmpty() )
        {
            // skip empty table
            return;
        }

        // skip if only one field and Content type
        if ( fields.size() == 1 )
        {
            if ( "Content".equals( (( ModelField ) fields.get( 0 )).getType() ) )
            {
                return;
            }
        }

        w.startElement( "table" );

        w.startElement( "tr" );

        w.startElement( "th" );

        w.writeText( elementFields ? "Element" : "Attribute" );

        w.endElement();

        w.startElement( "th" );

        w.writeText( "Description" );

        w.endElement();

        boolean showSinceColumn = version.greaterThan( firstVersion );

        if ( showSinceColumn )
        {
            w.startElement( "th" );

            w.writeText( "Since" );

            w.endElement();
        }

        w.endElement();

        for ( Iterator j = fields.iterator(); j.hasNext(); )
        {
            ModelField f = (ModelField) j.next();

            if ( "Content".equals( f.getType() ) )
            {
                continue;
            }

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) f.getMetadata( XmlFieldMetadata.ID );

            w.startElement( "tr" );

            w.startElement( "td" );

            w.startElement( "code" );

            boolean flatAssociation = false;

            if ( isInnerAssociation( f ) )
            {
                flatAssociation = XmlFieldMetadata.LIST_STYLE_FLAT.equals( fieldMetadata.getListStyle() );

                ModelAssociation assoc = (ModelAssociation) f;

                ModelClass associationModelClass = getModel().getClass( assoc.getTo(), getGeneratedVersion() );

                w.startElement( "a" );


                if ( fieldMetadata.getTagName() != null )
                {
                    w.addAttribute( "href", "#class_" + fieldMetadata.getTagName() );
                }
                else
                {
                    w.addAttribute( "href", "#class_" + uncapitalise( associationModelClass.getName() ) );
                }
                if ( flatAssociation )
                {
                    if ( fieldMetadata.getTagName() != null )
                    {
                        w.writeText( uncapitalise( fieldMetadata.getTagName() ) );
                    }
                    else
                    {
                        w.writeText( uncapitalise( associationModelClass.getName() ) );
                    }
                }
                else
                {
                    w.writeText( f.getName() );
                }

                w.endElement();
            }
            else
            {
                w.writeText( resolveFieldTagName( f ) );
            }

            w.endElement();

            w.endElement();

            // Description

            w.startElement( "td" );

            if ( flatAssociation )
            {
                w.writeMarkup( "<b>List</b> " );
            }

            if ( f.getDescription() != null )
            {
                w.writeMarkup( f.getDescription() );
            }
            else
            {
                w.writeText( "No description." );
            }

            // Write the default value, if it exists.
            // But only for fields that are not a ModelAssociation
            if ( f.getDefaultValue() != null && !( f instanceof ModelAssociation ) )
            {
                w.writeText( " The default value is " );
                w.startElement( "code" );
                w.writeText( f.getDefaultValue() );
                w.endElement();
                w.writeText( "." );
            }

            w.endElement();

            // Since

            if ( showSinceColumn )
            {
                w.startElement( "td" );

                if ( f.getVersionRange() != null )
                {
                    Version fromVersion = f.getVersionRange().getFromVersion();
                    if ( fromVersion != null && fromVersion.greaterThan( firstVersion ) )
                    {
                        w.writeMarkup( fromVersion.toString() );
                    }
                }

                w.endElement();
            }

            w.endElement();
        }

        w.endElement();

    }

    private String getModelClassDescriptor( ModelClass modelClass, ModelAssociation association, int depth )
    {
        return getModelClassDescriptor( modelClass, association, depth, true );
    }

    /**
     * Build the pretty tree describing the model. This method is recursive.
     * @param modelClass the class we are printing the model
     * @param association the association we are coming from (can be <code>null</code>)
     * @param depth how deep we currently are (for spacers purpose)
     * @param recursive are we still in recursive mode or not
     * @return the String representing the tree model
     * @throws ModelloRuntimeException
     */
    private String getModelClassDescriptor( ModelClass modelClass, ModelAssociation association, int depth, boolean recursive )
        throws ModelloRuntimeException
    {
        StringBuffer sb = new StringBuffer();

        appendSpacer( sb, depth );

        String tagName = resolveTagName( modelClass, association );
        sb.append( "&lt;<a href=\"#class_" ).append( tagName ).append( "\">" ).append( tagName ).append( "</a>" );

        List fields = getFieldsForClass( modelClass );

        List attributeFields = getAttributeFieldsForClass( modelClass );

        if ( attributeFields.size() > 0 )
        {

            for ( Iterator iter = attributeFields.iterator(); iter.hasNext(); )
            {
                ModelField f = (ModelField) iter.next();

                sb.append( ' ' );

                sb.append( resolveFieldTagName( f ) ).append( "=.." );
            }

            sb.append( ' ' );

            fields.removeAll( attributeFields );

        }

        if ( fields.size() > 0 )
        {
            sb.append( "&gt;\n" );

            for ( Iterator iter = fields.iterator(); iter.hasNext(); )
            {
                ModelField f = (ModelField) iter.next();

                XdocFieldMetadata xdocFieldMetadata = (XdocFieldMetadata) f.getMetadata( XdocFieldMetadata.ID );

                if ( XdocFieldMetadata.BLANK.equals( xdocFieldMetadata.getSeparator() ) )
                {
                    sb.append( '\n' );
                }

                if ( isInnerAssociation( f ) && recursive )
                {
                    ModelAssociation assoc = (ModelAssociation) f;

                    XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) f.getMetadata( XmlFieldMetadata.ID );

                    boolean listStyleWrapped =
                        ModelAssociation.MANY_MULTIPLICITY.equals( assoc.getMultiplicity() )
                        && XmlFieldMetadata.LIST_STYLE_WRAPPED.equals( fieldMetadata.getListStyle() );

                    if ( listStyleWrapped )
                    {
                        depth++;

                        appendSpacer( sb, depth );

                        sb.append( "&lt;" ).append( uncapitalise( assoc.getName() ) ).append( "&gt;\n" );
                    }

                    ModelClass fieldModelClass = getModel().getClass( assoc.getTo(), getGeneratedVersion() );

                    if ( ( modelClass.getName().equals( fieldModelClass.getName() ) )
                        && ( modelClass.getPackageName().equals( fieldModelClass.getPackageName() ) ) )
                    {
                        sb.append( getModelClassDescriptor( fieldModelClass, assoc, depth + 1, false ) );
                    }
                    else
                    {
                        sb.append( getModelClassDescriptor( fieldModelClass, assoc, depth + 1 ) );
                    }

                    if ( listStyleWrapped )
                    {
                        appendSpacer( sb, depth );

                        sb.append( "&lt;/" ).append( uncapitalise( assoc.getName() ) ).append( "&gt;\n" );

                        depth--;
                    }
                }
                else
                {
                    appendSpacer( sb, depth + 1 );

                    sb.append( "&lt;" ).append( resolveFieldTagName( f ) ).append( "/&gt;\n" );
                }
            }

            appendSpacer( sb, depth );

            sb.append( "&lt;/" ).append( tagName ).append( "&gt;\n" );
        }
        else
        {
            sb.append( "/&gt;\n" );
        }

        return sb.toString();
    }

    /**
     * Compute the tagName of a given class, living inside an association.
     * @param modelClass the class we are looking for the tag name
     * @param association the association where this class is used
     * @return the tag name to use
     */
    private String resolveTagName( ModelClass modelClass, ModelAssociation association )
    {
        XmlClassMetadata metadata = (XmlClassMetadata) modelClass.getMetadata( XmlClassMetadata.ID );

        String tagName;
        if ( metadata == null || metadata.getTagName() == null )
        {
            if ( association == null )
            {
                tagName = uncapitalise( modelClass.getName() );
            }
            else
            {
                tagName = association.getName();

                if ( ModelAssociation.MANY_MULTIPLICITY.equals( association.getMultiplicity() ) )
                {
                    tagName = singular( tagName );
                }
            }
        }
        else
        {
            tagName = metadata.getTagName();
        }

        if ( association != null )
        {
            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) association.getMetadata( XmlFieldMetadata.ID );
            if ( fieldMetadata != null )
            {
                if ( fieldMetadata.getAssociationTagName() != null )
                {
                    tagName = fieldMetadata.getAssociationTagName();
                }
                else if ( fieldMetadata.getTagName() != null )
                {
                    tagName = fieldMetadata.getTagName();
                }
            }
        }

        return tagName;
    }

    /**
     * Appends the required spacers to the given StringBuffer.
     * @param sb where to append the spacers
     * @param depth the depth of spacers to generate
     */
    private static void appendSpacer( StringBuffer sb, int depth )
    {
        for ( int i = 0; i < depth; i++ )
        {
            sb.append( "  " );
        }
    }
}
