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

import junit.framework.Assert;
import org.codehaus.modello.test.features.Features;
import org.codehaus.modello.test.features.io.xpp3.ModelloFeaturesTestXpp3Reader;
import org.codehaus.modello.test.features.io.xpp3.ModelloFeaturesTestXpp3Writer;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.modello.verifier.VerifierException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Hervé Boutemy
 * @version $Id$
 */
public class Xpp3FeaturesVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Features features = verifyReader();

        verifyWriter( features );
    }

    public Features verifyReader()
        throws Exception
    {
        ModelloFeaturesTestXpp3Reader reader = new ModelloFeaturesTestXpp3Reader();

        return reader.read( getClass().getResourceAsStream( "/features.xml" ) );
    }

    public void verifyWriter( Features features )
        throws Exception
    {
        ModelloFeaturesTestXpp3Writer writer = new ModelloFeaturesTestXpp3Writer();

        StringWriter buffer = new StringWriter();

        // workaround for MODELLO-126
        features.getSimpleTypes().setObjectDate( null ); 

        writer.write( buffer, features );

        String actualXml = buffer.toString();

        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreComments( true );
        Diff diff = XMLUnit.compareXML( IOUtil.toString( getClass().getResourceAsStream( "/features.xml" ), "UTF-8" ), actualXml );

        if ( !diff.identical() )
        {
            throw new VerifierException( "writer result is not the same as original content: " + diff );
        }
    }
}
