package org.codehaus.modello.generator.xml.xdoc;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.generator.AbstractGenerator;
import org.codehaus.modello.generator.xml.DefaultXMLWriter;
import org.codehaus.modello.generator.xml.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;

/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class XdocGenerator
    extends AbstractGenerator
{
    public XdocGenerator( String model, String outputDirectory )
    {
        super( model, outputDirectory );
    }

    public void generate()
        throws Exception
    {
        Model objectModel = getModel();

        File f = new File( getOutputDirectory(), objectModel.getId() + ".xml" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        XMLWriter w = new DefaultXMLWriter( writer );

        writer.write( "<?xml version=\"1.0\"?>\n" );

        w.startElement( "document" );

        w.startElement( "properties" );

        w.startElement( "author" );

        w.addAttribute( "email", "dev@modello.codehaus.org" );

        w.writeText( "Maven Development Team" );

        w.endElement();

        w.startElement( "title" );

        w.writeText( "Maven Model Documentation" );

        w.endElement();

        w.endElement();

        // Body

        w.startElement( "body" );

        for ( Iterator i = objectModel.getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            w.startElement( "section" );

            w.addAttribute( "name", modelClass.getName() );

            w.startElement( "p" );

            w.startElement( "table" );

            w.startElement( "tr" );

            w.startElement( "th" );

            w.writeText( "Element" );

            w.endElement();

            w.startElement( "th" );

            w.writeText( "Description" );

            w.endElement();

            w.endElement();

            for ( Iterator j = modelClass.getFields().iterator(); j.hasNext(); )
            {
                ModelField field = (ModelField) j.next();

                w.startElement( "tr" );

                w.startElement( "td" );

                w.writeText( field.getName() );

                w.endElement();

                w.startElement( "td" );

                if ( field.getDescription() != null )
                {
                    w.writeText( field.getDescription() );
                }
                else
                {
                    w.writeText( "No description." );
                }

                w.endElement();

                w.endElement();
            }

            w.endElement();

            w.endElement();

            w.endElement();
        }

        w.endElement();

        w.endElement();

        writer.flush();

        writer.close();
    }
}
