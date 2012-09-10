package org.codehaus.modello.generator.xml.stax;

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

import junit.framework.Assert;
import org.codehaus.modello.test.model.vif.Model;
import org.codehaus.modello.test.model.vif.io.stax.VersionInFieldStaxReader;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import javax.xml.stream.XMLStreamException;

/**
 */
public class StaxVerifierWrongVersion
    extends Verifier
{
    public void verify()
        throws IOException, XMLStreamException
    {
        File file = new File( "src/test/verifiers/stax-wrong-version/wrong-version.xml" );

        Reader reader = ReaderFactory.newXmlReader( file );
        VersionInFieldStaxReader modelReader = new VersionInFieldStaxReader();

        Assert.assertEquals( "1.2.3", modelReader.determineVersion( reader ) );

        reader = ReaderFactory.newXmlReader( file );
        try
        {
            modelReader.read( reader );
            Assert.fail( "Should have choked on the version" );
        }
        catch ( XMLStreamException e )
        {
            Assert.assertTrue( e.getMessage().endsWith(
            "Document model version of '1.2.3' doesn't match reader version of '4.0.0'" ) );
        }
    }
}
