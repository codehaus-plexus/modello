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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;

import org.codehaus.modello.generator.xml.xpp3.test.standaloneread.RootClass;
import org.codehaus.modello.generator.xml.xpp3.test.standaloneread.StandaloneReadClass;

import org.codehaus.modello.generator.xml.xpp3.test.standaloneread.io.xpp3.StandaloneReadXpp3Reader;

import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.util.xml.XmlStreamReader;
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
        assertTrue( file.exists() , "File does not exist:" + fileName);

        InputStream in = new FileInputStream( file );
        assertNotNull( in , "Expected not null input stream");
        RootClass rootClass = reader.read( in );
        assertNotNull( rootClass , "Expected not null RootClass");

        in = new FileInputStream( file );
        assertNotNull( in , "Expected not null input stream");
        rootClass = reader.read( in, true );
        assertNotNull( rootClass , "Expected not null RootClass");

        Reader fr = new XmlStreamReader( file );
        rootClass = reader.read( fr );
        assertNotNull( rootClass , "Expected not null RootClass");

        fr = new XmlStreamReader( file );
        rootClass = reader.read( fr, true );
        assertNotNull( rootClass , "Expected not null RootClass");

        fileName = "src/test/verifiers/testStandaloneRead/standaloneReadClass.xml";
        file = new File( fileName );
        assertTrue( file.exists() , "File does not exist:" + fileName);

        in = new FileInputStream( file );
        assertNotNull( in , "Expected not null input stream");
        StandaloneReadClass standaloneReadClass = reader.readStandaloneReadClass( in );
        assertNotNull( standaloneReadClass , "Expected not null StandaloneReadClass");

        in = new FileInputStream( file );
        assertNotNull( in , "Expected not null input stream");
        standaloneReadClass = reader.readStandaloneReadClass( in, true );
        assertNotNull( standaloneReadClass , "Expected not null StandaloneReadClass");

        fr = new XmlStreamReader( file );
        standaloneReadClass = reader.readStandaloneReadClass( fr );
        assertNotNull( standaloneReadClass , "Expected not null StandaloneReadClass");

        fr = new XmlStreamReader( file );
        standaloneReadClass = reader.readStandaloneReadClass( fr, true );
        assertNotNull( standaloneReadClass , "Expected not null StandaloneReadClass");

        String methodName = "readFooClass";
        for ( Method method : reader.getClass().getMethods() )
        {
            assertFalse( methodName.equals( method.getName() ) , "Found method " + methodName);
        }
    }
}
