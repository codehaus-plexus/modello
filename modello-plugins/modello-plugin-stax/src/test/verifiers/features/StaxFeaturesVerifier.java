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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

/**
 * @author Herve Boutemy
 */
public class StaxFeaturesVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        verifyAPI();

        Features features = verifyReader();

        features.getXmlFeatures().getXmlTransientFields().setTransientString( "NOT-TO-BE-WRITTEN" );

        verifyWriter( features );

        verifyBadVersion();

        verifyWrongElement();

        verifyWrongContent();

        verifyTransientElement();

        verifyEncoding();
    }

    public void verifyAPI()
        throws Exception
    {
        assertReader( ModelloFeaturesTestStaxReader.class, Features.class, Reader.class, XMLStreamException.class );
        assertReader( ModelloFeaturesTestStaxReader.class, Features.class, InputStream.class, XMLStreamException.class );

        assertWriter( ModelloFeaturesTestStaxWriter.class, Features.class, Writer.class, XMLStreamException.class );
        assertWriter( ModelloFeaturesTestStaxWriter.class, Features.class, OutputStream.class, XMLStreamException.class );
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
        // writer doesn't handle namespace
        actualXml = actualXml.replaceFirst( "<preserve space=\"preserve\">", "<preserve xml:space=\"preserve\">" );

        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreComments( true );
        Diff diff = XMLUnit.compareXML( initialXml, actualXml );

        if ( !diff.identical() )
        {
            System.err.println( actualXml );
            throw new VerifierException( "writer result is not the same as original content: " + diff );
        }

        if ( !actualXml.contains( "<fieldTrim>by default, field content is trimmed</fieldTrim>" ) )
        {
            throw new VerifierException( "fieldTrim was not trimmed..." );
        }
        if ( !actualXml.contains( "<fieldNoTrim>   do not trim this field   </fieldNoTrim>" ) )
        {
            throw new VerifierException( "fieldNoTrim was trimmed..." );
        }
        if ( !actualXml.contains( "<element>by default, the element content is trimmed</element>" ) )
        {
            throw new VerifierException( "dom was not trimmed..." );
        }
        if ( !actualXml.contains( "<preserve xml:space=\"preserve\">   but with xml:space=\"preserve\", the element content is preserved   </preserve>" ) )
        {
            throw new VerifierException( "preserve was trimmed..." );
        }
        if ( !actualXml.contains( "<element>   do not trim the element content   </element>" ) )
        {
            throw new VerifierException( "domNoTrim was trimmed..." );
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
            checkExpectedFailure( xse, "Document model version of '2.0.0' doesn't match reader version of '1.0.0'" );
        }
    }

    public void verifyWrongElement()
        throws Exception
    {
        ModelloFeaturesTestStaxReader reader = new ModelloFeaturesTestStaxReader();

        // reading with strict=false should accept unknown element
        reader.read( getXmlResourceReader( "/features-wrong-element.xml" ), false );
        reader.read( getXmlResourceReader( "/features-wrong-element2.xml" ), false );

        // by default, strict=true: reading should not accept unknown element
        try
        {
            reader.read( getXmlResourceReader( "/features-wrong-element.xml" ) );

            throw new VerifierException( "Reading a document with an unknown element under strict option should fail." );
        }
        catch ( XMLStreamException xse )
        {
            // expected failure
            checkExpectedFailure( xse, "'invalidElement'" );
        }
        try
        {
            reader.read( getXmlResourceReader( "/features-wrong-element2.xml" ) );

            throw new VerifierException( "Reading a document with an unknown element under strict option should fail." );
        }
        catch ( XMLStreamException xse )
        {
            checkExpectedFailure( xse, "'invalidElement'" );
        }
    }

    public void verifyWrongContent()
        throws Exception
    {
        ModelloFeaturesTestStaxReader reader = new ModelloFeaturesTestStaxReader();

        // reading with strict=false should accept unexpected text content
        reader.read( getClass().getResourceAsStream( "/features-wrong-content.xml" ), false );

        // by default, strict=true: reading should not accept unexpected content
        try
        {
            reader.read( getClass().getResourceAsStream( "/features-wrong-content.xml" ) );

            throw new VerifierException( "Reading a document with a bad content under strict option should fail." );
        }
        catch ( XMLStreamException xse )
        {
            checkExpectedFailure( xse, "non-all-whitespace CHARACTERS or CDATA event" );
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
            checkExpectedFailure( e, "transientString" );
        }
    }

    private void checkExpectedFailure( XMLStreamException xse, String expectedMessage )
        throws VerifierException
    {
        if ( xse.getMessage().indexOf( expectedMessage ) < 0 )
        {
            throw new VerifierException( "Unexpected failure: \"" + xse.getMessage() + "\"", xse );
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
