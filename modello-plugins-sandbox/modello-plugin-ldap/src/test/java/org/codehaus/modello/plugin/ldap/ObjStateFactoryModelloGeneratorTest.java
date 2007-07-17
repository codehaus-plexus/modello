package org.codehaus.modello.plugin.ldap;

/*
 * Copyright (c) 2005, Codehaus.org
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

import org.codehaus.modello.ModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ObjStateFactoryModelloGeneratorTest
    extends ModelloGeneratorTest
{
    public ObjStateFactoryModelloGeneratorTest()
    {
        super( "obj-state-factory" );
    }

    public void testBasicSchemaGeneration()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( ReaderFactory.newXmlReader( getTestFile( "src/test/models/simple.mdo" ) ) );


        // ----------------------------------------------------------------------
        // Execute
        // ----------------------------------------------------------------------

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getGeneratedSources().getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );

        modello.generate( model, "obj-state-factory", parameters );

        // ----------------------------------------------------------------------
        // Assert the generated schema
        // ----------------------------------------------------------------------

        dumpFile( "foo/bar/io/ldap/BoyObjStateFactory.java" );

        dumpFile( "foo/bar/io/ldap/GirlObjStateFactory.java" );
    }

    private void dumpFile( String name )
        throws IOException
    {
        File output = new File( getGeneratedSources(), name );

        assertTrue( "Missing expected generated output: " + output.getAbsolutePath(), output.exists() );

        BufferedReader reader = new BufferedReader( new FileReader( output ) );

        String line;

        while ( ( line = reader.readLine() ) != null )
        {
            System.out.println( line );
        }
    }
}
