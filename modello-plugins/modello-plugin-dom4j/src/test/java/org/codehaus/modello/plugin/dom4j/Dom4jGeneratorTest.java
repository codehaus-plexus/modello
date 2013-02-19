package org.codehaus.modello.plugin.dom4j;

/*
 * Copyright (c) 2006, Codehaus.org
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
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;

import java.util.List;
import java.util.Properties;

/**
 * Test the generators.
 *
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 */
public class Dom4jGeneratorTest
    extends AbstractModelloJavaGeneratorTest
{
    public Dom4jGeneratorTest()
    {
        super( "dom4j" );
    }

    public void testDom4jGenerator()
        throws Throwable
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/maven.mdo" ) );

        List<ModelClass> classesList = model.getClasses( new Version( "4.0.0" ) );

        assertEquals( 27, classesList.size() );

        ModelClass clazz = (ModelClass) classesList.get( 0 );

        assertEquals( "Model", clazz.getName() );

        ModelField extend = clazz.getField( "extend", new Version( "4.0.0" ) );

        assertTrue( extend.hasMetadata( XmlFieldMetadata.ID ) );

        XmlFieldMetadata xml = (XmlFieldMetadata) extend.getMetadata( XmlFieldMetadata.ID );

        assertNotNull( xml );

        assertTrue( xml.isAttribute() );

        assertEquals( "extender", xml.getTagName() );

        ModelField build = clazz.getField( "build", new Version( "4.0.0" ) );

        assertTrue( build.hasMetadata( XmlFieldMetadata.ID ) );

        xml = (XmlFieldMetadata) build.getMetadata( XmlFieldMetadata.ID );

        assertNotNull( xml );

        assertEquals( "builder", xml.getTagName() );

        Properties parameters = getModelloParameters( "4.0.0" );

        modello.generate( model, "java", parameters );
        modello.generate( model, "dom4j-writer", parameters );
        modello.generate( model, "dom4j-reader", parameters );

        addDependency( "dom4j", "dom4j" );

        compileGeneratedSources();

        verifyCompiledGeneratedSources( "org.codehaus.modello.generator.xml.dom4j.Dom4jVerifier" );
    }

}
