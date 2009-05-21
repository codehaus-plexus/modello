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

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.Reader;
import java.util.Properties;

/**
 * @version $Id$
 */
public class ConverterGeneratorTest
    extends AbstractModelloJavaGeneratorTest
{
    private static final String MAVEN_MODEL_FILE = "src/test/resources/models/maven.mdo";

    public ConverterGeneratorTest()
    {
        super( "converters" );
    }

    public void testConverterGenerator()
        throws Throwable
    {
        generateConverterClasses( ReaderFactory.newXmlReader( getTestFile( MAVEN_MODEL_FILE ) ), "3.0.0", "4.0.0" );

        addDependency( "stax", "stax-api", "1.0.1" );
        addDependency( "net.java.dev.stax-utils", "stax-utils", "20060502" );
        addDependency( "org.codehaus.woodstox", "wstx-asl", "3.2.0" );

        compile( getOutputDirectory(), getOutputClasses() );

        verify( "ConvertersVerifier", "converters" );
    }

    private void generateConverterClasses( Reader modelReader, String fromVersion, String toVersion )
        throws Throwable
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Properties parameters = new Properties();
        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getOutputDirectory().getAbsolutePath() );
        parameters.setProperty( ModelloParameterConstants.ALL_VERSIONS, fromVersion + "," + toVersion );

        Model model = modello.loadModel( modelReader );

        generateClasses( parameters, modello, model, fromVersion, toVersion, "java" );
        generateClasses( parameters, modello, model, fromVersion, toVersion, "stax-reader" );
        generateClasses( parameters, modello, model, fromVersion, toVersion, "stax-writer" );
        generateClasses( parameters, modello, model, fromVersion, toVersion, "converters" );
    }

    private void generateClasses( Properties parameters, ModelloCore modello, Model model, String fromVersion,
                                  String toVersion, String outputType )
        throws ModelloException
    {
        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, toVersion );
        modello.generate( model, outputType, parameters );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( true ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, fromVersion );
        modello.generate( model, outputType, parameters );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( true ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, toVersion );
        modello.generate( model, outputType, parameters );
    }
}
