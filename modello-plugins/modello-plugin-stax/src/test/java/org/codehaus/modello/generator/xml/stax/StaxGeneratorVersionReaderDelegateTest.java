package org.codehaus.modello.generator.xml.stax;

/*
 * Copyright (c) 2006, Codehaus.org
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

import org.codehaus.modello.model.Model;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 */
public class StaxGeneratorVersionReaderDelegateTest
    extends AbstractStaxGeneratorTestCase
{
    public StaxGeneratorVersionReaderDelegateTest()
        throws ComponentLookupException
    {
        super( "stax-version-reader-delegate" );
    }

    public void testStaxReaderVersionInField()
        throws Throwable
    {
        Model model = modello.loadModel( getXmlResourceReader( "/version-in-namespace.mdo" ) );

        verifyModel( model, "org.codehaus.modello.generator.xml.stax.StaxVerifierVersionReaderDelegate",
                     new String[] { "4.0.0", "4.0.1" } );
    }

}
