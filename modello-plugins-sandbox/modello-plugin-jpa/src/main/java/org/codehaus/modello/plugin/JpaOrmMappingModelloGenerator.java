package org.codehaus.modello.plugin;

/**
 * Copyright 2007-2008 The Apache Software Foundation.
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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

        // TODO: Prepare classes to be mapped here 

        printWriter.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );

        writer.startElement( "entity-mappings" );
        writer.addAttribute( "xmlns", "http://java.sun.com/xml/ns/persistence/orm" );
        writer.addAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        writer
            .addAttribute( "xsi:schemaLocation",
                           "http://java.sun.com/xml/ns/persistence/orm http://java.sun.com/xml/ns/persistence/orm_1_0.xsd" );
        writer.addAttribute( "version", "1.0" );

        // TODO: Write out mappings for classes

        writer.endElement(); // close root element

        printWriter.println();

        printWriter.close();

    }

}
