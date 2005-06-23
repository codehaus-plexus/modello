package org.codehaus.modello.maven;

/*
 * Copyright (c) 2004, Codehaus.org
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

import java.io.File;

import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.maven.ModelloXFireMojo;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @version $Id$
 */
public class ModelloXFireMojoTest
    extends PlexusTestCase
{
    
    public void testNOTHING()
    {
        System.out.println( "I'm gelding this test until we're ready to release with xfire plugin intact." );
    }
/*
    public void testModelloJavaMojo()
        throws Exception
    {
        ModelloCore modelloCore = (ModelloCore) lookup( ModelloCore.ROLE );

        ModelloXFireMojo mojo = new ModelloXFireMojo();

        File outputDirectory = getTestFile( "target/xfire-test" );

        FileUtils.deleteDirectory( outputDirectory );

        // ----------------------------------------------------------------------
        // Call the mojo
        // ----------------------------------------------------------------------

        mojo.setOutputDirectory( outputDirectory );

        mojo.setModel( getTestPath( "src/test/resources/java-model.mdo" ) );

        mojo.setVersion( "1.0.0" );

        mojo.setPackageWithVersion( Boolean.TRUE );

        mojo.setModelloCore( modelloCore );

        mojo.execute();

        // ----------------------------------------------------------------------
        // Assert
        // ----------------------------------------------------------------------

        File javaFile = new File( outputDirectory, "org/codehaus/mojo/modello/javatest/v1_0_0/Model.aegis.xml" );

        assertTrue( "The generated java file doesn't exist: '" + javaFile.getAbsolutePath() + "'.", javaFile.exists() );
    }
*/
}
