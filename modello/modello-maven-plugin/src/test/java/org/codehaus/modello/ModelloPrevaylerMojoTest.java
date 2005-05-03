package org.codehaus.modello;

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
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloPrevaylerMojoTest
    extends PlexusTestCase
{
    public void testModelloPrevaylerMojo()
        throws Exception
    {
        ModelloCore modelloCore = (ModelloCore) lookup( ModelloCore.ROLE );

        ModelloPrevaylerMojo mojo = new ModelloPrevaylerMojo();

        File outputDirectory = getTestFile( "target/prevayler-test" );

        FileUtils.deleteDirectory( outputDirectory );

        // ----------------------------------------------------------------------
        // Call the mojo
        // ----------------------------------------------------------------------

        mojo.setOutputDirectory( outputDirectory );

        mojo.setModel( getTestPath( "src/test/resources/prevayler-model.mdo" ) );

        mojo.setVersion("1.0.0" );

        mojo.setPackageWithVersion( Boolean.FALSE );

        mojo.setModelloCore( modelloCore );

        mojo.execute();

        // ----------------------------------------------------------------------
        // Assert
        // ----------------------------------------------------------------------

        File store = new File( outputDirectory, "org/codehaus/mojo/modello/test/prevayler/PrevaylerTestPrevaylerStore.java" );

        assertTrue( "Coun't not read the prevayler store.", store.canRead() );
    }
}
