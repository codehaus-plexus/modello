package org.codehaus.modello.generator.xml.xpp3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Properties;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;


/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 27 juil. 2008
 * @version $Id$
 */
public class Xpp3ElementWithAttAndContentTest
    extends AbstractModelloGeneratorTest
{
    public Xpp3ElementWithAttAndContentTest()
    {
        super( "xpp3-changes" );
    }

    public void testModelGeneration()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) container.lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( ReaderFactory.newXmlReader( getTestFile( "src/test/resources/changes.mdo" ) ) );

        File generatedSources = new File( getTestPath( "target/xpp3-changes/sources" ) );

        File classes = new File( getTestPath( "target/xpp3-changes/classes" ) );

        FileUtils.deleteDirectory( generatedSources );

        FileUtils.deleteDirectory( classes );

        generatedSources.mkdirs();

        classes.mkdirs();

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, generatedSources.getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );

        modello.generate( model, "java", parameters );

        modello.generate( model, "xpp3-writer", parameters );

        modello.generate( model, "xpp3-reader", parameters );

        addDependency( "org.codehaus.modello", "modello-core", getModelloVersion() );

        compile( generatedSources, classes );

        verify( "org.codehaus.modello.generator.xml.xpp3.Xpp3ChangesVerifier", "xpp3-changes" );


    }
}
