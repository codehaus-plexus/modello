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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.compiler.Compiler;
import org.codehaus.plexus.compiler.CompilerError;
import org.codehaus.plexus.compiler.javac.JavacCompiler;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class ModelloGeneratorTest
    extends ModelloTest
{
    private List dependencies = new ArrayList();

    private String name;

    private File mavenRepoLocal;

    private List urls = new ArrayList();

    protected ModelloGeneratorTest( String name )
    {
        this.name = name;
    }

    public final void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( getTestPath( "target/" + getName() ) );

        String repo = System.getProperty( "maven.repo.local" );

        assertNotNull( "Missing system property: maven.repo.local", repo );

        mavenRepoLocal = new File( repo );

        addDependency( "junit", "junit", "3.8.1" );

        addDependency( "plexus", "plexus-utils", "1.0-alpha-1-SNAPSHOT" );
    }

    public void addDependency( String groupId, String artifactId, String version )
        throws Exception
    {
        File dependency = new File( mavenRepoLocal, groupId + "/jars/" + artifactId + "-" + version + ".jar" );

        assertTrue( "Cant find dependency: " + dependency, dependency.isFile() );

        dependencies.add( dependency );

        addClassPathFile( dependency );
    }

    public String getName()
    {
        return name;
    }

    public List getClasspath()
    {
        return dependencies;
    }

    protected void compile( File generatedSources, File destinationDirectory )
        throws Exception
    {
        String[] classPathElements = new String[dependencies.size() + 2];

        classPathElements[0] = getTestPath( "target/classes" );

        classPathElements[1] = getTestPath( "target/test-classes" );

        for ( int i = 0; i < dependencies.size(); i++ )
        {
            classPathElements[i + 2] = ((File) dependencies.get( i )).getAbsolutePath();
        }

        String[] sourceDirectories = new String[]{
            getTestPath( "src/test/verifiers/" + getName() ),
            generatedSources.getAbsolutePath()
        };

        Compiler compiler = new JavacCompiler();

        List messages = compiler.compile( classPathElements, sourceDirectories, destinationDirectory.getAbsolutePath() );

        for ( Iterator it = messages.iterator(); it.hasNext(); )
        {
            CompilerError message = (CompilerError) it.next();

            System.out.println( message.getFile() + "[" + message.getStartLine() + "," + message.getStartColumn() + "]: " + message.getMessage() );
        }

        assertEquals( "There was compilation errors.", 0, messages.size() );
    }

    protected void verify( String className, String testName )
        throws Throwable
    {
        // TODO: flip back to getTestFile() when plexus has File getTestFile()
        addClassPathFile( new File( getTestPath( "target/" + getName() + "/classes" ) ) );

        addClassPathFile( new File( getTestPath( "target/classes" ) ) );

        addClassPathFile( new File( getTestPath( "target/test-classes" ) ) );

        URLClassLoader classLoader = URLClassLoader.newInstance( (URL[]) urls.toArray( new URL[ urls.size() ] ), Thread.currentThread().getContextClassLoader() );

        Class clazz = classLoader.loadClass( className );

        Method verify = clazz.getMethod( "verify", new Class[0] );

        if ( false )
        {
            printClasspath( classLoader );
        }

        try
        {
            verify.invoke( clazz.newInstance(), new Object[0] );
        }
        catch( InvocationTargetException ex )
        {
            throw ex.getCause();
        }
    }

    protected void addClassPathFile( File file )
        throws Exception
    {
        assertTrue( "File doesn't exists: " + file.getAbsolutePath(), file.exists() );

        urls.add( file.toURL() );
    }

    protected void printClasspath( URLClassLoader classLoader )
    {
        URL[] urls = classLoader.getURLs();

        for ( int i = 0; i < urls.length; i++ )
        {
            URL url = urls[i];

            System.out.println( url );
        }
    }
}
