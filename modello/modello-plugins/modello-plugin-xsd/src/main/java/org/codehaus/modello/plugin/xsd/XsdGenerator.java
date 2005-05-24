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
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    extends AbstractModelloGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateXsd();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating xsd.", ex );
        }
    }

    private void generateXsd()
        throws IOException
    {
        Model objectModel = getModel();

        String directory = getOutputDirectory().getAbsolutePath();

        if ( isPackageWithVersion() )
        {
            directory += "/" + getGeneratedVersion();
        }

        File f = new File( directory, objectModel.getId() + "-" + getGeneratedVersion() + ".xsd" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        XMLWriter w = new PrettyPrintXMLWriter( writer );

        writer.write( "<?xml version=\"1.0\"?>\n" );

        // TODO: the writer should be knowledgable of namespaces, but this works
        w.startElement( "xs:schema" );
        w.addAttribute( "xmlns:xs", "http://www.w3.org/2001/XMLSchema" );
        w.addAttribute( "elementFormDefault", "qualified" );
        // TODO: make configurable
        w.addAttribute( "targetNamespace", "http://maven.apache.org/POM/4.0.0" );
        w.addAttribute( "xmlns", "http://maven.apache.org/POM/4.0.0" );

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        // Element descriptors
        // Traverse from root so "abstract" models aren't included
        int initialCapacity = objectModel.getClasses( getGeneratedVersion() ).size();
        writeElementDescriptor( w, objectModel, root, new HashSet( initialCapacity ), new HashSet() );

        w.endElement();

        writer.flush();

        writer.close();
    }

    private void writeElementDescriptor( XMLWriter w, Model objectModel, ModelClass modelClass, Set written,
                                         Set writtenFields )
    {
        written.add( modelClass );

        ModelClassMetadata metadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

        String tagName;
        if ( metadata == null || metadata.getTagName() == null )
        {
            tagName = uncapitalise( modelClass.getName() );
        }
        else
        {
            tagName = metadata.getTagName();
        }

        w.startElement( "xs:element" );
        w.addAttribute( "name", tagName );

        w.startElement( "xs:complexType" );

        w.startElement( "xs:all" );

        for ( Iterator j = modelClass.getFields( getGeneratedVersion() ).iterator(); j.hasNext(); )
        {
            ModelField field = (ModelField) j.next();

            w.startElement( "xs:element" );

            String xsdType = getXsdType( field.getType() );
            if ( xsdType != null )
            {
                writtenFields.add( getFieldKey( field ) );
                w.addAttribute( "name", field.getName() );
                w.addAttribute( "type", xsdType );

                if ( field.getDefaultValue() != null )
                {
                    w.addAttribute( "default", field.getDefaultValue() );
                }
            }
            else
            {
                w.addAttribute( "name", field.getName() );
            }

            // Usually, would only do this if the field is not "required", but due to inheritence, it may be present,
            // even if not here, so we need to let it slide
            w.addAttribute( "minOccurs", "0" );

            w.endElement();
        }

        w.endElement();

        w.endElement();

        w.endElement();

        for ( Iterator iter = modelClass.getFields( getGeneratedVersion() ).iterator(); iter.hasNext(); )
        {
            ModelField field = (ModelField) iter.next();

            if ( !writtenFields.contains( getFieldKey( field ) ) )
            {
                if ( field instanceof ModelAssociation &&
                    isClassInModel( ( (ModelAssociation) field ).getTo(), objectModel ) )
                {
                    ModelAssociation association = (ModelAssociation) field;
                    ModelClass fieldModelClass = objectModel.getClass( association.getTo(), getGeneratedVersion() );

                    if ( !written.contains( fieldModelClass ) )
                    {
                        writeElementDescriptor( w, objectModel, fieldModelClass, written, writtenFields );
                    }
                    if ( "*".equals( association.getMultiplicity() ) )
                    {
                        writeListElement( w, field, writtenFields );
                    }
                }
                else
                {
                    if ( List.class.getName().equals( field.getType() ) )
                    {
                        writeListElement( w, field, writtenFields );
                    }
                    else if ( Properties.class.getName().equals( field.getType() ) || "DOM".equals( field.getType() ) )
                    {
                        writePropertiesElement( w, field, writtenFields );
                    }
                    else
                    {
                        writeSingleElement( field, w, writtenFields );
                    }
                }
            }
        }
    }

    private static String getFieldKey( ModelField field )
    {
        return field.getName() + " " + field.getType();
    }

    private void writeSingleElement( ModelField field, XMLWriter w, Set writtenFields )
    {
        String xsdType = getXsdType( field.getType() );

        if ( xsdType == null )
        {
            throw new IllegalArgumentException( "Unknown type: " + field.getType() );
        }

        w.startElement( "xs:element" );
        w.addAttribute( "name", field.getName() );
        w.addAttribute( "type", xsdType );
        w.endElement();

        writtenFields.add( getFieldKey( field ) );
    }

    private void writePropertiesElement( XMLWriter w, ModelField field, Set writtenFields )
    {
        w.startElement( "xs:element" );
        w.addAttribute( "name", field.getName() );
        writtenFields.add( getFieldKey( field ) );

        w.startElement( "xs:complexType" );

        w.startElement( "xs:sequence" );

        w.startElement( "xs:any" );
        w.addAttribute( "minOccurs", "0" );
        w.addAttribute( "maxOccurs", "unbounded" );
        w.addAttribute( "processContents", "lax" );

        w.endElement();

        w.endElement();

        w.endElement();

        w.endElement();
    }

    private void writeListElement( XMLWriter w, ModelField field, Set writtenFields )
    {
        w.startElement( "xs:element" );
        w.addAttribute( "name", field.getName() );
        writtenFields.add( getFieldKey( field ) );

        w.startElement( "xs:complexType" );

        w.startElement( "xs:sequence" );

        w.startElement( "xs:element" );
        w.addAttribute( "name", singular( field.getName() ) );
        w.addAttribute( "minOccurs", "0" );
        w.addAttribute( "maxOccurs", "unbounded" );

        w.endElement();

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
        else if ( "boolean".equals( type ) )
        {
            return "xs:boolean";
        }
        else
        {
            return null;
        }
    }

}
