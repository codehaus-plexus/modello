package org.codehaus.modello;

/*
 * Copyright (c) 2004, Jason van Zyl
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

import java.io.File;
import java.io.StringReader;

import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloTest
    extends PlexusTestCase
{
    private Modello modello;

    private String basedir;

    public ModelloTest()
    {
        basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );
    }

    protected ModelloCore getModelloCore()
        throws Exception
    {
        return (ModelloCore) lookup( ModelloCore.ROLE );
    }

    protected Model loadModel( String name )
        throws Exception
    {
        ModelloCore modello = getModelloCore();

        String fileString = FileUtils.fileRead( getTestPath( name ) );

        fileString = StringUtils.replace( fileString, "<description>", "<description><![CDATA[" );

        fileString = StringUtils.replace( fileString, "</description>", "]]></description>" );

        return modello.loadModel( new StringReader( fileString ) );
    }
}
