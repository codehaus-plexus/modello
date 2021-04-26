package org.codehaus.modello.plugin.jdom;

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

import java.util.Properties;

/**
 * @author Hervé Boutemy
 */
public class FeaturesJDOMGeneratorTest
    extends AbstractModelloJavaGeneratorTest
{
    public FeaturesJDOMGeneratorTest()
    {
        super( "features" );
    }

    public void testJavaGenerator()
        throws Throwable
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/features.mdo" ) );

        Properties parameters = getModelloParameters( "1.0.0" );

        modello.generate( model, "java", parameters );
        modello.generate( model, "stax-reader", parameters );
        modello.generate( model, "jdom-writer", parameters );

        addDependency( "org.jdom", "jdom" );
        addDependency( "org.xmlunit", "xmlunit-core" );
        compileGeneratedSources();

        verifyCompiledGeneratedSources( "org.codehaus.modello.generator.xml.jdom.JDOMFeaturesVerifier" );
    }
}
