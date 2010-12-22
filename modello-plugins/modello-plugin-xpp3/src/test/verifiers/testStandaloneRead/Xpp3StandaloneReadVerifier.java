package org.codehaus.modello.generator.xml.xpp3;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;

import junit.framework.Assert;

import org.codehaus.modello.generator.xml.xpp3.test.standaloneread.RootClass;
import org.codehaus.modello.generator.xml.xpp3.test.standaloneread.StandaloneReadClass;

import org.codehaus.modello.generator.xml.xpp3.test.standaloneread.io.xpp3.StandaloneReadXpp3Reader;

import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class Xpp3StandaloneReadVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        StandaloneReadXpp3Reader reader = new StandaloneReadXpp3Reader();

        String fileName = "src/test/verifiers/testStandaloneRead/rootClass.xml";
        File file = new File( fileName );
        assertTrue( "File does not exist:" + fileName, file.exists() );

        InputStream in = new FileInputStream( file );
        assertNotNull( "Expected not null input stream", in );
        RootClass rootClass = reader.read( in );
        assertNotNull( "Expected not null RootClass", rootClass );

        in = new FileInputStream( file );
        assertNotNull( "Expected not null input stream", in );
        rootClass = reader.read( in, true );
        assertNotNull( "Expected not null RootClass", rootClass );

        Reader fr = ReaderFactory.newXmlReader( file );
        rootClass = reader.read( fr );
        assertNotNull( "Expected not null RootClass", rootClass );

        fr = ReaderFactory.newXmlReader( file );
        rootClass = reader.read( fr, true );
        assertNotNull( "Expected not null RootClass", rootClass );

        fileName = "src/test/verifiers/testStandaloneRead/standaloneReadClass.xml";
        file = new File( fileName );
        assertTrue( "File does not exist:" + fileName, file.exists() );

        in = new FileInputStream( file );
        assertNotNull( "Expected not null input stream", in );
        StandaloneReadClass standaloneReadClass = reader.readStandaloneReadClass( in );
        assertNotNull( "Expected not null StandaloneReadClass", standaloneReadClass );

        in = new FileInputStream( file );
        assertNotNull( "Expected not null input stream", in );
        standaloneReadClass = reader.readStandaloneReadClass( in, true );
        assertNotNull( "Expected not null StandaloneReadClass", standaloneReadClass );

        fr = ReaderFactory.newXmlReader( file );
        standaloneReadClass = reader.readStandaloneReadClass( fr );
        assertNotNull( "Expected not null StandaloneReadClass", standaloneReadClass );

        fr = ReaderFactory.newXmlReader( file );
        standaloneReadClass = reader.readStandaloneReadClass( fr, true );
        assertNotNull( "Expected not null StandaloneReadClass", standaloneReadClass );

        String methodName = "readFooClass";
        for ( Method method : reader.getClass().getMethods() )
        {
            assertFalse( "Found method " + methodName, methodName.equals( method.getName() ) );
        }
    }
}
