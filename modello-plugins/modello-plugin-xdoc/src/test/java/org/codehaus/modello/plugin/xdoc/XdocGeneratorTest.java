package org.codehaus.modello.plugin.xdoc;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.modello.verifier.VerifierException;
import org.codehaus.plexus.util.FileUtils;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import junit.framework.Assert;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class XdocGeneratorTest
    extends AbstractModelloGeneratorTest

{
    public XdocGeneratorTest()
    {
        super( "xdoc" );
    }

    protected File getOutputDirectory()
    {
        return getTestFile( "target/generated-site/xdoc" );
    }

    public void testXdocGenerator()
        throws Exception
    {
        checkMavenXdocGenerator();
        checkFeaturesXdocGenerator();
        checkSettingsXdocGenerator();
    }
    
    public void testHtmlToXml() throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/html4.mdo" ) );

        Properties parameters = getModelloParameters( "1.0.0" );

        modello.generate( model, "xdoc", parameters );
        
        Diff diff = DiffBuilder.compare( Input.fromStream( XdocGeneratorTest.class.getResourceAsStream( "/html4.expected.xml" ) ) )
                   .withTest( Input.fromFile( new File( getOutputDirectory(), "html4.xml" ) ) ).build();
        
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    private void checkMavenXdocGenerator()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/maven.mdo" ) );

        List<ModelClass> classesList = model.getClasses( new Version( "4.0.0" ) );

        assertEquals( 26, classesList.size() );

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

        modello.generate( model, "xdoc", parameters );

        //addDependency( "modello", "modello-core", "1.0-SNAPSHOT" );

        //verify( "org.codehaus.modello.generator.xml.cdoc.XdocVerifier", "xdoc" );
        checkInternalLinks( "maven.xml" );
    }

    public void checkFeaturesXdocGenerator()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/features.mdo" ) );

        Properties parameters = getModelloParameters( "1.5.0" );

        modello.generate( model, "xdoc", parameters );

        checkInternalLinks( "features.xml" );

        String content = FileUtils.fileRead( new File( getOutputDirectory(), "features.xml" ), "UTF-8" );

        assertTrue( "Transient fields were erroneously documented", !content.contains( "transientString" ) );
    }

    public void checkSettingsXdocGenerator()
        throws Exception
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/settings.mdo" ) );

        Properties parameters = getModelloParameters( "1.5.0" );

        modello.generate( model, "xdoc", parameters );

        checkInternalLinks( "settings.xml" );

        String content = FileUtils.fileRead( new File( getOutputDirectory(), "settings.xml" ), "UTF-8" );

        assertTrue( "Properties field was erroneously documented", !content.contains("&lt;properties/&gt;"));
    }

    /**
     * Checks internal links in the xdoc content: for every 'a href="#xxx"' link, a 'a name="xxx"' must exist (or there
     * is a problem in the generated content).
     *
     * @param xdoc
     * @throws Exception
     */
    private void checkInternalLinks( String filename )
        throws Exception
    {
        String content = FileUtils.fileRead( new File( getOutputDirectory(), filename ), "UTF-8" );

        Set<String> hrefs = new HashSet<String>();
        Pattern p = Pattern.compile( "<a href=\"#(class_[^\"]+)\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE );
        Matcher m = p.matcher( content );
        while ( m.find() )
        {
            hrefs.add( m.group( 1 ) );
        }
        Assert.assertTrue( "should find some '<a href=' links", hrefs.size() > 0 );

        Set<String> names = new HashSet<String>();
        p = Pattern.compile( "<a name=\"(class_[^\"]+)\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE );
        m = p.matcher( content );
        while ( m.find() )
        {
            names.add( m.group( 1 ) );
        }
        Assert.assertTrue( "should find some '<a name=' anchor definitions", names.size() > 0 );

        hrefs.removeAll( names );
        if ( hrefs.size() > 0 )
        {
            throw new VerifierException( "some internal hrefs in " + filename + " are not defined: " + hrefs );
        }
    }
}
