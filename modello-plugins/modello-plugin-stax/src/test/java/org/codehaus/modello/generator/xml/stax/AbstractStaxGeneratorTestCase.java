package org.codehaus.modello.generator.xml.stax;

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
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.Properties;

public abstract class AbstractStaxGeneratorTestCase
    extends AbstractModelloJavaGeneratorTest
{
    protected ModelloCore modello;

    protected AbstractStaxGeneratorTestCase( String name )
    {
        super( name );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        modello = (ModelloCore) container.lookup( ModelloCore.ROLE );
    }

    protected void verifyModel( Model model, String className )
        throws IOException, ModelloException, CompilerException
    {
        verifyModel( model, className, null );
    }

    protected void verifyModel( Model model, String className, String[] versions )
        throws IOException, ModelloException, CompilerException
    {
        Properties parameters = new Properties();
        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getOutputDirectory().getAbsolutePath() );
        parameters.setProperty( ModelloParameterConstants.VERSION, "4.0.0" );
        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );

        modello.generate( model, "java", parameters );
        modello.generate( model, "stax-writer", parameters );
        modello.generate( model, "stax-reader", parameters );

        if ( versions != null && versions.length > 0 )
        {
            parameters.setProperty( ModelloParameterConstants.ALL_VERSIONS, StringUtils.join( versions, "," ) );

            for ( int i = 0; i < versions.length; i++ )
            {
                parameters.setProperty( ModelloParameterConstants.VERSION, versions[i] );
                parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( true ) );

                modello.generate( model, "java", parameters );
                modello.generate( model, "stax-writer", parameters );
                modello.generate( model, "stax-reader", parameters );
            }
        }

        addDependency( "net.java.dev.stax-utils", "stax-utils", "20060502" );
        addDependency( "stax", "stax-api", "1.0.1" );
        addDependency( "org.codehaus.woodstox", "wstx-asl", "3.2.0" );

        compile( getOutputDirectory(), getOutputClasses() );

        verify( className, getName() );
    }
}
