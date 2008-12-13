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
import org.codehaus.modello.test.features.Features;
import org.codehaus.modello.test.features.io.stax.ModelloFeaturesTestStaxReader;
import org.codehaus.modello.test.features.io.stax.ModelloFeaturesTestStaxWriter;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.modello.verifier.VerifierException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

/**
 * @author Hervé Boutemy
 * @version $Id$
 */
public class StaxFeaturesVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Features features = verifyReader();

        verifyWriter( features );

        verifyBadVersion();

        verifyWrongElement();
    }

    public Features verifyReader()
        throws Exception
    {
        ModelloFeaturesTestStaxReader reader = new ModelloFeaturesTestStaxReader();

        return reader.read( new InputStreamReader( getClass().getResourceAsStream( "/features.xml" ), "UTF-8" ) );
    }

    public void verifyWriter( Features features )
        throws Exception
    {
        ModelloFeaturesTestStaxWriter writer = new ModelloFeaturesTestStaxWriter();

        StringWriter buffer = new StringWriter();

        // workaround for MODELLO-126
        features.getSimpleTypes().setObjectDate( null ); 

        writer.write( buffer, features );

        String actualXml = buffer.toString();
        // workaround...
        actualXml = actualXml.replaceFirst( "<features>", "<features xmlns=\"http://modello.codehaus.org/FEATURES\" "
          + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
          + "xsi:schemaLocation=\"http://modello.codehaus.org/FEATURES http://modello.codehaus.org/features-1.0.0.xsd\">" );

        // alias is rendered as default field name => must be reverted here to let the test pass
        actualXml = actualXml.replaceFirst( "<id>alias</id>", "<key>alias</key>" );

        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreComments( true );
        Diff diff = XMLUnit.compareXML( IOUtil.toString( getClass().getResourceAsStream( "/features.xml" ), "UTF-8" ), actualXml );

        if ( !diff.identical() )
        {
            System.err.println( actualXml );
            throw new VerifierException( "writer result is not the same as original content: " + diff );
        }
    }

    public void verifyBadVersion()
        throws Exception
    {
        ModelloFeaturesTestStaxReader reader = new ModelloFeaturesTestStaxReader();

        try
        {
            reader.read( new InputStreamReader( getClass().getResourceAsStream( "/features-bad-version.xml" ), "UTF-8" ) );

            throw new VerifierException( "Reading a document with a version different from the version of the parser should fail." );
        }
        catch ( XMLStreamException xse )
        {
            // expected failure
            if ( xse.getMessage().indexOf( "Document model version of '2.0.0' doesn't match reader version of '1.0.0'" ) < 0 )
            {
                throw new VerifierException( "Unexpected failure when reading a document with a version different from"
                                             + " the version of the parser: \"" + xse.getMessage() + "\"", xse );
            }
        }
    }

    public void verifyWrongElement()
        throws Exception
    {
        ModelloFeaturesTestStaxReader reader = new ModelloFeaturesTestStaxReader();

        // reading with strict=false should accept unknown element
        reader.read( new InputStreamReader( getClass().getResourceAsStream( "/features-wrong-element.xml" ), "UTF-8" ), false );

        // by default, strict=true: reading should not accept unknown element
        try
        {
            reader.read( new InputStreamReader( getClass().getResourceAsStream( "/features-wrong-element.xml" ), "UTF-8" ) );

            throw new VerifierException( "Reading a document with an unknown element under strict option should fail." );
        }
        catch ( XMLStreamException xse )
        {
            // expected failure
            if ( xse.getMessage().indexOf( "'invalidElement'" ) < 0 )
            {
                throw new VerifierException( "Unexpected failure when reading a document an unknown element under"
                                             + " strict option: \"" + xse.getMessage() + "\"", xse );
            }
        }
    }
}
