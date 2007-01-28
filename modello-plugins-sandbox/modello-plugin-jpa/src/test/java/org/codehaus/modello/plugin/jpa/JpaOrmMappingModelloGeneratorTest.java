package org.codehaus.modello.plugin.jpa;

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

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;

import java.io.FileReader;
import java.util.Properties;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id: JpaOrmMappingModelloGeneratorTest.java 780 2007-01-11 19:09:14Z
 *          rahul $
 * @since 1.0.0
 */
public class JpaOrmMappingModelloGeneratorTest extends AbstractModelloGeneratorTest
{

    /**
     * @param arg0
     */
    public JpaOrmMappingModelloGeneratorTest()
    {
        super( "jpa-mapping" );
    }

    public void testOrmMappingGeneration() throws Exception
    {

        ModelloCore core = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model =
            core.loadModel( new FileReader( getTestFile( getBasedir(), "src/test/resources/continuum-jpa.xml" ) ) );

        Properties parameters = new Properties();

        // parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, "target/output" );
        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getGeneratedSources().getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.FALSE.toString() );

        core.generate( model, "jpa-mapping", parameters );

        assertGeneratedFileExists( "orm.xml" );

    }
}
