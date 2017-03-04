package org.codehaus.modello.plugin.java;

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

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.Properties;

/**
 * check <code>toString()</code> method is generated.
 *
 * @author <a href="mailto:olamy@apache.org">Olivier Lamy</a>
 */
public class ToStringJavaGeneratorTest
    extends AbstractModelloJavaGeneratorTest
{
    public ToStringJavaGeneratorTest()
    {
        super( "tostring" );
    }

    /**
     * @throws Exception if any exception occurs
     */
    public void testJavaGeneratorWithToString()
        throws Exception 
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/models/tostring.mdo" ) );

        Properties parameters = getModelloParameters( "1.0.0" );

        modello.generate( model, "java", parameters );

        compileGeneratedSources();

        File source = new File(getOutputDirectory(), "foo/bar/tostring/MyClass.java");

        String sourceGenerated = FileUtils.fileRead( source );

        assertTrue( sourceGenerated.contains( "public java.lang.String toString()" ) );
    }
}
