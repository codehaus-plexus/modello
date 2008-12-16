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

public class UpgradeVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        File file = new File( "src/test/verifiers/upgrade/input.xml" );

        org.codehaus.modello.test.maven.v3_0_0.io.stax.MavenStaxReader readerV3 = new org.codehaus.modello.test.maven.v3_0_0.io.stax.MavenStaxReader();
        org.codehaus.modello.test.maven.v3_0_0.Model modelV3 = readerV3.read( file.getAbsolutePath() );

        org.codehaus.modello.test.maven.v4_0_0.upgrade.VersionUpgrade upgradeV4 = new org.codehaus.modello.test.maven.v4_0_0.upgrade.BasicVersionUpgrade();
        org.codehaus.modello.test.maven.Model modelV4 = upgradeV4.upgradeModel( modelV3 );

        // technically, v3 groupId and v4 groupId are two unrelated fields and thus there is no automatic conversion
        modelV4.setGroupId( modelV3.getGroupId() );

        StringWriter sw = new StringWriter();
        org.codehaus.modello.test.maven.io.stax.MavenStaxWriter writerV4 = new org.codehaus.modello.test.maven.io.stax.MavenStaxWriter();
        writerV4.write( sw, modelV4 );

        Assert.assertEquals(
            convertLineEndings( FileUtils.fileRead( "src/test/verifiers/upgrade/expected.xml" ).trim() ),
            convertLineEndings( scrubXmlDeclQuotes( sw.toString() ).trim() ) );
        
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
