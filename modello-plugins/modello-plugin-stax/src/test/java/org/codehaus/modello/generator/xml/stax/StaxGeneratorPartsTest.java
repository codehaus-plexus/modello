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
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.Version;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.util.List;

/**
 */
public class StaxGeneratorPartsTest
    extends AbstractStaxGeneratorTestCase
{
    public StaxGeneratorPartsTest()
        throws ComponentLookupException
    {
        super( "stax-parts" );
    }

    public void testStaxReaderParts()
        throws Throwable
    {
        Model model = modello.loadModel( getXmlResourceReader( "/parts.mdo" ) );

        List<ModelClass> classesList = model.getClasses( new Version( "4.0.0" ) );

        assertEquals( 12, classesList.size() );

        ModelClass clazz = (ModelClass) classesList.get( 0 );

        assertEquals( "Model", clazz.getName() );

        verifyModel( model, "org.codehaus.modello.generator.xml.stax.StaxVerifierParts" );
    }

}
