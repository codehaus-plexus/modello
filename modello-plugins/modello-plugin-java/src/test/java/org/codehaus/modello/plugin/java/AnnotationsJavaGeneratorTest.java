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

import java.io.File;
import java.util.Properties;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.FileUtils;

/**
 * @version $Id: TmpJavaGeneratorTest.java 1125 2009-01-10 20:29:32Z hboutemy $
 */
public class AnnotationsJavaGeneratorTest
    extends AbstractModelloJavaGeneratorTest
{
    public AnnotationsJavaGeneratorTest()
    {
        super( "annotations" );
    }

    public void testJavaGeneratorWithAnnotations()
        throws Throwable
    {
        if ( skipJava5FeatureTest() )
        {
            return;
        }

        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/models/annotations.mdo" ) );

        Properties parameters = getModelloParameters( "1.0.0", true );

        modello.generate( model, "java", parameters );

        addDependency( "javax.xml.bind", "jaxb-api", "2.1" );
        addDependency( "org.apache.geronimo.specs", "geronimo-jpa_2.0_spec", "1.0" );
        compileGeneratedSources( true );

        String groupClassText = FileUtils.fileRead( new File( getOutputDirectory(), "model/Group.java" ) );

        // we could check a little more robust then this.
        assertTrue( "class:\n" + groupClassText, groupClassText.indexOf( "@javax.persistence.JoinColumn" ) >= 0 );
        // TODO: add a check for every annotations places supported: class, interface, field, association (?)
    }
}