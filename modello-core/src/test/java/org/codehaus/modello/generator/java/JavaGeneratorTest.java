package org.codehaus.modello.generator.java;

/*
 * LICENSE
 */

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.codehaus.modello.FileUtils;
import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloTestCase;
import org.codehaus.plexus.compiler.Compiler;
import org.codehaus.plexus.compiler.CompilerError;
import org.codehaus.plexus.compiler.javac.IsolatedClassLoader;
import org.codehaus.plexus.compiler.javac.JavacCompiler;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JavaGeneratorTest
    extends ModelloTestCase
{
    private String modelFile = "model.xml";

    public void testJavaGenerator()
        throws Exception
    {
        File sourceDirectory = getTestFile( "target/java-generator/source" );

        File outputDirectory = getTestFile( "target/java-generator/output" );

        FileUtils.deleteDirectory( sourceDirectory );

        FileUtils.deleteDirectory( outputDirectory );

        sourceDirectory.mkdirs();

        outputDirectory.mkdirs();

        Modello modello = getModello();

        modello.work( modelFile, "java", sourceDirectory, "4.0.0", false );

        compile( sourceDirectory, getTestFile( "src/test/verifiers" ), outputDirectory );

        IsolatedClassLoader classLoader = new IsolatedClassLoader();

        classLoader.addURL( outputDirectory.toURL() );

        classLoader.addURL( getTestFile( "target/classes" ).toURL() );

        classLoader.addURL( getTestFile( "target/test-classes" ).toURL() );

        Class clazz = classLoader.loadClass( "org.codehaus.modello.generator.java.JavaVerifier" );

        Method verify = clazz.getMethod( "verify", new Class[0] );

        verify.invoke( clazz.newInstance(), new Object[0] );
    }

    private void compile( File generatedSources, File verifierSources, File destinationDirectory )
        throws Exception
    {
        String[] classPathElements = new String[]{
            getTestPath( "target/classes" ),
            getTestPath( "target/test-classes" )
        };

        String[] sourceDirectories = new String[]{ 
            verifierSources.getAbsolutePath(),
            generatedSources.getAbsolutePath()
        };

        Compiler compiler = new JavacCompiler();

        List messages = compiler.compile( classPathElements, sourceDirectories, destinationDirectory.getAbsolutePath() );

        for ( Iterator it = messages.iterator(); it.hasNext(); )
        {
            CompilerError message = (CompilerError) it.next();

            System.out.println( message.getMessage() );
        }

        assertEquals( "There was compilation errors.", 0, messages.size() );
    }
}
