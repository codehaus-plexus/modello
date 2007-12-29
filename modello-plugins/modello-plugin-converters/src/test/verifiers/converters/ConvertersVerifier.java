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

import org.codehaus.modello.verifier.Verifier;

import junit.framework.Assert;
import java.io.*;
import org.codehaus.plexus.util.FileUtils;

public class ConvertersVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        // Note that this is *not* a full POM translation (fields like currentVersion are not mapped)

        File file = new File( "src/test/verifiers/converters/input.xml" );

        org.codehaus.modello.test.maven.convert.ConverterTool convert = new org.codehaus.modello.test.maven.convert.ConverterTool();
        org.codehaus.modello.test.maven.v3_0_0.Model modelV3 = convert.convertFromFile_v3_0_0( file );
        StringWriter sw = new StringWriter();
        org.codehaus.modello.test.maven.v3_0_0.io.stax.MavenStaxWriter writerV3 = new org.codehaus.modello.test.maven.v3_0_0.io.stax.MavenStaxWriter();
        writerV3.write( sw, modelV3 );

        Assert.assertEquals(
            convertLineEndings( FileUtils.fileRead( "src/test/verifiers/converters/expected-v3.xml" ).trim() ),
            convertLineEndings( scrubXmlDeclQuotes( sw.toString() ).trim() ) );

        org.codehaus.modello.test.maven.v4_0_0.Model modelV4 = convert.convertFromFile_v4_0_0( file );

        sw = new StringWriter();
        org.codehaus.modello.test.maven.v4_0_0.io.stax.MavenStaxWriter writerV4 = new org.codehaus.modello.test.maven.v4_0_0.io.stax.MavenStaxWriter();
        writerV4.write( sw, modelV4 );

        Assert.assertEquals(
            convertLineEndings( FileUtils.fileRead( "src/test/verifiers/converters/expected.xml" ).trim() ),
            convertLineEndings( scrubXmlDeclQuotes( sw.toString() ).trim() ) );

        org.codehaus.modello.test.maven.Model model = convert.convertFromFile( file );

        sw = new StringWriter();
        org.codehaus.modello.test.maven.io.stax.MavenStaxWriter writer = new org.codehaus.modello.test.maven.io.stax.MavenStaxWriter();
        writer.write( sw, model );

        Assert.assertEquals(
            convertLineEndings( FileUtils.fileRead( "src/test/verifiers/converters/expected.xml" ).trim() ),
            convertLineEndings( scrubXmlDeclQuotes( sw.toString() ).trim() ) );

        // Test trying to convert to an old version
        try
        {
            modelV3 = convert.convertFromFile_v3_0_0( new File( "src/test/verifiers/converters/expected.xml" ) );
            Assert.fail( "Should have failed to convert" );
        }
        catch ( IllegalStateException e )
        {
            Assert.assertTrue( true );
        }
    }

    private String convertLineEndings( String s )
    {
        return s.replaceAll( "\r\n", "\n" );
    }

    private String scrubXmlDeclQuotes( String s )
    {
        if ( s.startsWith( "<?xml version='1.0' encoding='UTF-8'?>"))
        {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + s.substring( "<?xml version='1.0' encoding='UTF-8'?>".length() );
        }
        return s;
    }
}
