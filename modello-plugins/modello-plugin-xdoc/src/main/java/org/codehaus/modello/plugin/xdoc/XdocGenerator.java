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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.VersionRange;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.modello.plugins.xml.XmlFieldMetadata;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:emmanuel@venisse.net">Emmanuel Venisse</a>
 * @version $Id$
 */
public class XdocGenerator
    extends AbstractModelloGenerator
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
            generateXdoc();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating XDoc.", ex );
        }
    }

    private void generateXdoc()
        throws IOException
    {
        Model objectModel = getModel();

        String directory = getOutputDirectory().getAbsolutePath();

        if ( isPackageWithVersion() )
        {
            directory += "/" + getGeneratedVersion();
        }

        File f = new File( directory, objectModel.getId() + ".xml" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
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
        sb.append( getModelClassDescriptor( objectModel, root, null, 0 ) );

        w.writeMarkup( "\n" + sb );

        w.endElement();

        // Element descriptors
        // Traverse from root so "abstract" models aren't included
        writeElementDescriptor( w, objectModel, root, null, new HashSet() );

        w.endElement();

        w.endElement();

        w.endElement();

        writer.flush();

        writer.close();
    }

    private void writeElementDescriptor( XMLWriter w, Model objectModel, ModelClass modelClass, ModelField field,
                                         Set written )
    {
        writeElementDescriptor( w, objectModel, modelClass, field, written, true );
    }

    private void writeElementDescriptor( XMLWriter w, Model objectModel, ModelClass modelClass, ModelField field,
                                         Set written, boolean recursive )
    {
        written.add( modelClass );

        ModelClassMetadata metadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

        String tagName;
        if ( metadata == null || metadata.getTagName() == null )
        {
            if ( field == null )
            {
                tagName = uncapitalise( modelClass.getName() );
            }
            else
            {
                tagName = field.getName();
                if ( field instanceof ModelAssociation )
                {
                    ModelAssociation a = (ModelAssociation) field;
                    if ( ModelAssociation.MANY_MULTIPLICITY.equals( a.getMultiplicity() ) )
                    {
                        tagName = singular( tagName );
                    }
                }
            }
        }
        else
        {
            tagName = metadata.getTagName();
        }

        if ( field != null )
        {
            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );
            if ( fieldMetadata != null )
            {
                if ( fieldMetadata.getAssociationTagName() != null )
                {
                    tagName = fieldMetadata.getAssociationTagName();
                }
                else
                {
                    if ( fieldMetadata.getTagName() != null )
                    {
                        tagName = fieldMetadata.getTagName();
                    }
                }
            }
        }

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

        w.startElement( "table" );

        w.startElement( "tr" );

        w.startElement( "th" );

        w.writeText( "Element" );

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

        List fields = getFieldsForClass( objectModel, modelClass );

        for ( Iterator j = fields.iterator(); j.hasNext(); )
        {
            ModelField f = (ModelField) j.next();

            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) f.getMetadata( XmlFieldMetadata.ID );

            w.startElement( "tr" );

            // Element

            w.startElement( "td" );

            w.startElement( "code" );

            boolean flatAssociation = f instanceof ModelAssociation
                && isClassInModel( ( (ModelAssociation) f ).getTo(), objectModel )
                && XmlFieldMetadata.LIST_STYLE_FLAT.equals( fieldMetadata.getListStyle() );

            if ( flatAssociation )
            {
                ModelAssociation association = (ModelAssociation) f;

                ModelClass associationModelClass = objectModel.getClass( association.getTo(), getGeneratedVersion() );

                w.writeText( uncapitalise( associationModelClass.getName() ) );
            }
            else
            {
                w.writeText( f.getName() );
            }

            w.endElement();

            w.endElement();

            // Description

            w.startElement( "td" );

            if ( flatAssociation )
            {
                w.writeMarkup( "<b>List</b>  " );
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

        w.endElement();

        for ( Iterator iter = fields.iterator(); iter.hasNext(); )
        {
            ModelField f = (ModelField) iter.next();

            if ( f instanceof ModelAssociation && isClassInModel( ( (ModelAssociation) f ).getTo(), objectModel )
                && recursive )
            {
                ModelAssociation association = (ModelAssociation) f;
                ModelClass fieldModelClass = objectModel.getClass( association.getTo(), getGeneratedVersion() );

                if ( !written.contains( f.getName() ) )
                {
                    if ( ( modelClass.getName().equals( fieldModelClass.getName() ) )
                        && ( modelClass.getPackageName().equals( fieldModelClass.getPackageName() ) ) )
                    {
                        writeElementDescriptor( w, objectModel, fieldModelClass, f, written, false );
                    }
                    else
                    {
                        writeElementDescriptor( w, objectModel, fieldModelClass, f, written );
                    }
                }
            }
        }
    }

    private List getFieldsForClass( Model objectModel, ModelClass modelClass )
    {
        List fields = new ArrayList();
        while ( modelClass != null )
        {
            fields.addAll( modelClass.getFields( getGeneratedVersion() ) );
            String superClass = modelClass.getSuperClass();
            if ( superClass != null )
            {
                modelClass = objectModel.getClass( superClass, getGeneratedVersion() );
            }
            else
            {
                modelClass = null;
            }
        }
        return fields;
    }

    /**
     * Return the child attribute fields of this class.
     * @param objectModel global object model
     * @param modelClass current class
     * @return the list of attribute fields of this class
     */
    private List getAttributeFieldsForClass( Model objectModel, ModelClass modelClass )
    {
        List attributeFields = new ArrayList();
        while ( modelClass != null )
        {
            List allFields = modelClass.getFields( getGeneratedVersion() );

            Iterator allFieldsIt = allFields.iterator();

            while ( allFieldsIt.hasNext() )
            {
                ModelField field = (ModelField) allFieldsIt.next();
                XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );
                if ( fieldMetadata.isAttribute() )
                {
                    attributeFields.add( field );
                }
            }

            String superClass = modelClass.getSuperClass();
            if ( superClass != null )
            {
                modelClass = objectModel.getClass( superClass, getGeneratedVersion() );
            }
            else
            {
                modelClass = null;
            }
        }
        return attributeFields;
    }

    private String getModelClassDescriptor( Model objectModel, ModelClass modelClass, ModelField field, int depth )
    {
        return getModelClassDescriptor( objectModel, modelClass, field, depth, true );
    }

    private String getModelClassDescriptor( Model objectModel, ModelClass modelClass, ModelField field, int depth,
                                            boolean recursive )
        throws ModelloRuntimeException
    {
        StringBuffer sb = new StringBuffer();

        indent( sb, depth );

        ModelClassMetadata metadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

        String tagName;
        if ( metadata == null || metadata.getTagName() == null )
        {
            if ( field == null )
            {
                tagName = uncapitalise( modelClass.getName() );
            }
            else
            {
                tagName = field.getName();
                if ( field instanceof ModelAssociation )
                {
                    ModelAssociation a = (ModelAssociation) field;
                    if ( ModelAssociation.MANY_MULTIPLICITY.equals( a.getMultiplicity() ) )
                    {
                        tagName = singular( tagName );
                    }
                }
            }
        }
        else
        {
            tagName = metadata.getTagName();
        }

        if ( field != null )
        {
            XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );
            if ( fieldMetadata != null )
            {
                if ( fieldMetadata.getAssociationTagName() != null )
                {
                    tagName = fieldMetadata.getAssociationTagName();
                }
                else
                {
                    if ( fieldMetadata.getTagName() != null )
                    {
                        tagName = fieldMetadata.getTagName();
                    }
                }
            }
        }

        sb.append( "&lt;<a href=\"#class_" ).append( tagName ).append( "\">" ).append( tagName );

        sb.append( "</a>" );

        List fields = getFieldsForClass( objectModel, modelClass );

        List attributeFields = getAttributeFieldsForClass( objectModel, modelClass );

        if ( attributeFields.size() > 0 )
        {

            for ( Iterator iter = attributeFields.iterator(); iter.hasNext(); )
            {
                ModelField f = (ModelField) iter.next();

                sb.append( " " );

                sb.append( uncapitalise( f.getName() ) ).append( "=.." );
            }

            sb.append( " " );

            fields.removeAll( attributeFields );

        }

        if ( fields.size() > 0 )
        {
            sb.append( "&gt;\n" );

            for ( Iterator iter = fields.iterator(); iter.hasNext(); )
            {
                ModelField f = (ModelField) iter.next();

                if ( f instanceof ModelAssociation && isClassInModel( ( (ModelAssociation) f ).getTo(), objectModel )
                    && recursive )
                {
                    ModelAssociation association = (ModelAssociation) f;

                    XmlFieldMetadata fieldMetadata = (XmlFieldMetadata) f.getMetadata( XmlFieldMetadata.ID );

                    boolean listStyleWrapped =
                        ModelAssociation.MANY_MULTIPLICITY.equals( association.getMultiplicity() )
                        && !XmlFieldMetadata.LIST_STYLE_FLAT.equals( fieldMetadata.getListStyle() ); 

                    if ( listStyleWrapped )
                    {
                        depth++;

                        indent( sb, depth );

                        sb.append( "&lt;" ).append( uncapitalise( association.getName() ) ).append( "&gt;\n" );
                    }

                    ModelClass fieldModelClass = objectModel.getClass( association.getTo(), getGeneratedVersion() );

                    if ( ( modelClass.getName().equals( fieldModelClass.getName() ) )
                        && ( modelClass.getPackageName().equals( fieldModelClass.getPackageName() ) ) )
                    {
                        sb.append( getModelClassDescriptor( objectModel, fieldModelClass, f, depth + 1, false ) );
                    }
                    else
                    {
                        sb.append( getModelClassDescriptor( objectModel, fieldModelClass, f, depth + 1 ) );
                    }

                    if ( listStyleWrapped )
                    {
                        indent( sb, depth );

                        sb.append( "&lt;/" ).append( uncapitalise( association.getName() ) ).append( "&gt;\n" );

                        depth--;
                    }

                }
                else
                {
                    indent( sb, depth + 1 );

                    sb.append( "&lt;" ).append( uncapitalise( f.getName() ) ).append( "/&gt;\n" );

                }
            }

            indent( sb, depth );

            sb.append( "&lt;/" ).append( tagName ).append( "&gt;\n" );
        }
        else
        {
            sb.append( "/&gt;\n" );
        }

        return sb.toString();
    }

    private static void indent( StringBuffer sb, int depth )
    {
        for ( int i = 0; i < depth; i++ )
        {
            sb.append( "  " );
        }
    }
}
