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

import java.io.Reader;
import java.util.Properties;

/**
 */
public class ConverterGeneratorTest
    extends AbstractModelloJavaGeneratorTest
{
    public ConverterGeneratorTest()
    {
        super( "converters" );
    }

    public void testConverterGenerator()
        throws Throwable
    {
        generateConverterClasses( getXmlResourceReader( "/models/maven.mdo" ), "3.0.0", "4.0.0" );

        generateConverterClasses( getXmlResourceReader( "/features.mdo" ), "1.0.0", "1.1.0" );

        addDependency( "stax", "stax-api" );
        addDependency( "org.codehaus.woodstox", "wstx-asl" );

        compileGeneratedSources();

        verifyCompiledGeneratedSources( "ConvertersVerifier" );
    }

    private void generateConverterClasses( Reader modelReader, String fromVersion, String toVersion )
        throws Throwable
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( modelReader );

        Properties parameters = new Properties();
        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getOutputDirectory().getAbsolutePath() );
        parameters.setProperty( ModelloParameterConstants.ALL_VERSIONS, fromVersion + "," + toVersion );

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
