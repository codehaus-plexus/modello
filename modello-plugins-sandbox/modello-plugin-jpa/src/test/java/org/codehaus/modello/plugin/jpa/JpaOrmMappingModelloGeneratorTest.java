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

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id: JpaOrmMappingModelloGeneratorTest.java 780 2007-01-11 19:09:14Z
 *          rahul $
 * @since 1.0.0
 */
public class JpaOrmMappingModelloGeneratorTest
    extends AbstractModelloGeneratorTest
{

    private static final String ORM_XML = "orm.xml";

    /**
     * @param arg0
     */
    public JpaOrmMappingModelloGeneratorTest()
    {
        super( "jpa-mapping" );
    }

    public void testOrmMappingGeneration()
        throws Exception
    {

        ModelloCore core = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = core
            .loadModel( new FileReader( getTestFile( getBasedir(), "src/test/resources/continuum-jpa.xml" ) ) );

        Properties parameters = new Properties();

        // parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY,
        // "target/output" );
        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getGeneratedSources().getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.1.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.FALSE.toString() );

        core.generate( model, "jpa-mapping", parameters );

        assertGeneratedFileExists( ORM_XML );

        // verify structure of generated ORM
        SAXReader reader = new SAXReader();
        Document ormDoc = reader.read( new File( "target/" + getName() + "/" + ORM_XML ) );
        assertNotNull( ormDoc );

        Element rootElement = ormDoc.getRootElement();
        assertNotNull( rootElement );
        assertEquals( "entity-mappings", rootElement.getName() );

        // TODO: Use Dom4jUtils to verify.
        XPath expression = DocumentHelper.createXPath( "/entity-mappings" );
        List nodes = expression.selectNodes( ormDoc );
        assertNotNull( nodes );
        assertEquals( 1, nodes.size() );
    }
}
