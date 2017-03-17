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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.BaseElement;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.VersionRange;
import org.codehaus.modello.plugin.xdoc.metadata.XdocFieldMetadata;
import org.codehaus.modello.plugin.xsd.XsdModelHelper;
import org.codehaus.modello.plugins.xml.AbstractXmlGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlModelMetadata;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:emmanuel@venisse.net">Emmanuel Venisse</a>
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

        initHeader( w );

        w.startElement( "document" );

        w.startElement( "properties" );

        writeTextElement( w, "title", objectModel.getName() );

        w.endElement();

        // Body

        w.startElement( "body" );

        w.startElement( "section" );

        w.addAttribute( "name", objectModel.getName() );

        writeMarkupElement( w, "p", getDescription( objectModel ) );

        // XML representation of the model with links
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        writeMarkupElement( w, "source", "\n" + getModelXmlDescriptor( root ) );

        // Element descriptors
        // Traverse from root so "abstract" models aren't included
        writeModelDescriptor( w, root );

        w.endElement();

        w.endElement();

        w.endElement();

        writer.flush();

        writer.close();
    }

    /**
     * Get the anchor name by which model classes can be accessed in the generated xdoc/html file.
     *
     * @param tagName the name of the XML tag of the model class
     * @return the corresponding anchor name
     */
    private String getAnchorName( String tagName )
    {
        return "class_" + tagName ;
    }

    /**
     * Write description of the whole model.
     *
     * @param w the output writer
     * @param rootModelClass the root class of the model
     */
    private void writeModelDescriptor( XMLWriter w, ModelClass rootModelClass )
    {
        writeElementDescriptor( w, rootModelClass, null, new HashSet<String>() );
    }

    /**
     * Write description of an element of the XML representation of the model. This method is recursive.
     *
     * @param w the output writer
     * @param modelClass the mode class to describe
     * @param association the association we are coming from (can be <code>null</code>)
     * @param written set of data already written
     */
    private void writeElementDescriptor( XMLWriter w, ModelClass modelClass, ModelAssociation association,
                                         Set<String> written )
    {
        String tagName = resolveTagName( modelClass, association );

        String id = getId( tagName, modelClass );
        if ( written.contains( id ) )
        {
            // tag already written for this model class accessed as this tag name
            return;
        }
        written.add( id );

        written.add( tagName );

        w.startElement( "a" );

        w.addAttribute( "name", getAnchorName( tagName ) );

        w.endElement();

        w.startElement( "subsection" );

        w.addAttribute( "name", tagName );

        writeMarkupElement( w, "p", getDescription( modelClass ) );

        List<ModelField> elementFields = getFieldsForXml( modelClass, getGeneratedVersion() );

        ModelField contentField = getContentField( elementFields );

        if ( contentField != null )
        {
            // this model class has a Content field
            w.startElement( "p" );

            writeTextElement( w, "b", "Element Content: " );

            w.writeMarkup( getDescription( contentField ) );

            w.endElement();
        }

        List<ModelField> attributeFields = getXmlAttributeFields( elementFields );

        elementFields.removeAll( attributeFields );

        writeFieldsTable( w, attributeFields, false ); // write attributes
        writeFieldsTable( w, elementFields, true ); // write elements

        w.endElement();

        // check every fields that are inner associations to write their element descriptor
        for ( ModelField f : elementFields )
        {
            if ( isInnerAssociation( f ) )
            {
                ModelAssociation assoc = (ModelAssociation) f;
                ModelClass fieldModelClass = getModel().getClass( assoc.getTo(), getGeneratedVersion() );

                if ( !written.contains( getId( resolveTagName( fieldModelClass, assoc ), fieldModelClass ) ) )
                {
                    writeElementDescriptor( w, fieldModelClass, assoc, written );
                }
            }
        }
    }

    private String getId( String tagName, ModelClass modelClass )
    {
        return tagName + '/' + modelClass.getPackageName() + '.' + modelClass.getName();
    }

    /**
     * Write a table containing model fields description.
     *
     * @param w the output writer
     * @param fields the fields to add in the table
     * @param elementFields <code>true</code> if fields are elements, <code>false</code> if fields are attributes
     */
    private void writeFieldsTable( XMLWriter w, List<ModelField> fields, boolean elementFields )
    {
        if ( fields == null || fields.isEmpty() )
        {
            // skip empty table
            return;
        }

        // skip if only one element field with xml.content == true
        if ( elementFields && ( fields.size() == 1 ) && hasContentField( fields ) )
        {
            return;
        }

        w.startElement( "table" );

        w.startElement( "tr" );

        writeTextElement( w, "th", elementFields ? "Element" : "Attribute" );

        writeTextElement( w, "th", "Type" );

        boolean showSinceColumn = version.greaterThan( firstVersion );

        if ( showSinceColumn )
        {
            writeTextElement( w, "th", "Since" );
        }

        writeTextElement( w, "th", "Description" );

        w.endElement(); // tr

        for ( ModelField f : fields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) f.getMetadata( XmlFieldMetadata.ID );

            if ( xmlFieldMetadata.isContent() )
            {
                continue;
            }

            w.startElement( "tr" );

            // Element/Attribute column

            String tagName = resolveTagName( f, xmlFieldMetadata );

            w.startElement( "td" );

            w.startElement( "code" );

            boolean manyAssociation = false;

            if ( f instanceof ModelAssociation )
            {
                ModelAssociation assoc = (ModelAssociation) f;

                XmlAssociationMetadata xmlAssociationMetadata =
                    (XmlAssociationMetadata) assoc.getAssociationMetadata( XmlAssociationMetadata.ID );

                manyAssociation = assoc.isManyMultiplicity();

                String itemTagName = manyAssociation ? resolveTagName( tagName, xmlAssociationMetadata ) : tagName;

                if ( manyAssociation && xmlAssociationMetadata.isWrappedItems() )
                {
                    w.writeText( tagName );
                    w.writeMarkup( "/" );
                }
                if ( isInnerAssociation( f ) )
                {
                    w.startElement( "a" );
                    w.addAttribute( "href", "#" + getAnchorName( itemTagName ) );
                    w.writeText( itemTagName );
                    w.endElement();
                }
                else if ( ModelDefault.PROPERTIES.equals( f.getType() ) )
                {
                    if ( xmlAssociationMetadata.isMapExplode() )
                    {
                        w.writeText( "(key,value)" );
                    }
                    else
                    {
                        w.writeMarkup( "<i>key</i>=<i>value</i>" );
                    }
                }
                else
                {
                    w.writeText( itemTagName );
                }
                if ( manyAssociation )
                {
                    w.writeText( "*" );
                }
            }
            else
            {
                w.writeText( tagName );
            }

            w.endElement(); // code

            w.endElement(); // td

            // Type column

            w.startElement( "td" );

            w.startElement( "code" );

            if ( f instanceof ModelAssociation )
            {
                ModelAssociation assoc = (ModelAssociation) f;

                if ( assoc.isOneMultiplicity() )
                {
                    w.writeText( assoc.getTo() );
                }
                else
                {
                    w.writeText( assoc.getType().substring( "java.util.".length() ) );

                    if ( assoc.isGenericType() )
                    {
                        w.writeText( "<" + assoc.getTo() + ">" );
                    }
                }
            }
            else
            {
                w.writeText( f.getType() );
            }

            w.endElement(); // code

            w.endElement(); // td

            // Since column

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

            // Description column

            w.startElement( "td" );

            if ( manyAssociation )
            {
                w.writeMarkup( "<b>(Many)</b> " );
            }

            w.writeMarkup( getDescription( f ) );

            // Write the default value, if it exists.
            // But only for fields that are not a ModelAssociation
            if ( f.getDefaultValue() != null && !( f instanceof ModelAssociation ) )
            {
                w.writeMarkup( "<br/><strong>Default value is</strong>: " );

                writeTextElement( w, "code", f.getDefaultValue() );

                w.writeText( "." );
            }

            w.endElement(); // td

            w.endElement(); // tr
        }

        w.endElement(); // table

    }

    /**
     * Build the pretty tree describing the XML representation of the model.
     *
     * @param rootModelClass the model root class
     * @return the String representing the tree model
     */
    private String getModelXmlDescriptor( ModelClass rootModelClass )
    {
        return getElementXmlDescriptor( rootModelClass, null, new Stack<String>() );
    }

    /**
     * Build the pretty tree describing the XML representation of an element of the model. This method is recursive.
     *
     * @param modelClass the class we are printing the model
     * @param association the association we are coming from (can be <code>null</code>)
     * @param stack the stack of elements that have been traversed to come to the current one
     * @return the String representing the tree model
     * @throws ModelloRuntimeException
     */
    private String getElementXmlDescriptor( ModelClass modelClass, ModelAssociation association, Stack<String> stack )
        throws ModelloRuntimeException
    {
        StringBuffer sb = new StringBuffer();

        appendSpacer( sb, stack.size() );

        String tagName = resolveTagName( modelClass, association );

        // <tagName
        sb.append( "&lt;<a href=\"#" ).append( getAnchorName( tagName ) ).append( "\">" );
        sb.append( tagName ).append( "</a>" );

        boolean addNewline = false;
        if ( stack.size() == 0 )
        {
            // try to add XML Schema reference
            try
            {
                String targetNamespace = XsdModelHelper.getTargetNamespace( modelClass.getModel(), getGeneratedVersion() );

                XmlModelMetadata xmlModelMetadata = (XmlModelMetadata) modelClass.getModel().getMetadata( XmlModelMetadata.ID );

                if ( StringUtils.isNotBlank( targetNamespace ) && ( xmlModelMetadata.getSchemaLocation() != null ) )
                {
                    String schemaLocation = xmlModelMetadata.getSchemaLocation( getGeneratedVersion() );

                    sb.append( " xmlns=\"" + targetNamespace + "\"" );
                    sb.append( " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" );
                    sb.append( "  xsi:schemaLocation=\"" + targetNamespace );
                    sb.append( " <a href=\"" + schemaLocation + "\">" + schemaLocation + "</a>\"" );

                    addNewline = true;
                }
            }
            catch ( ModelloException me )
            {
                // ignore unavailable XML Schema configuration
            }
        }

        String id = tagName + '/' + modelClass.getPackageName() + '.' + modelClass.getName();
        if ( stack.contains( id ) )
        {
            // recursion detected
            sb.append( "&gt;...recursion...&lt;" ).append( tagName ).append( "&gt;\n" );
            return sb.toString();
        }

        List<ModelField> fields = getFieldsForXml( modelClass, getGeneratedVersion() );

        List<ModelField> attributeFields = getXmlAttributeFields( fields );

        if ( attributeFields.size() > 0 )
        {

            for ( ModelField f : attributeFields )
            {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) f.getMetadata( XmlFieldMetadata.ID );

                if ( addNewline )
                {
                    addNewline = false;

                    sb.append( "\n  " );
                }
                else
                {
                    sb.append( ' ' );
                }

                sb.append( resolveTagName( f, xmlFieldMetadata ) ).append( "=.." );
            }

            sb.append( ' ' );

        }

        fields.removeAll( attributeFields );

        if ( ( fields.size() == 0 ) || ( ( fields.size() == 1 ) && hasContentField( fields ) ) )
        {
            sb.append( "/&gt;\n" );
        }
        else
        {
            sb.append( "&gt;\n" );

            stack.push( id );

            for ( ModelField f : fields )
            {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) f.getMetadata( XmlFieldMetadata.ID );

                XdocFieldMetadata xdocFieldMetadata = (XdocFieldMetadata) f.getMetadata( XdocFieldMetadata.ID );

                if ( XdocFieldMetadata.BLANK.equals( xdocFieldMetadata.getSeparator() ) )
                {
                    sb.append( '\n' );
                }

                String fieldTagName = resolveTagName( f, xmlFieldMetadata );

                if ( isInnerAssociation( f ) )
                {
                    ModelAssociation assoc = (ModelAssociation) f;

                    boolean wrappedItems = false;
                    if ( assoc.isManyMultiplicity() )
                    {
                        XmlAssociationMetadata xmlAssociationMetadata =
                            (XmlAssociationMetadata) assoc.getAssociationMetadata( XmlAssociationMetadata.ID );
                        wrappedItems = xmlAssociationMetadata.isWrappedItems();
                    }

                    if ( wrappedItems )
                    {
                        appendSpacer( sb, stack.size() );

                        sb.append( "&lt;" ).append( fieldTagName ).append( "&gt;\n" );

                        stack.push( fieldTagName );
                    }

                    ModelClass fieldModelClass = getModel().getClass( assoc.getTo(), getGeneratedVersion() );

                    sb.append( getElementXmlDescriptor( fieldModelClass, assoc, stack ) );

                    if ( wrappedItems )
                    {
                        stack.pop();

                        appendSpacer( sb, stack.size() );

                        sb.append( "&lt;/" ).append( fieldTagName ).append( "&gt;\n" );
                    }
                }
                else if ( ModelDefault.PROPERTIES.equals( f.getType() ) )
                {
                    ModelAssociation assoc = (ModelAssociation) f;
                    XmlAssociationMetadata xmlAssociationMetadata =
                        (XmlAssociationMetadata) assoc.getAssociationMetadata( XmlAssociationMetadata.ID );

                    appendSpacer( sb, stack.size() );
                    sb.append( "&lt;" ).append( fieldTagName ).append( "&gt;\n" );

                    if ( xmlAssociationMetadata.isMapExplode() )
                    {
                        appendSpacer( sb, stack.size() + 1 );
                        sb.append( "&lt;key/&gt;\n" );
                        appendSpacer( sb, stack.size() + 1 );
                        sb.append( "&lt;value/&gt;\n" );
                    }
                    else
                    {
                        appendSpacer( sb, stack.size() + 1 );
                        sb.append( "&lt;<i>key</i>&gt;<i>value</i>&lt;/<i>key</i>&gt;\n" );
                    }

                    appendSpacer( sb, stack.size() );
                    sb.append( "&lt;/" ).append( fieldTagName ).append( "&gt;\n" );
                }
                else
                {
                    appendSpacer( sb, stack.size() );

                    sb.append( "&lt;" ).append( fieldTagName ).append( "/&gt;\n" );
                }
            }

            stack.pop();

            appendSpacer( sb, stack.size() );

            sb.append( "&lt;/" ).append( tagName ).append( "&gt;\n" );
        }

        return sb.toString();
    }

    /**
     * Compute the tagName of a given class, living inside an association.
     * @param modelClass the class we are looking for the tag name
     * @param association the association where this class is used
     * @return the tag name to use
     * @todo refactor to use resolveTagName helpers instead
     */
    private String resolveTagName( ModelClass modelClass, ModelAssociation association )
    {
        XmlClassMetadata xmlClassMetadata = (XmlClassMetadata) modelClass.getMetadata( XmlClassMetadata.ID );

        String tagName;
        if ( xmlClassMetadata == null || xmlClassMetadata.getTagName() == null )
        {
            if ( association == null )
            {
                tagName = uncapitalise( modelClass.getName() );
            }
            else
            {
                tagName = association.getName();

                if ( association.isManyMultiplicity() )
                {
                    tagName = singular( tagName );
                }
            }
        }
        else
        {
            tagName = xmlClassMetadata.getTagName();
        }

        if ( association != null )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) association.getMetadata( XmlFieldMetadata.ID );

            XmlAssociationMetadata xmlAssociationMetadata =
                (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

            if ( xmlFieldMetadata != null )
            {
                if ( xmlAssociationMetadata.getTagName() != null )
                {
                    tagName = xmlAssociationMetadata.getTagName();
                }
                else if ( xmlFieldMetadata.getTagName() != null )
                {
                    tagName = xmlFieldMetadata.getTagName();

                    if ( association.isManyMultiplicity() )
                    {
                        tagName = singular( tagName );
                    }
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

    private static String getDescription( BaseElement element )
    {
        return ( element.getDescription() == null ) ? "No description." : rewrite( element.getDescription() );
    }

    private static void writeTextElement( XMLWriter w, String name, String text )
    {
        w.startElement( name );
        w.writeText( text );
        w.endElement();
    }

    private static void writeMarkupElement( XMLWriter w, String name, String markup )
    {
        w.startElement( name );
        w.writeMarkup( markup );
        w.endElement();
    }
    
    /**
     * Ensures that text will have balanced tags
     * 
     * @param text xml or html based content
     * @return valid XML string
     */
    private static String rewrite( String text )
    {
        Document document = Jsoup.parseBodyFragment( text );
        document.outputSettings().syntax( Document.OutputSettings.Syntax.xml );
        return document.body().html();
    }
}
