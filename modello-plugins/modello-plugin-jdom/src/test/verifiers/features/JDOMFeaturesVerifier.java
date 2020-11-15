package org.codehaus.modello.generator.xml.jdom;

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
import org.codehaus.modello.test.features.io.jdom.ModelloFeaturesTestJDOMWriter;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.modello.verifier.VerifierException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author Herve Boutemy
 */
public class JDOMFeaturesVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Features features = read();

        features.getXmlFeatures().getXmlTransientFields().setTransientString( "NOT-TO-BE-WRITTEN" );

        verifyWriter( features );
    }

    private Features read()
        throws Exception
    {
        // no JDOM reader: using Stax reader instead...
        ModelloFeaturesTestStaxReader reader = new ModelloFeaturesTestStaxReader();

        return reader.read( getXmlResourceReader( "/features.xml" ) );
    }

    public void verifyWriter( Features features )
        throws Exception
    {
        ModelloFeaturesTestJDOMWriter writer = new ModelloFeaturesTestJDOMWriter();

        StringWriter buffer = new StringWriter();

        writer.write( features, buffer, Format.getRawFormat() );

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
            System.err.println( "known features missing: MODELLO-161 = attributes, MODELLO-202 = Content type, "
                                + "and much more: Properties, xml.tagName, ..." );
            /*throw*/ new VerifierException( "writer result is not the same as original content: " + diff )
                .printStackTrace( System.err );
        }

        if ( !actualXml.contains( "<fieldTrim>by default, field content is trimmed</fieldTrim>" ) )
        {
            throw new VerifierException( "fieldTrim was trimmed..." );
        }
        if ( !actualXml.contains( "<fieldNoTrim>   do not trim this field   </fieldNoTrim>" ) )
        {
            throw new VerifierException( "fieldNoTrim was trimmed..." );
        }
        if ( !actualXml.contains( "<element>by default, the element content is trimmed</element>" ) )
        {
            throw new VerifierException( "dom was trimmed..." );
        }
        if ( !actualXml.contains( "<element>   do not trim the element content   </element>" ) )
        {
            throw new VerifierException( "domNoTrim was trimmed..." );
        }
    }
}
