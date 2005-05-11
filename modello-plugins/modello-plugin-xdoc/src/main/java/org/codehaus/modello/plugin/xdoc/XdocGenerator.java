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
import org.codehaus.modello.ModelloRuntimeException;
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
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 * @author <a href="mailto:emmanuel@venisse.net">Emmanuel Venisse</a>
 * @version $Id$
 */
public class XdocGenerator
    extends AbstractModelloGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

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

        FileWriter writer = new FileWriter( f );

        XMLWriter w = new PrettyPrintXMLWriter( writer );

        writer.write( "<?xml version=\"1.0\"?>\n" );

        w.startElement( "document" );

        w.startElement( "properties" );

        w.startElement( "title" );

        w.writeText( objectModel.getDescription() );

        w.endElement();

        w.endElement();

        // Body

        w.startElement( "body" );
        
        // Descriptor with links
        
        w.startElement( "section" );

        w.addAttribute( "name", objectModel.getDescription() );

        w.startElement( "p" );

        w.startElement( "source" );

        StringBuffer sb = new StringBuffer();

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );
        sb.append( getModelClassDescriptor( objectModel, root, 0 ) );

        w.writeMarkup( "\n" + sb.toString() );

        w.endElement();

        // Element descriptors
        // Traverse from root so "abstract" models aren't included
        writeElementDescriptor( w, objectModel, root, new HashSet() );

        w.endElement();

        w.endElement();

        w.endElement();

        w.endElement();

        writer.flush();

        writer.close();
    }

    private void writeElementDescriptor( XMLWriter w, Model objectModel, ModelClass modelClass, Set written )
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

        w.startElement( "a" );

        w.addAttribute( "name", modelClass.getName() );

        w.endElement();

        w.startElement( "subsection" );

        w.addAttribute( "name", tagName );

        if ( modelClass.getDescription() != null )
        {
            w.startElement( "p" );

            w.writeMarkup( modelClass.getDescription() );

            w.endElement();
        }

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

            w.startElement( "code" );

            w.writeText( field.getName() );

            w.endElement();

            w.endElement();

            w.startElement( "td" );

            if ( field.getDescription() != null )
            {
                w.writeMarkup( field.getDescription() );
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

        for ( Iterator iter = modelClass.getFields( getGeneratedVersion() ).iterator(); iter.hasNext(); )
        {
            ModelField field = (ModelField) iter.next();

            if ( field instanceof ModelAssociation &&
                isClassInModel( ( (ModelAssociation) field ).getTo(), objectModel ) )
            {
                ModelAssociation association = (ModelAssociation) field;
                ModelClass fieldModelClass = objectModel.getClass( association.getTo(), getGeneratedVersion() );

                if ( !written.contains( fieldModelClass ) )
                {
                    writeElementDescriptor( w, objectModel, fieldModelClass, written );
                }
            }
        }
    }

    private String getModelClassDescriptor( Model objectModel, ModelClass modelClass, int depth )
        throws ModelloRuntimeException
    {
        StringBuffer sb = new StringBuffer();

        for ( int i = 0; i < depth; i++ )
        {
            sb.append( "  " );
        }

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
        sb.append( "&lt;<a href=\"#" + modelClass.getName() + "\">" + tagName );

        if ( modelClass.getFields( getGeneratedVersion() ).size() > 0 )
        {
            sb.append( "</a>&gt;\n" );

            for ( Iterator iter = modelClass.getFields( getGeneratedVersion() ).iterator(); iter.hasNext(); )
            {
                ModelField field = (ModelField) iter.next();

                ModelClass fieldModelClass = null;

                if ( field instanceof ModelAssociation &&
                    isClassInModel( ( (ModelAssociation) field ).getTo(), objectModel ) )
                {
                    ModelAssociation association = (ModelAssociation) field;

                    if ( ModelAssociation.MANY_MULTIPLICITY.equals( association.getMultiplicity() ) )
                    {
                        depth++;

                        for ( int i = 0; i < depth; i++ )
                        {
                            sb.append( "  " );
                        }

                        sb.append( "&lt;" + uncapitalise( association.getName() ) + "&gt;\n" );
                    }

                    fieldModelClass = objectModel.getClass( association.getTo(), getGeneratedVersion() );

                    sb.append( getModelClassDescriptor( objectModel, fieldModelClass, depth + 1 ) );

                    if ( ModelAssociation.MANY_MULTIPLICITY.equals( association.getMultiplicity() ) )
                    {
                        for ( int i = 0; i < depth; i++ )
                        {
                            sb.append( "  " );
                        }

                        sb.append( "&lt;/" + uncapitalise( association.getName() ) + "&gt;\n" );

                        depth--;
                    }

                }
                else
                {
                    for ( int i = 0; i < depth + 1; i++ )
                    {
                        sb.append( "  " );
                    }

                    sb.append( "&lt;" + uncapitalise( field.getName() ) + "/&gt;\n" );
                }
            }

            for ( int i = 0; i < depth; i++ )
            {
                sb.append( "  " );
            }

            sb.append( "&lt;/" + tagName + "&gt;\n" );
        }
        else
        {
            sb.append( "</a>/&gt;\n" );
        }

        return sb.toString();
    }
}
