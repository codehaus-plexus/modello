package org.codehaus.modello.generator.xml.schema;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.generator.AbstractGenerator;
import org.codehaus.modello.generator.xml.DefaultXMLWriter;
import org.codehaus.modello.generator.xml.XMLWriter;

// This spits out the classes but I must also spit out xs:elements for the
// fields themselves for the xsd to be correct.

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @version $Id$
 */
public class XmlSchemaGenerator
    extends AbstractGenerator
{
    private boolean annotate;

    public XmlSchemaGenerator( String model, String outputDirectory, String modelVersion, boolean packageWithVersion )
    {
        super( model, outputDirectory, modelVersion, packageWithVersion );
    }

    public void generate()
        throws ModelloException
    {
        try
        {
            generateXmlSchema();
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while generating XML schema.", ex );
        }
    }

    private void generateXmlSchema()
        throws IOException, ModelloException
    {
        Model objectModel = getModel();

        File f = new File( getOutputDirectory(), objectModel.getId() + ".xsd" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        XMLWriter w = new DefaultXMLWriter( writer );

        writer.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );

        w.startElement( "xs:schema" );

        w.addAttribute( "xmlns:xs", "http://www.w3.org/2001/XMLSchema" );

        w.addAttribute( "elementFormDefault", "qualified" );

        for ( Iterator i = objectModel.getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            if ( outputElement( modelClass.getVersion(), modelClass.getName() ) )
            {
                w.startElement( "xs:element" );

                w.addAttribute( "name", uncapitalise( modelClass.getName() ) );

                annotation( w, modelClass.getDescription() );

                w.startElement( "xs:complexType" );

                w.startElement( "xs:sequence" );

                for ( Iterator j = modelClass.getFields().iterator(); j.hasNext(); )
                {
                    ModelField field = (ModelField) j.next();

                    if ( outputElement( field.getVersion(), modelClass.getName() + "." + field.getName() ) )
                    {
                        w.startElement( "xs:element" );

                        w.addAttribute( "ref", field.getName() );

                        if ( !field.isRequired() )
                        {
                            w.addAttribute( "minOccurs", "0" );
                        }

                        if ( annotate )
                        {
                            annotation( w, field.getDescription() );
                        }

                        w.endElement();
                    }
                }

                w.endElement();

                w.endElement();

                w.endElement();

                // Now write the fields separately

                writer.write( "\n" );

                for ( Iterator j = modelClass.getFields().iterator(); j.hasNext(); )
                {
                    ModelField field = (ModelField) j.next();

                    if ( outputElement( field.getVersion(), modelClass.getName() + "." + field.getName() ) )
                    {

                        // We only need to output elements that are primitive and all we
                        // are dealing with right now is Strings. We'll deal with primitives
                        // more thoroughly. We can borrow the code from castor.
                        if ( field.getType().equals( "String" ) )
                        {
                            w.startElement( "xs:element" );

                            w.addAttribute( "name", field.getName() );

                            w.addAttribute( "type", "xs:string" );

                            if ( annotate )
                            {
                                annotation( w, field.getDescription() );
                            }

                            w.endElement();
                        }
                        else if ( isCollection( field.getType() ) )
                        {
                            writer.write( "\n" );

                            w.startElement( "xs:element" );

                            w.addAttribute( "name", field.getName() );

                            w.startElement( "xs:complexType" );

                            w.startElement( "xs:sequence" );

                            w.startElement( "xs:element" );

                            w.addAttribute( "ref", singular( field.getName() ) );

                            if ( annotate )
                            {
                                annotation( w, field.getDescription() );
                            }

                            w.endElement();

                            w.endElement();

                            w.endElement();

                            w.endElement();
                        }
                    }
                }
            }

            writer.write( "\n" );

        }

        w.endElement();

        writer.flush();

        writer.close();
    }

    public static String uncapitalise( String str )
    {
        if ( str == null )
        {
            return null;
        }
        else if ( str.length() == 0 )
        {
            return "";
        }
        else
        {
            return new StringBuffer( str.length() )
                .append( Character.toLowerCase( str.charAt( 0 ) ) )
                .append( str.substring( 1 ) )
                .toString();
        }
    }

    private void annotation( XMLWriter w, String annotation )
    {
        if ( annotation == null )
        {
            return;
        }

        w.startElement( "xs:annotation" );

        w.startElement( "xs:documentation" );

        w.writeText( annotation );

        w.endElement();

        w.endElement();
    }
}
