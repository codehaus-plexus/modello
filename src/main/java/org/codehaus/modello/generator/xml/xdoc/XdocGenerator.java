package org.codehaus.modello.generator.xml.xdoc;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.generator.xml.DefaultXMLWriter;
import org.codehaus.modello.generator.xml.XMLWriter;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
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

        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
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

            for ( Iterator j = modelClass.getFields( getGeneratedVersion() ).iterator(); j.hasNext(); )
            {
                ModelField field = (ModelField) j.next();

                w.startElement( "tr" );

                w.startElement( "td" );

                if ( objectModel.getClass( capitalise( field.getName() ), getGeneratedVersion() ) != null )
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

            w.endElement();

            w.endElement();

            w.endElement();
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

        sb.append( getModelClassDescriptor( objectModel, objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() ), 0 ) );

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

        if ( modelClass.getFields( getGeneratedVersion() ).size() > 0 )
        {
            sb.append( "&gt;</a>\n" );

            for ( Iterator iter = modelClass.getFields( getGeneratedVersion() ).iterator(); iter.hasNext(); )
            {
                ModelField field = (ModelField) iter.next();

                ModelClass fieldModelClass = objectModel.getClass( capitalise( field.getName() ), getGeneratedVersion() );

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
