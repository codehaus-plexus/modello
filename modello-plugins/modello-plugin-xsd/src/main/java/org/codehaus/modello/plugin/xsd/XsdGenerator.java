package org.codehaus.modello.plugin.xsd;

/*
 * Copyright (c) 2005, Codehaus.org
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
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.xsd.metadata.XsdClassMetadata;
import org.codehaus.modello.plugin.xsd.metadata.XsdModelMetadata;
import org.codehaus.modello.plugins.xml.AbstractXmlGenerator;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlModelMetadata;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 * @version $Id$
 */
public class XsdGenerator
    extends AbstractXmlGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateXsd( parameters );
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating xsd.", ex );
        }
    }

    private void generateXsd( Properties parameters )
        throws IOException, ModelloException
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
        String xsdFileName = parameters.getProperty( ModelloParameterConstants.OUTPUT_XSD_FILE_NAME );

        File f = new File( directory, objectModel.getId() + "-" + getGeneratedVersion() + ".xsd" );

        if ( xsdFileName != null )
        {
            f = new File( directory, xsdFileName );
        }

        Writer writer = WriterFactory.newXmlWriter( f );

        try
        {
            XMLWriter w = new PrettyPrintXMLWriter( writer );

            writer.write( "<?xml version=\"1.0\"?>\n" );

            // TODO: the writer should be knowledgeable of namespaces, but this works
            w.startElement( "xs:schema" );
            w.addAttribute( "xmlns:xs", "http://www.w3.org/2001/XMLSchema" );
            w.addAttribute( "elementFormDefault", "qualified" );

            ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ),
                                                    getGeneratedVersion() );

            XmlModelMetadata xmlModelMetadata = (XmlModelMetadata) root.getModel().getMetadata( XmlModelMetadata.ID );

            XsdModelMetadata xsdModelMetadata = (XsdModelMetadata) root.getModel().getMetadata( XsdModelMetadata.ID );

            String namespace;
            if ( StringUtils.isNotEmpty( xsdModelMetadata.getNamespace() ) )
            {
                namespace = xsdModelMetadata.getNamespace( getGeneratedVersion() );
            }
            else
            {
                // xsd.namespace is not set, try using xml.namespace
                if ( StringUtils.isEmpty( xmlModelMetadata.getNamespace() ) )
                {
                    throw new ModelloException( "Cannot generate xsd without xmlns specification:"
                                                + " <model xml.namespace='...'> or <model xsd.namespace='...'>" );
                }

                namespace = xmlModelMetadata.getNamespace( getGeneratedVersion() );
            }

            w.addAttribute( "xmlns", namespace );

            String targetNamespace;
            if ( xsdModelMetadata.getTargetNamespace() == null )
            {
                // xsd.target-namespace not set, using namespace
                targetNamespace = namespace;
            }
            else
            {
                targetNamespace = xsdModelMetadata.getTargetNamespace( getGeneratedVersion() );
            }

            // add targetNamespace if attribute is not blank (specifically set to avoid a target namespace)
            if ( StringUtils.isNotBlank( targetNamespace ) )
            {
                w.addAttribute( "targetNamespace", targetNamespace );
            }

            w.startElement( "xs:element" );
            String tagName = resolveTagName( root );
            w.addAttribute( "name", tagName );
            w.addAttribute( "type", root.getName() );

            writeClassDocumentation( w, root );

            w.endElement();

            // Element descriptors
            // Traverse from root so "abstract" models aren't included
            int initialCapacity = objectModel.getClasses( getGeneratedVersion() ).size();
            writeComplexTypeDescriptor( w, objectModel, root, new HashSet( initialCapacity ) );

            w.endElement();
        }
        finally
        {
            writer.close();
        }
    }

    private static void writeClassDocumentation( XMLWriter w, ModelClass modelClass )
    {
        writeDocumentation( w, modelClass.getVersionRange().toString(), modelClass.getDescription() );
    }

    private static void writeFieldDocumentation( XMLWriter w, ModelField field )
    {
        writeDocumentation( w, field.getVersionRange().toString(), field.getDescription() );
    }

    private static void writeDocumentation( XMLWriter w, String version, String description )
    {
        if ( version != null || description != null )
        {
            w.startElement( "xs:annotation" );

            if ( version != null )
            {
                w.startElement( "xs:documentation" );
                w.addAttribute( "source", "version" );
                w.writeText( version );
                w.endElement();
            }

            if ( description != null )
            {
                w.startElement( "xs:documentation" );
                w.addAttribute( "source", "description" );
                w.writeText( description );
                w.endElement();
            }

            w.endElement();
        }
    }

    private void writeComplexTypeDescriptor( XMLWriter w, Model objectModel, ModelClass modelClass, Set written )
    {
        written.add( modelClass );

        w.startElement( "xs:complexType" );
        w.addAttribute( "name", modelClass.getName() );

        List fields = getFieldsForClass( modelClass );

        boolean hasContentField = hasContentField( fields );

        List attributeFields = getAttributeFieldsForClass( modelClass );

        fields.removeAll( attributeFields );

        boolean mixedContent = hasContentField && fields.size() > 0;

        // other fields with complexType
        // if yes it's a mixed content element and attribute
        if ( mixedContent )
        {
            w.addAttribute( "mixed", "true" );
        }
        else if ( hasContentField )
        {
            // yes it's only an extension of xs:string
            w.startElement( "xs:simpleContent" );

            w.startElement( "xs:extension" );

            w.addAttribute( "base", "xs:string" );
        }

        writeClassDocumentation( w, modelClass );

        Set toWrite = new HashSet();

        if ( fields.size() > 0 )
        {
            XsdClassMetadata xsdClassMetadata = (XsdClassMetadata) modelClass.getMetadata( XsdClassMetadata.ID );
            boolean compositorAll = XsdClassMetadata.COMPOSITOR_ALL.equals( xsdClassMetadata.getCompositor() );

            if ( ( mixedContent ) || ( !hasContentField ) )
            {
                if ( compositorAll )
                {
                    w.startElement( "xs:all" );
                }
                else
                {
                    w.startElement( "xs:sequence" );
                }
            }

            for ( Iterator j = fields.iterator(); j.hasNext(); )
            {
                ModelField field = (ModelField) j.next();

                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

                if ( !hasContentField )
                {
                    w.startElement( "xs:element" );
                }

                // Usually, would only do this if the field is not "required", but due to inheritance, it may be
                // present, even if not here, so we need to let it slide
                if ( !hasContentField )
                {
                    w.addAttribute( "minOccurs", "0" );
                }

                String xsdType = getXsdType( field.getType() );

                if ( "Date".equals( field.getType() ) && "long".equals( xmlFieldMetadata.getFormat() ) )
                {
                    xsdType = getXsdType( "long" );
                }

                if ( ( xsdType != null ) || "char".equals( field.getType() ) || "Char".equals( field.getType() ) )
                {
                    w.addAttribute( "name", resolveTagName( field, xmlFieldMetadata ) );
                    if ( xsdType != null )
                    {
                        // schema built-in datatype
                        w.addAttribute( "type", xsdType );
                    }

                    if ( field.getDefaultValue() != null )
                    {
                        w.addAttribute( "default", field.getDefaultValue() );
                    }

                    writeFieldDocumentation( w, field );

                    if ( xsdType == null )
                    {
                        writeCharElement( w );
                    }
                }
                else
                {
                    // TODO cleanup/split this part it's no really human readable :-)
                    if ( isInnerAssociation( field ) )
                    {
                        ModelAssociation association = (ModelAssociation) field;
                        ModelClass fieldModelClass = objectModel.getClass( association.getTo(), getGeneratedVersion() );

                        toWrite.add( fieldModelClass );

                        if ( association.isManyMultiplicity() )
                        {
                            XmlAssociationMetadata xmlAssociationMetadata =
                                (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                            if ( xmlAssociationMetadata.isWrappedItems() )
                            {
                                w.addAttribute( "name", resolveTagName( field, xmlFieldMetadata ) );
                                writeFieldDocumentation( w, field );

                                writeListElement( w, xmlFieldMetadata, xmlAssociationMetadata, field,
                                                  fieldModelClass.getName() );
                            }
                            else
                            {
                                if ( compositorAll )
                                {
                                    // xs:all does not accept maxOccurs="unbounded", xs:sequence MUST be used
                                    // to be able to represent this constraint
                                    throw new IllegalStateException( field.getName() + " field is declared as xml.listStyle=\"flat\" "
                                        + "then class " + modelClass.getName() + " MUST be declared as xsd.compositor=\"sequence\"" );
                                }

                                if ( mixedContent )
                                {
                                    w.startElement( "xs:element" );
                                    w.addAttribute( "minOccurs", "0" );
                                }

                                if ( xmlAssociationMetadata != null && xmlAssociationMetadata.getTagName() != null )
                                {
                                    w.addAttribute( "name", xmlAssociationMetadata.getTagName() );
                                }
                                else
                                {
                                    w.addAttribute( "name", singular( association.getName() ) );
                                }

                                w.addAttribute( "type", fieldModelClass.getName() );
                                w.addAttribute( "maxOccurs", "unbounded" );

                                writeFieldDocumentation( w, field );

                                if ( mixedContent )
                                {
                                    w.endElement();
                                }
                            }
                        }
                        else
                        {
                            // not many multiplicity
                            w.addAttribute( "name", resolveTagName( field, xmlFieldMetadata ) );
                            w.addAttribute( "type", fieldModelClass.getName() );
                            writeFieldDocumentation( w, field );
                        }
                    }
                    else
                    {
                        w.addAttribute( "name", resolveTagName( field, xmlFieldMetadata ) );
                        writeFieldDocumentation( w, field );

                        if ( List.class.getName().equals( field.getType() ) )
                        {
                            ModelAssociation association = (ModelAssociation) field;

                            XmlAssociationMetadata xmlAssociationMetadata =
                                (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                            writeListElement( w, xmlFieldMetadata, xmlAssociationMetadata, field,
                                              getXsdType( "String" ) );
                        }
                        else if ( Properties.class.getName().equals( field.getType() )
                                        || "DOM".equals( field.getType() ) )
                        {
                            writePropertiesElement( w );
                        }
                        else if ( "Content".equals( field.getType() ) )
                        {
                            // skip this
                        }
                        else
                        {
                            throw new IllegalStateException( "Non-association field of a non-primitive type '"
                                                             + field.getType() + "' for '" + field.getName() + "'" );
                        }
                    }
                }
                if ( !hasContentField )
                {
                    w.endElement();
                }
            }
            if ( !hasContentField || mixedContent )
            {
                w.endElement(); // xs:all or xs:sequence
            }
        }

        for ( Iterator j = attributeFields.iterator(); j.hasNext(); )
        {
            ModelField field = (ModelField) j.next();

            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            w.startElement( "xs:attribute" );

            String xsdType = getXsdType( field.getType() );

            String tagName = resolveTagName( field, xmlFieldMetadata );

            w.addAttribute( "name", tagName );

            if ( xsdType != null )
            {
                w.addAttribute( "type", xsdType );
            }

            if ( field.getDefaultValue() != null )
            {
                w.addAttribute( "default", field.getDefaultValue() );
            }

            writeFieldDocumentation( w, field );

            if ( "char".equals( field.getType() ) || "Char".equals( field.getType() ) )
            {
                writeCharElement( w );
            }
            else if ( xsdType == null )
            {
                throw new IllegalStateException( "Attribute field of a non-primitive type '" + field.getType()
                    + "' for '" + field.getName() + "'" );
            }

            w.endElement();
        }

        if ( hasContentField && !mixedContent )
        {
            w.endElement(); //xs:extension

            w.endElement(); //xs:simpleContent
        }


        w.endElement(); // xs:complexType

        for ( Iterator iter = toWrite.iterator(); iter.hasNext(); )
        {
            ModelClass fieldModelClass = (ModelClass) iter.next();
            if ( !written.contains( fieldModelClass ) )
            {
                writeComplexTypeDescriptor( w, objectModel, fieldModelClass, written );
            }
        }
    }

    private static void writeCharElement( XMLWriter w )
    {
        // a char, described as a simpleType base on string with a length restriction to 1
        w.startElement( "xs:simpleType" );

        w.startElement( "xs:restriction" );
        w.addAttribute( "base", "xs:string" );

        w.startElement( "xs:length" );
        w.addAttribute( "value", "1" );
        w.addAttribute( "fixed", "true" );

        w.endElement();

        w.endElement();

        w.endElement();
    }

    private static void writePropertiesElement( XMLWriter w )
    {
        w.startElement( "xs:complexType" );

        w.startElement( "xs:sequence" );

        w.startElement( "xs:any" );
        w.addAttribute( "minOccurs", "0" );
        w.addAttribute( "maxOccurs", "unbounded" );
        w.addAttribute( "processContents", "skip" );

        w.endElement();

        w.endElement();

        w.endElement();
    }

    private void writeListElement( XMLWriter w, XmlFieldMetadata xmlFieldMetadata,
                                   XmlAssociationMetadata xmlAssociationMetadata, ModelField field, String type )
    {
        String fieldTagName = resolveTagName( field, xmlFieldMetadata );

        String valuesTagName = resolveTagName( fieldTagName, xmlAssociationMetadata );

        w.startElement( "xs:complexType" );

        w.startElement( "xs:sequence" );

        w.startElement( "xs:element" );
        w.addAttribute( "name", valuesTagName );
        w.addAttribute( "minOccurs", "0" );
        w.addAttribute( "maxOccurs", "unbounded" );
        w.addAttribute( "type", type );

        w.endElement();

        w.endElement();

        w.endElement();
    }

    private static String getXsdType( String type )
    {
        if ( "String".equals( type ) )
        {
            return "xs:string";
        }
        else if ( "boolean".equals( type ) || "Boolean".equals( type ) )
        {
            return "xs:boolean";
        }
        else if ( "byte".equals( type ) || "Byte".equals( type ) )
        {
            return "xs:byte";
        }
        else if ( "short".equals( type ) || "Short".equals( type ) )
        {
            return "xs:short";
        }
        else if ( "int".equals( type ) || "Integer".equals( type ) )
        {
            return "xs:int";
        }
        else if ( "long".equals( type ) || "Long".equals( type ) )
        {
            return "xs:long";
        }
        else if ("float".equals( type ) || "Float".equals( type ) )
        {
            return "xs:float";
        }
        else if ("double".equals( type ) || "Double".equals( type ) )
        {
            return "xs:double";
        }
        else if ( "Date".equals( type ) )
        {
            return "xs:dateTime";
        }
        else
        {
            return null;
        }
    }
}
