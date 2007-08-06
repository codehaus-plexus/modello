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
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelValidationException;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public abstract class AbstractRegistryGeneratorTestCase
    extends AbstractModelloGeneratorTest
{
    public AbstractRegistryGeneratorTestCase( String name )
    {
        super( name );
    }

    protected void prepareTest( String outputType )
        throws ComponentLookupException, ModelloException, ModelValidationException, IOException, CompilerException
    {
        ModelloCore modello = (ModelloCore) container.lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( ReaderFactory.newXmlReader( getTestFile( "src/test/resources/model.mdo" ) ) );

        File generatedSources = getTestFile( "target/" + outputType + "/sources" );

        File classes = getTestFile( "target/" + outputType + "/classes" );

        FileUtils.deleteDirectory( generatedSources );

        FileUtils.deleteDirectory( classes );

        generatedSources.mkdirs();

        classes.mkdirs();

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, generatedSources.getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );

        modello.generate( model, "java", parameters );

        modello.generate( model, outputType, parameters );

        addDependency( "org.codehaus.modello", "modello-core", getModelloVersion() );
        addDependency( "org.codehaus.plexus.registry", "plexus-registry-api", "1.0-alpha-2" );
        addDependency( "org.codehaus.plexus.registry", "plexus-registry-commons", "1.0-alpha-2" );
        addDependency( "org.codehaus.plexus", "plexus-container-default", "1.0-alpha-30" );
        addDependency( "commons-collections", "commons-collections", "3.1" );
        addDependency( "commons-configuration", "commons-configuration", "1.3" );
        addDependency( "commons-lang", "commons-lang", "2.1" );
        addDependency( "commons-logging", "commons-logging-api", "1.0.4" );

        compile( generatedSources, classes );
    }
}
