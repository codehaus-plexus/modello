package org.codehaus.modello.generator.xml.xdoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.ModelField;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.generator.xml.DefaultXMLWriter;
import org.codehaus.modello.generator.xml.XMLWriter;
import org.codehaus.modello.plugin.AbstractModelloGenerator;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @version $Id$
 */
public class XdocGenerator
    extends AbstractModelloGenerator
{
/*
    public XdocGenerator( Model model, File outputDirectory, String modelVersion, boolean packageWithVersion )
    {
        super( model, outputDirectory, modelVersion, packageWithVersion );
    }
*/
    public void generate(Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateXdoc();
        }
        catch( IOException ex )
        {
            throw new ModelloException( "Exception while generating XDoc.", ex );
        }
    }

    private void generateXdoc()
        throws ModelloException, IOException
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
        
        // Descriptor with links
        
        w.startElement( "section" );

        w.addAttribute( "name", "Descriptor with links" );

        w.startElement( "p" );

        w.startElement( "source" );

        w.writeText( "\n" + getDescriptorWithLink( objectModel ) );

        w.endElement();

        w.endElement();
        
        // Element descriptors

        for ( Iterator i = objectModel.getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            if ( outputElement( modelClass ) )
            {
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

                    if ( outputElement( field ) )
                    {
                        w.startElement( "tr" );

                        w.startElement( "td" );

                        if ( objectModel.getClass( capitalise( field.getName() ) ) != null )
                        {
                            w.writeText( field.getName() );
                        }
                        else
                        {
                            w.writeText( field.getName() );
                        }

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
                }

                w.endElement();

                w.endElement();

                w.endElement();
            }
        }

        w.endElement();

        w.endElement();

        w.endElement();

        writer.flush();

        writer.close();
    }

    private String getDescriptorWithLink( Model objectModel )
        throws ModelloRuntimeException
    {
        StringBuffer sb = new StringBuffer();

        sb.append( getModelClassDescriptor( objectModel, objectModel.getClass( objectModel.getRoot() ), 0 ) );

        return sb.toString();
    }

    // @todo I'm not going to test this right now but I have reworked the source handling in
    // the new xdoc plugin to handle the escaping of XML that that we don't have to do it in
    // source code.
    private String getModelClassDescriptor( Model objectModel, ModelClass modelClass, int depth )
        throws ModelloRuntimeException
    {
        StringBuffer sb = new StringBuffer();

        for ( int i = 0; i < depth; i++ )
        {
            sb.append( "  " );
        }

        sb.append( "<a href=\"#" + modelClass.getName() + "\">&lt;" + uncapitalise( modelClass.getName() ) );

        if ( modelClass.getFields().size() > 0 && outputElement( modelClass ) )
        {
            sb.append( "&gt;</a>\n" );

            for ( Iterator iter = modelClass.getFields().iterator(); iter.hasNext(); )
            {
                ModelField field = (ModelField) iter.next();

                if ( outputElement( field ) )
                {

                    ModelClass fieldModelClass = objectModel.getClass( capitalise( field.getName() ) );

                    if ( fieldModelClass != null )
                    {
                        sb.append( getModelClassDescriptor( objectModel, fieldModelClass, depth + 1 ) );
                    }
                    else
                    {
                        for ( int i = 0; i < depth + 1; i++ )
                        {
                            sb.append( "  " );
                        }


                        sb.append( "<a href=\"#" + modelClass.getName() + "\">&lt;" + uncapitalise( field.getName() ) + "/&gt;</a>\n" );
                    }
                }
            }

            for ( int i = 0; i < depth; i++ )
            {
                sb.append( "  " );
            }

            sb.append( "<a href=\"#" + modelClass.getName() + "\">&lt;" + uncapitalise( modelClass.getName() ) + "&gt;</a>\n" );
        }
        else
        {
            sb.append( "/&gt;</a>\n" );
        }

        return sb.toString();
    }
}
