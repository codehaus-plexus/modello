package org.codehaus.modello.plugin.registry;

/*
 * Copyright (c) 2007, Codehaus.org
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

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: Xpp3GeneratorTest.java 675 2006-11-16 10:58:59Z brett $
 */
public class RegistryReaderGeneratorTest
    extends AbstractModelloGeneratorTest
{
    public RegistryReaderGeneratorTest()
    {
        super( "registry-reader" );
    }

    public void testRegistryReader()
        throws Throwable
    {
        ModelloCore modello = (ModelloCore) container.lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( new FileReader( getTestPath( "src/test/resources/model.mdo" ) ) );

        File generatedSources = new File( getTestPath( "target/registry-reader/sources" ) );

        File classes = new File( getTestPath( "target/registry-reader/classes" ) );

        FileUtils.deleteDirectory( generatedSources );

        FileUtils.deleteDirectory( classes );

        generatedSources.mkdirs();

        classes.mkdirs();

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, generatedSources.getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );

        modello.generate( model, "java", parameters );

        modello.generate( model, "registry-reader", parameters );

        Properties properties = new Properties( System.getProperties() );
        if ( properties.getProperty( "version" ) == null )
        {
            properties.load(
                getClass().getResourceAsStream( "/META-INF/maven/org.codehaus.modello/modello-core/pom.properties" ) );
        }
        addDependency( "org.codehaus.modello", "modello-core", properties.getProperty( "version" ) );
        addDependency( "org.codehaus.plexus", "plexus-registry", "1.0-SNAPSHOT" );
        addDependency( "org.codehaus.plexus", "plexus-component-api", "1.0-alpha-16" );
        addDependency( "org.codehaus.plexus", "plexus-container-default", "1.0-alpha-16" );
        addDependency( "commons-collections", "commons-collections", "3.1" );
        addDependency( "commons-configuration", "commons-configuration", "1.3" );
        addDependency( "commons-lang", "commons-lang", "2.1" );
        addDependency( "commons-logging", "commons-logging-api", "1.0.4" );

        compile( generatedSources, classes );

        verify( "org.codehaus.modello.plugin.registry.RegistryReaderVerifier", "registry-reader" );
    }
}
