package org.codehaus.modello.plugin.jpox;

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

import java.io.FileReader;
import java.io.File;
import java.util.Properties;

import org.codehaus.modello.ModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JPoxStoreModelloGeneratorTest
    extends ModelloGeneratorTest
{
    public JPoxStoreModelloGeneratorTest()
    {
        super( "jpox-store" );
    }

    public void testSimpleInvocation()
        throws Exception
    {
        ModelloCore core = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = core.loadModel( new FileReader( getTestPath( "src/test/resources/mergere-tissue.mdo" ) ) );

        // ----------------------------------------------------------------------
        // Generate the code
        // ----------------------------------------------------------------------

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getGeneratedSources().getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.FALSE.toString() );

        core.generate( model, "jpox-store", parameters );

        // ----------------------------------------------------------------------
        // Assert
        // ----------------------------------------------------------------------

        assertGeneratedFileExists( "org/mergere/tissue/TissueJPoxStore.java" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void assertGeneratedFileExists( String filename )
    {
        File file = new File( getGeneratedSources(), filename );

        assertTrue( "Missing generated file: " + file.getAbsolutePath(), file.canRead() );

        assertTrue( "The generated file is empty.", file.length() > 0 );
    }
}
