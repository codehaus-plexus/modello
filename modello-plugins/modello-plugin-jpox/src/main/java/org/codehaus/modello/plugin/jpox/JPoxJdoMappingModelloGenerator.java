package org.codehaus.modello.plugin.jpox;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.store.metadata.StoreFieldMetadata;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JPoxJdoMappingModelloGenerator
    extends AbstractModelloGenerator
{
    public void generate( Model model, Properties properties )
        throws ModelloException
    {
        initialize( model, properties );

        // ----------------------------------------------------------------------
        // Generate the JDO files
        // ----------------------------------------------------------------------

        try
        {
            File packageJdo = new File( getOutputDirectory(), "META-INF/package.jdo" );

            File parent = packageJdo.getParentFile();

            if ( !parent.exists() )
            {
                if( !parent.mkdirs() )
                {
                    throw new ModelloException( "Error while creating parent directories for the file '" + packageJdo.getAbsolutePath() + "'." );
                }
            }

            generatePackageJdo( packageJdo, model );
        }
        catch ( IOException e )
        {
            throw new ModelloException( "Error while writing package.jdo.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void generatePackageJdo( File file, Model model )
        throws IOException
    {
        OutputStreamWriter fileWriter = new OutputStreamWriter( new FileOutputStream( file ) , "UTF-8" );

        PrintWriter printWriter = new PrintWriter( fileWriter );

        XMLWriter writer = new PrettyPrintXMLWriter( printWriter );

        Map classes = new HashMap();

        for ( Iterator it = model.getClasses( getGeneratedVersion() ).iterator(); it.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) it.next();

            String packageName = modelClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

            List list = (List) classes.get( packageName );

            if ( list == null )
            {
                list = new ArrayList();

                classes.put( packageName, list );
            }

            list.add( modelClass );
        }

        printWriter.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        printWriter.println();
        printWriter.println( "<!DOCTYPE jdo PUBLIC" );
        printWriter.println( "  \"-//Sun Microsystems, Inc.//DTD Java Data Objects Metadata 1.0//EN\"" );
        printWriter.println( "  \"http://java.sun.com/dtd/jdo_1_0.dtd\">" );
        printWriter.println();

        writer.startElement( "jdo" );

        for ( Iterator it = classes.values().iterator(); it.hasNext(); )
        {
            List list = (List) it.next();

            if ( list.size() == 0 )
            {
                continue;
            }

            String packageName = ((ModelClass) list.get( 0 ) ).getPackageName( isPackageWithVersion(), getGeneratedVersion() );

            writer.startElement( "package" );

            writer.addAttribute( "name", packageName );

            for ( Iterator it2 = list.iterator(); it2.hasNext(); )
            {
                ModelClass modelClass = (ModelClass) it2.next();

                writeClass( writer, modelClass );
            }

            writer.endElement(); // package
        }

        writer.endElement(); // jdo

        printWriter.println();

        printWriter.close();
    }

    private void writeClass( XMLWriter writer, ModelClass modelClass )
    {
        writer.startElement( "class" );

        writer.addAttribute( "name", modelClass.getName() );

        writer.addAttribute( "identity-type", "datastore" );

        if ( modelClass.getSuperClass() != null )
        {
            ModelClass superClass = modelClass.getModel().getClass( modelClass.getSuperClass(), getGeneratedVersion() );

            String packageName = modelClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );
            String superPackageName = superClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

            if ( packageName.equals( superPackageName ) )
            {
                writer.addAttribute( "persistence-capable-superclass", superClass.getName() );
            }
            else
            {
                writer.addAttribute( "persistence-capable-superclass", superPackageName + "." + superClass.getName() );
            }
        }

        for ( Iterator it = modelClass.getFields( getGeneratedVersion() ).iterator(); it.hasNext(); )
        {
            ModelField modelField = (ModelField) it.next();

            writeModelField( writer,  modelField );
        }

        writer.endElement(); // class
    }

    private void writeModelField( XMLWriter writer, ModelField modelField )
    {
        writer.startElement( "field" );

        StoreFieldMetadata metaData = (StoreFieldMetadata) modelField.getMetadata( StoreFieldMetadata.ID );

        writer.addAttribute( "name", modelField.getName() );

        if ( metaData.isStorable() )
        {
            writer.addAttribute( "persistence-modifier", "persistent" );

            writer.addAttribute( "default-fetch-group", "true" );
        }
        else
        {
            writer.addAttribute( "persistence-modifier", "none" );
        }

        if ( modelField.getName().equals( "id" ) )
        {
            writer.addAttribute( "primary-key", "true" );
        }

        if ( modelField.isRequired() )
        {
            writer.addAttribute( "null-value", "exception" );
        }
        else
        {
            writer.addAttribute( "null-value", "none" );
        }

        if ( modelField.getType().equals( "java.util.List" ) ||
             modelField.getType().equals( "java.util.Set" ) )
        {
            writer.startElement( "collection" );

            ModelAssociation modelAssociation = (ModelAssociation) modelField;

            writer.addAttribute( "element-type", modelAssociation.getTo() );

            writer.endElement();
        }
        else if ( modelField.getType().equals( "java.util.Map" ) )
        {
            writer.startElement( "map" );

            ModelAssociation modelAssociation = (ModelAssociation) modelField;

            writer.addAttribute( "key-type", "java.lang.Object" );

            writer.addAttribute( "value-type", modelAssociation.getTo() );

            writer.endElement();
        }
        else if ( modelField.getType().equals( "java.util.Properties" ) )
        {
            writer.startElement( "map" );

            writer.addAttribute( "key-type", "java.lang.String" );

            writer.addAttribute( "value-type", "java.lang.String" );

            writer.endElement();
        }
        else
        {
            if ( metaData.getMaxSize() > 0 )
            {
                writer.startElement( "extension" );

                writer.addAttribute( "vendor-name", "jpox" );

                writer.addAttribute( "key", "length" );

                writer.addAttribute( "value", "max " + metaData.getMaxSize() );

                writer.endElement();
            }
        }

        writer.endElement(); // field
    }
}
