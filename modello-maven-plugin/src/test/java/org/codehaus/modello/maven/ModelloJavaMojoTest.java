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

import org.codehaus.modello.core.ModelloCore;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.Arrays;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ModelloJavaMojoTest
    extends PlexusTestCase
{
    public void testModelloJavaMojo()
        throws Exception
    {
        ModelloCore modelloCore = (ModelloCore) lookup( ModelloCore.ROLE );

        BuildContext buildContext = (BuildContext) lookup( BuildContext.class );

        ModelloJavaMojo mojo = new ModelloJavaMojo();

        File outputDirectory = getTestFile( "target/java-test" );

        FileUtils.deleteDirectory( outputDirectory );

        // ----------------------------------------------------------------------
        // Call the mojo
        // ----------------------------------------------------------------------

        mojo.setOutputDirectory( outputDirectory );

        String models[] = new String[1];
        models[0] = getTestPath( "src/test/resources/java-model.mdo" );
        mojo.setModels( models );

        mojo.setVersion( "1.0.0" );

        mojo.setPackageWithVersion( false );

        mojo.setPackagedVersions( Arrays.asList( new String[] { "0.9.0", "1.0.0" } ) );

        mojo.setModelloCore( modelloCore );
        
        mojo.setBuildContext( buildContext );

        mojo.execute();

        // ----------------------------------------------------------------------
        // Assert
        // ----------------------------------------------------------------------

        File javaFile = new File( outputDirectory, "org/codehaus/mojo/modello/javatest/Model.java" );

        assertTrue( "The generated java file doesn't exist: '" + javaFile.getAbsolutePath() + "'.", javaFile.exists() );

        javaFile = new File( outputDirectory, "org/codehaus/mojo/modello/javatest/NewModel.java" );

        assertTrue( "The generated java file doesn't exist: '" + javaFile.getAbsolutePath() + "'.", javaFile.exists() );

        javaFile = new File( outputDirectory, "org/codehaus/mojo/modello/javatest/v1_0_0/Model.java" );

        assertTrue( "The generated java file doesn't exist: '" + javaFile.getAbsolutePath() + "'.", javaFile.exists() );

        javaFile = new File( outputDirectory, "org/codehaus/mojo/modello/javatest/v1_0_0/NewModel.java" );

        assertTrue( "The generated java file doesn't exist: '" + javaFile.getAbsolutePath() + "'.", javaFile.exists() );

        javaFile = new File( outputDirectory, "org/codehaus/mojo/modello/javatest/v0_9_0/Model.java" );

        assertTrue( "The generated java file doesn't exist: '" + javaFile.getAbsolutePath() + "'.", javaFile.exists() );

        javaFile = new File( outputDirectory, "org/codehaus/mojo/modello/javatest/v0_9_0/NewModel.java" );

        assertFalse( "The generated java file shouldn't exist: '" + javaFile.getAbsolutePath() + "'.",
                     javaFile.exists() );
    }
}
