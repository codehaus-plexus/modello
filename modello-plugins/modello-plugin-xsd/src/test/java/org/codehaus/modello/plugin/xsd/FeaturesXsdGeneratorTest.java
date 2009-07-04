package org.codehaus.modello.plugin.xsd;

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

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author Hervé Boutemy
 * @version $Id$
 */
public class FeaturesXsdGeneratorTest
    extends AbstractModelloGeneratorTest
{
    public FeaturesXsdGeneratorTest()
    {
        super( "features" );
    }

    public void testXsdGenerator()
        throws Throwable
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Properties parameters = new Properties();
        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getOutputDirectory().getAbsolutePath() );
        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        Model model = modello.loadModel( getXmlResourceReader( "/features.mdo" ) );

        modello.generate( model, "xsd", parameters );

        /* only available in JAXP 1.3, JDK 5+
        SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        Schema schema = factory.newSchema( new StreamSource( new File( generatedSources, "features.xsd" ) ) );

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setSchema( schema );
        SAXParser parser = spf.newSAXParser();
        parser.parse( new InputSource( getClass().getResourceAsStream( "/features.xml" ) ) );
        */

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating( true );
        factory.setNamespaceAware( true );
        SAXParser saxParser = factory.newSAXParser();
        saxParser.setProperty( "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                               "http://www.w3.org/2001/XMLSchema" );
        saxParser.setProperty( "http://java.sun.com/xml/jaxp/properties/schemaSource",
                               new File( getOutputDirectory(), "features-1.0.0.xsd" ) );

        saxParser.parse( getClass().getResourceAsStream( "/features.xml" ), new Handler() );

        try
        {
            saxParser.parse( getClass().getResourceAsStream( "/features-invalid.xml" ), new Handler() );
            fail( "parsing of features-invalid.xml should have failed" );
        }
        catch ( SAXParseException e )
        {
            // ok, expected exception
            e.printStackTrace();
            assertTrue( String.valueOf( e.getMessage() ).indexOf( "invalidElement" ) >= 0 );
        }

        try
        {
            saxParser.parse( getClass().getResourceAsStream( "/features-invalid-transient.xml" ), new Handler() );
            fail( "XSD did not prohibit appearance of transient fields" );
        }
        catch ( SAXParseException e )
        {
            // ok, expected exception
            e.printStackTrace();
            assertTrue( String.valueOf( e.getMessage() ).indexOf( "transientString" ) >= 0 );
        }
    }

    private static class Handler
        extends DefaultHandler
    {
        public void warning ( SAXParseException e )
            throws SAXException
        {
            throw e;
        }

        public void error ( SAXParseException e )
            throws SAXException
        {
            throw e;
        }
    }
}
