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
 * @author Herve Boutemy
 * @version $Id$
 */
public class StaxFeaturesVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Features features = verifyReader();

        features.getXmlFeatures().getXmlTransientFields().setTransientString( "NOT-TO-BE-WRITTEN" );

        verifyWriter( features );

        verifyBadVersion();

        verifyWrongElement();

        verifyTransientElement();

        verifyEncoding();
    }

    public Features verifyReader()
        throws Exception
    {
        ModelloFeaturesTestStaxReader reader = new ModelloFeaturesTestStaxReader();

        return reader.read( getXmlResourceReader( "/features.xml" ) );
    }

    public void verifyWriter( Features features )
        throws Exception
    {
        ModelloFeaturesTestStaxWriter writer = new ModelloFeaturesTestStaxWriter();

        StringWriter buffer = new StringWriter();

        writer.write( buffer, features );

        String initialXml = IOUtil.toString( getXmlResourceReader( "/features.xml" ) );
        String actualXml = buffer.toString();

        // alias is rendered as default field name => must be reverted here to let the test pass
        actualXml = actualXml.replaceFirst( "<id>alias</id>", "<key>alias</key>" );

        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreComments( true );
        Diff diff = XMLUnit.compareXML( initialXml, actualXml );

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
            reader.read( getXmlResourceReader( "/features-bad-version.xml" ) );

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
        reader.read( getXmlResourceReader( "/features-wrong-element.xml" ), false );

        // by default, strict=true: reading should not accept unknown element
        try
        {
            reader.read( getXmlResourceReader( "/features-wrong-element.xml" ) );

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

    public void verifyTransientElement()
        throws Exception
    {
        ModelloFeaturesTestStaxReader reader = new ModelloFeaturesTestStaxReader();

        try
        {
            reader.read( getXmlResourceReader( "/features-invalid-transient.xml" ) );

            fail( "Transient fields should not be processed by parser." );
        }
        catch ( XMLStreamException e )
        {
            assertTrue( e.getMessage().indexOf( "transientString" ) >= 0 );
        }
    }

    private void checkEncoding( String resource, String encoding )
        throws Exception
    {
        ModelloFeaturesTestStaxReader reader = new ModelloFeaturesTestStaxReader();

        Features features = reader.read( getXmlResourceReader( resource ) );
        assertEquals( "modelEncoding", encoding, features.getModelEncoding() );

        ModelloFeaturesTestStaxWriter writer = new ModelloFeaturesTestStaxWriter();
        StringWriter buffer = new StringWriter();
        writer.write( buffer, features );
        String xmlHeader = buffer.toString().substring( 0, 44 );

        if ( encoding == null )
        {
            assertTrue( xmlHeader, xmlHeader.startsWith( "<?xml version='1.0'?>" ) );
        }
        else
        {
            assertTrue( xmlHeader, xmlHeader.startsWith( "<?xml version='1.0' encoding='" + encoding + "'?>" ) );
        }
    }

    public void verifyEncoding()
        throws Exception
    {
        checkEncoding( "/features.xml", null );
        checkEncoding( "/features-UTF-8.xml", "UTF-8" );
        checkEncoding( "/features-Latin-15.xml", "ISO-8859-15" );
    }
}
