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
 * Check that features.mdo (which tries to be the most complete model) can be checked against XSD generated from
 * Modello model <code>modello.mdo</code>.
 *
 * @author Hervé Boutemy
 */
public class ModelloXsdGeneratorTest
    extends AbstractModelloGeneratorTest
{
    public ModelloXsdGeneratorTest()
    {
        super( "modello" );
    }

    public void testXsdGenerator()
        throws Throwable
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Properties parameters = getModelloParameters( "1.4.0" );

        Model model = modello.loadModel( getTestFile( "../../src/main/mdo/modello.mdo" ) );

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
                               new File( getOutputDirectory(), "modello-1.4.0.xsd" ) );

        // first self-test: validate Modello model with xsd generated from it
        saxParser.parse( getTestFile( "../../src/main/mdo/modello.mdo" ), new Handler() );

        // then features.mdo
        saxParser.parse( getClass().getResourceAsStream( "/features.mdo" ), new Handler() );
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
