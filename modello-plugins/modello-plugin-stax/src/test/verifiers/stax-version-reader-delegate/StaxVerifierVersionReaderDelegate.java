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
import org.codehaus.modello.test.model.vin.io.stax.VersionInNamespaceStaxReaderDelegate;
import org.codehaus.modello.verifier.Verifier;

import java.io.File;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;

/**
 */
public class StaxVerifierVersionReaderDelegate
    extends Verifier
{
    public void verify()
        throws IOException, XMLStreamException
    {
        VersionInNamespaceStaxReaderDelegate modelReader = new VersionInNamespaceStaxReaderDelegate();

        String path = "src/test/verifiers/stax-version-reader-delegate/input-4.0.0.xml";
        org.codehaus.modello.test.model.vin.v4_0_0.Model model400 =
            (org.codehaus.modello.test.model.vin.v4_0_0.Model) modelReader.read( new File( path ) );

        Assert.assertEquals( "Maven", model400.getName() );
        Assert.assertEquals( "Something out of place.", model400.getDescription() );

        path = "src/test/verifiers/stax-version-reader-delegate/input-4.0.1.xml";
        org.codehaus.modello.test.model.vin.v4_0_1.Model model401 =
            (org.codehaus.modello.test.model.vin.v4_0_1.Model) modelReader.read( new File( path ) );

        Assert.assertEquals( "Maven", model401.getName() );
        Assert.assertEquals( "Something out of place.", model401.getDescription() );
    }
}
