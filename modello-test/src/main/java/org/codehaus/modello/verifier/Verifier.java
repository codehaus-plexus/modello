package org.codehaus.modello.verifier;

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
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;

import junit.framework.Assert;

import org.codehaus.plexus.util.ReaderFactory;

public abstract class Verifier
    extends Assert
{
    public abstract void verify()
        throws Throwable;

    protected File getTestFile( String name )
    {
        String basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );

        return new File( basedir, name );
    }

    protected String getTestPath( String name )
    {
        String basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );

        return new File( basedir, name ).getAbsolutePath();
    }

    protected Reader getXmlResourceReader( String name )
        throws IOException
    {
        return ReaderFactory.newXmlReader( getClass().getResourceAsStream( name ) );
    }

    protected void assertReader( Class<?> reader, Class<?> model, Class<?> input, Class<?> exception )
    {
        Method read;

        // Model read( InputStream|Reader ) throws IOException, ?
        try
        {
            read = reader.getMethod( "read", input );

            assertEquals( "Bad return type of " + read, model, read.getReturnType() );

            for ( Class<?> e : read.getExceptionTypes() )
            {
                assertTrue( "Unexpected exception " + e.getName() + " at " + read, IOException.class.equals( e )
                    || exception.equals( e ) );
            }
        }
        catch ( NoSuchMethodException e )
        {
            fail( e.toString() );
        }

        // Model read( InputStream|Reader, boolean ) throws IOException, ?
        try
        {
            read = reader.getMethod( "read", input, Boolean.TYPE );

            assertEquals( "Bad return type of " + read, model, read.getReturnType() );

            for ( Class<?> e : read.getExceptionTypes() )
            {
                assertTrue( "Unexpected exception " + e.getName() + " at " + read, IOException.class.equals( e )
                    || exception.equals( e ) );
            }
        }
        catch ( NoSuchMethodException e )
        {
            fail( e.toString() );
        }
    }

    protected void assertWriter( Class<?> writer, Class<?> model, Class<?> output, Class<?> exception )
    {
        Method write;

        // write( OutputStream|Writer, Model ) throws IOException, ?
        try
        {
            write = writer.getMethod( "write", output, model );

            for ( Class<?> e : write.getExceptionTypes() )
            {
                assertTrue( "Unexpected exception " + e.getName() + " at " + write, IOException.class.equals( e )
                    || exception.equals( e ) );
            }
        }
        catch ( NoSuchMethodException e )
        {
            fail( e.toString() );
        }
    }

}
