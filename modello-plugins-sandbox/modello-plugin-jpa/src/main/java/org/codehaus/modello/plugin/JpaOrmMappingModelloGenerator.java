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

import java.io.File;
import java.io.IOException;
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
public class JpaOrmMappingModelloGenerator extends AbstractModelloGenerator
{

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.ModelloGenerator#generate(org.codehaus.modello.model.Model,
     *      java.util.Properties)
     */
    public void generate( Model model, Properties properties ) throws ModelloException
    {
        initialize( model, properties );

        String fileName = properties.getProperty( ModelloParameterConstants.FILENAME, "orm.xml" );

        File directory = getOutputDirectory();
        File orm = new File( directory, fileName );

        File parent = orm.getParentFile();

        if ( !parent.exists() )
        {
            if ( !parent.mkdirs() )
            {
                throw new ModelloException( "Error while creating parent directories for the file " + "'"
                                + orm.getAbsolutePath() + "'." );
            }
        }

        // generateOrm( orm, model );

    }

}
