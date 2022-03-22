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

import org.codehaus.modello.verifier.VerifierException;
import org.codehaus.plexus.compiler.Compiler;
import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.CompilerMessage;
import org.codehaus.plexus.compiler.CompilerResult;
import org.codehaus.plexus.compiler.javac.JavacCompiler;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Base class for unit-tests of Modello plugins that generate java code.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @see #compileGeneratedSources() compileGeneratedSources() method to compile generated sources
 * @see #verifyCompiledGeneratedSources(String) verifyCompiledGeneratedSources(String) method to run a Verifier
 *      class against compiled generated code
 * @see org.codehaus.modello.verifier.Verifier Verifier base class for verifiers
 */
public abstract class AbstractModelloJavaGeneratorTest
    extends AbstractModelloGeneratorTest
{
    private List<File> dependencies = new ArrayList<File>();

    private List<URL> urls = new ArrayList<URL>();

    private List<String> classPathElements = new ArrayList<String>();

    protected AbstractModelloJavaGeneratorTest( String name )
    {
        super( name );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( getOutputClasses() );

        assertTrue( getOutputClasses().mkdirs() );
    }

    protected File getOutputDirectory()
    {
        return new File( super.getOutputDirectory(), "sources" );
    }

    protected File getOutputClasses()
    {
        return new File( super.getOutputDirectory(), "classes" );
    }

    protected void addDependency( String groupId, String artifactId )
    {
        File dependencyFile = getDependencyFile( groupId, artifactId );

        dependencies.add( dependencyFile );

        addClassPathFile( dependencyFile );
    }

    protected File getDependencyFile( String groupId, String artifactId )
    {
        // NOTE: dependency version is managed by project POM and not selectable by test

        String libsDir = System.getProperty( "tests.lib.dir", "target/test-libs" );
        File dependencyFile = new File( libsDir, artifactId + ".jar" );

        assertTrue( "Can't find dependency: " + dependencyFile.getAbsolutePath(), dependencyFile.isFile() );

        return dependencyFile;
    }

    public List<File> getClasspath()
    {
        return dependencies;
    }

    protected String getModelloVersion()
        throws IOException
    {
        Properties properties = new Properties( System.getProperties() );

        if ( properties.getProperty( "version" ) == null )
        {
            InputStream is = getResourceAsStream( "/META-INF/maven/org.codehaus.modello/modello-test/pom.properties" );

            if ( is != null )
            {
                properties.load( is );
            }
        }

        return properties.getProperty( "version" );
    }

    protected void compileGeneratedSources()
        throws IOException, CompilerException
    {
        compileGeneratedSources( getName() );
    }

    protected void compileGeneratedSources( int minJavaSource )
                    throws IOException, CompilerException
    {
        compileGeneratedSources( getName(), minJavaSource );
    }

    protected void compileGeneratedSources( String verifierId )
        throws IOException, CompilerException
    {
        String runtimeVersion = System.getProperty( "java.specification.version" );
        if ( runtimeVersion.startsWith( "1." ) )
        {
            runtimeVersion = runtimeVersion.substring( 2 );
        }
        int runtimeSource = Integer.parseInt( runtimeVersion );
        
        String javaSource;
        if ( runtimeSource <= 8 )
        {
            javaSource = "1.3";
        }
        else if ( runtimeSource <= 11 )
        {
            javaSource = "6";
        }
        else 
        {
            javaSource = "7";
        }
        
        compileGeneratedSources( verifierId, javaSource );
    }

    protected void compileGeneratedSources( String verifierId, int minJavaSource )
        throws IOException, CompilerException
    {
        String runtimeVersion = System.getProperty( "java.specification.version" );
        if ( runtimeVersion.startsWith( "1." ) )
        {
            runtimeVersion = runtimeVersion.substring( 2 );
        }
        int runtimeSource = Integer.parseInt( runtimeVersion );
        
        String javaSource;
        if ( runtimeSource <= 8 )
        {
            javaSource = "1." + Math.max( minJavaSource, 3 );
        }
        else if ( runtimeSource <= 11 )
        {
            javaSource = Integer.toString( Math.max( minJavaSource, 6) );
        }
        else 
        {
            javaSource = Integer.toString( Math.max( minJavaSource, 7) );;
        }
        
        compileGeneratedSources( verifierId, javaSource );
    }
    
    private void compileGeneratedSources( String verifierId, String javaSource )
        throws IOException, CompilerException
    {
        File generatedSources = getOutputDirectory();
        File destinationDirectory = getOutputClasses();

        addDependency( "junit", "junit" );
        addDependency( "org.codehaus.plexus", "plexus-utils" );
        addDependency( "org.codehaus.modello", "modello-test" );

        String[] classPathElements = new String[dependencies.size() + 2];
        classPathElements[0] = getTestPath( "target/classes" );
        classPathElements[1] = getTestPath( "target/test-classes" );

        for ( int i = 0; i < dependencies.size(); i++ )
        {
            classPathElements[i + 2] = ( (File) dependencies.get( i ) ).getAbsolutePath();
        }

        File verifierDirectory = getTestFile( "src/test/verifiers/" + verifierId );
        String[] sourceDirectories;
        if ( verifierDirectory.canRead() )
        {
            sourceDirectories = new String[]{ verifierDirectory.getAbsolutePath(), generatedSources.getAbsolutePath() };
        }
        else
        {
            sourceDirectories = new String[]{ generatedSources.getAbsolutePath() };
        }

        Compiler compiler;
        try
        {
            compiler = lookup(Compiler.class, "javac");
        }
        catch (ComponentLookupException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }

        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.setClasspathEntries( Arrays.asList( classPathElements ) );
        configuration.setSourceLocations( Arrays.asList( sourceDirectories ) );
        configuration.setOutputLocation( destinationDirectory.getAbsolutePath() );
        configuration.setDebug( true );
        
        configuration.setSourceVersion( javaSource );
        configuration.setTargetVersion( javaSource );

        CompilerResult result = compiler.performCompile( configuration );

        List<CompilerMessage> errors = new ArrayList<CompilerMessage>( 0 );
        for ( CompilerMessage compilerMessage : result.getCompilerMessages() )
        {
            if ( compilerMessage.isError() )
            {
                errors.add( compilerMessage );
            }
        }

        assertEquals( "There was compilation errors: " + errors, 0, errors.size() );
    }

    /**
     * Run a verifier class in a classloader context where compiled generated sources are available
     *
     * @param verifierClassName the class name of the verifier class
     */
    protected void verifyCompiledGeneratedSources( String verifierClassName )
    {
        addClassPathFile( getOutputClasses() );

        addClassPathFile( getTestFile( "target/classes" ) );

        addClassPathFile( getTestFile( "target/test-classes" ) );

        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        URLClassLoader classLoader = URLClassLoader.newInstance( urls.toArray( new URL[urls.size()] ), null );

        Thread.currentThread().setContextClassLoader( classLoader );

        try
        {
            Class<?> clazz = classLoader.loadClass( verifierClassName );

            Method verify = clazz.getMethod( "verify", new Class[0] );

            try
            {
                verify.invoke( clazz.getDeclaredConstructor().newInstance(), new Object[0] );
            }
            catch ( InvocationTargetException ex )
            {
                throw ex.getCause();
            }
        }
        catch ( Throwable throwable )
        {
            throw new VerifierException( "Error verifying modello tests: " + throwable.getMessage(), throwable );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldCCL );
        }
    }

    protected void addClassPathFile( File file )
    {
        assertTrue( "File doesn't exists: " + file.getAbsolutePath(), file.exists() );

        try
        {
            urls.add( file.toURI().toURL() );
        }
        catch ( MalformedURLException e )
        {
            throw new RuntimeException( e );
        }

        classPathElements.add( file.getAbsolutePath() );
    }

    protected void printClasspath( URLClassLoader classLoader )
    {
        URL[] urls = classLoader.getURLs();

        for ( URL url : urls )
        {
            System.out.println( url );
        }
    }

    protected void assertGeneratedFileExists( String filename )
    {
        File file = new File( getOutputDirectory(), filename );

        assertTrue( "Missing generated file: " + file.getAbsolutePath(), file.canRead() );

        assertTrue( "The generated file is empty.", file.length() > 0 );
    }

    /**
     * Check if a Java 5 feature test should be skipped, since it is not supported by current test environment.
     *
     * @return <code>true</code> if Java 5 is not available, then feature test should be skipped by caller
     */
    protected boolean skipJava5FeatureTest()
    {
        String javaVersion = System.getProperty( "java.specification.version", "1.5" );

        if ( "1.5".compareTo( javaVersion ) > 0 )
        {
            System.out.println(
                "Skipped Java 5 feature test, not supported by current test environment (" + javaVersion + ")" );
            return true;
        }

        return false;
    }

    protected List<String> getClassPathElements()
    {
        return classPathElements;
    }
}
