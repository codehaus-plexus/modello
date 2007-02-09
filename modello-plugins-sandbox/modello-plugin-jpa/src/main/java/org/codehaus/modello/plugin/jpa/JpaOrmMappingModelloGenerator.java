package org.codehaus.modello.plugin.jpa;

/**
 * Copyright 2007 Rahul Thakur
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.jpa.metadata.JpaClassLevelMetadata;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessor;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorContext;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorException;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorFactory;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorInstantiationException;
import org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * Generates the an ORM (Object Relational Mapping) from the Modello model
 * source.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id: JpaOrmMappingModelloGenerator.java 780 2007-01-11 19:09:14Z
 *          rahul $
 * @since 1.0.0
 * @plexus.component role="org.codehaus.modello.plugin.ModelloGenerator"
 *                   role-hint="jpa-mapping"
 */
public class JpaOrmMappingModelloGenerator
    extends AbstractModelloGenerator
{

    /**
     * @plexus.requirement role-hint="jpa"
     */
    private MetadataProcessorFactory processorFactory;

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.ModelloGenerator#generate(org.codehaus.modello.model.Model,
     *      java.util.Properties)
     */
    public void generate( Model model, Properties properties )
        throws ModelloException
    {
        initialize( model, properties );

        String fileName = properties.getProperty( ModelloParameterConstants.FILENAME, "orm.xml" );

        File directory = getOutputDirectory();
        File orm = new File( directory, fileName );
        File parent = orm.getParentFile();

        if ( !parent.exists() && !parent.mkdirs() )
            throw new ModelloException( "Error while creating parent directories for the file " + "'"
                + orm.getAbsolutePath() + "'." );

        // all good, continue with ORM generation
        try
        {
            generateOrm( orm, model );
        }
        catch ( IOException e )
        {
            if ( getLogger().isErrorEnabled() )
                getLogger().error( "Error generating ORM mapping " + orm.getAbsolutePath() );
        }

    }

    private void generateOrm( File orm, Model model )
        throws IOException, ModelloException
    {
        OutputStreamWriter fileWriter = new OutputStreamWriter( new FileOutputStream( orm ), "UTF-8" );

        PrintWriter printWriter = new PrintWriter( fileWriter );

        XMLWriter writer = new PrettyPrintXMLWriter( printWriter );

        Map classes = new HashMap();

        // Processed classes to be mapped here
        for ( Iterator it = model.getClasses( getGeneratedVersion() ).iterator(); it.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) it.next();

            JpaClassLevelMetadata metadata = (JpaClassLevelMetadata) modelClass.getMetadata( JpaClassLevelMetadata.ID );

            List processorMetadataList = metadata.getProcessorMetadata();
            for ( Iterator it2 = processorMetadataList.iterator(); it2.hasNext(); )
            {
                ProcessorMetadata procMetadata = (ProcessorMetadata) it2.next();
                try
                {
                    MetadataProcessor metadataProcessor = processorFactory.createMetadataProcessor( procMetadata );

                    // FIXME: Pass Context when we set it up.
                    boolean valid = metadataProcessor.validate( new MetadataProcessorContext(), procMetadata );

                    // FIXME: Pass Context when we set it up.
                    if ( valid )
                        metadataProcessor.process( new MetadataProcessorContext(), procMetadata );
                }
                catch ( MetadataProcessorInstantiationException e )
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch ( MetadataProcessorException e )
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            getLogger().info( "Adding '" + modelClass.getName() + "'" );

            String packageName = modelClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

            List list = (List) classes.get( packageName );

            if ( list == null )
            {
                list = new ArrayList();
            }

            list.add( modelClass );

            getLogger().info( "Added " + list.size() + " mapped classes for package '" + packageName + "'" );

            classes.put( packageName, list );
        }

        printWriter.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );

        writer.startElement( "entity-mappings" );
        writer.addAttribute( "xmlns", "http://java.sun.com/xml/ns/persistence/orm" );
        writer.addAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        writer
            .addAttribute( "xsi:schemaLocation",
                           "http://java.sun.com/xml/ns/persistence/orm http://java.sun.com/xml/ns/persistence/orm_1_0.xsd" );
        writer.addAttribute( "version", "1.0" );

        // Write out mappings for Entity classes
        for ( Iterator it = classes.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            List list = (List) entry.getValue();

            for ( Iterator it2 = list.iterator(); it2.hasNext(); )
            {
                ModelClass modelClass = (ModelClass) it2.next();

                writeClass( writer, modelClass );
            }
        }

        writer.endElement(); // close root element

        printWriter.println();

        printWriter.close();

    }

    /**
     * Writes out the mapping definition for an Entity class.
     * 
     * @param writer {@link XMLWriter} instance write out ORM mapping elements.
     * @param modelClass
     * @throws ModelloException if there was error writing out the mapping
     *             definition for a class.
     */
    private void writeClass( XMLWriter writer, ModelClass modelClass )
        throws ModelloException
    {
        writer.startElement( "entity" );

        writer.addAttribute( "class", modelClass.getPackageName() + "." + modelClass.getName() );

        writer.addAttribute( "access", "property" );

        writer.addAttribute( "metadata-complete", "true" );

        // TODO: process fields here.

        writer.endElement(); // close entity
    }
}
