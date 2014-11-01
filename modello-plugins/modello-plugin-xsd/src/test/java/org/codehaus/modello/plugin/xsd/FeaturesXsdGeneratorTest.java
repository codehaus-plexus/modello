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
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * @author Hervé Boutemy
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

        Model model = modello.loadModel( getXmlResourceReader( "/features.mdo" ) );

        Properties parameters = getModelloParameters( "1.0.0" );

        modello.generate( model, "xsd", parameters );

        SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        Schema schema = sf.newSchema( new StreamSource( new File( getOutputDirectory(), "features-1.0.0.xsd" ) ) );
        Validator validator = schema.newValidator();

        try
        {
            validator.validate( new StreamSource( getClass().getResourceAsStream( "/features.xml" ) ) );
        }
        catch ( SAXParseException e )
        {
            throw new ModelloException( "line " + e.getLineNumber() + " column " + e.getColumnNumber(), e );
        }

        try
        {
            validator.validate( new StreamSource( getClass().getResourceAsStream( "/features-invalid.xml" ) ) );
            fail( "parsing of features-invalid.xml should have failed" );
        }
        catch ( SAXParseException e )
        {
            // ok, expected exception
            assertTrue( String.valueOf( e.getMessage() ).contains( "invalidElement" ) );
        }

        try
        {
            validator.validate( new StreamSource( getClass().getResourceAsStream( "/features-invalid-transient.xml" ) ) );
            fail( "XSD did not prohibit appearance of transient fields" );
        }
        catch ( SAXParseException e )
        {
            // ok, expected exception
            assertTrue( String.valueOf( e.getMessage() ).contains( "transientString" ) );
        }
    }
}
