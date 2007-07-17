package org.codehaus.modello.plugin.converters;

/*
 * Copyright (c) 2006, Codehaus.org
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
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.File;
import java.util.Properties;

/**
 * @version $Id: JavaGeneratorTest.java 555 2006-01-29 21:38:08Z jvanzyl $
 */
public class ConverterGeneratorTest
    extends AbstractModelloGeneratorTest
{
    private String modelFile = "src/test/resources/models/maven.mdo";

    public ConverterGeneratorTest()
    {
        super( "converters" );
    }

    private File generatedSources;

    private File classes;

    public void testConverterGenerator()
        throws Throwable
    {
        generatedSources = getTestFile( "target/" + getName() + "/sources" );

        classes = getTestFile( "target/" + getName() + "/classes" );

        FileUtils.deleteDirectory( generatedSources );

        generatedSources.mkdirs();

        classes.mkdirs();

        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, generatedSources.getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.ALL_VERSIONS, "3.0.0,4.0.0" );

        Model model = modello.loadModel( ReaderFactory.newXmlReader( getTestFile( modelFile ) ) );

        generateClasses( parameters, modello, model, "java" );

        generateClasses( parameters, modello, model, "stax-reader" );

        generateClasses( parameters, modello, model, "stax-writer" );

        generateClasses( parameters, modello, model, "converters" );

        addDependency( "stax", "stax-api", "1.0.1" );
        addDependency( "net.java.dev.stax-utils", "stax-utils", "20060502" );
        addDependency( "org.codehaus.woodstox", "wstx-asl", "3.2.0" );

        compile( generatedSources, classes );

        verify( "ConvertersVerifier", "converters" );
    }

    private void generateClasses( Properties parameters, ModelloCore modello, Model model, String t )
        throws ModelloException
    {
        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );
        modello.generate( model, t, parameters );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( true ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, "3.0.0" );
        modello.generate( model, t, parameters );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( true ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );
        modello.generate( model, t, parameters );
    }
}
