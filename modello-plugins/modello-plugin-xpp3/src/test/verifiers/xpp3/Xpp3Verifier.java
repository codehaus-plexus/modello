package org.codehaus.modello.generator.xml.xpp3;

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
import org.codehaus.modello.test.model.Build;
import org.codehaus.modello.test.model.Component;
import org.codehaus.modello.test.model.ContentTest;
import org.codehaus.modello.test.model.Local;
import org.codehaus.modello.test.model.MailingList;
import org.codehaus.modello.test.model.Model;
import org.codehaus.modello.test.model.Organization;
import org.codehaus.modello.test.model.Repository;
import org.codehaus.modello.test.model.Scm;
import org.codehaus.modello.test.model.SourceModification;
import org.codehaus.modello.test.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.modello.test.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.modello.verifier.VerifierException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class Xpp3Verifier
    extends Verifier
{
    /**
     * TODO: Add a association thats not under the root element
     */
    public void verify()
        throws Exception
    {
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));

        verifyWriter();

        verifyReader();

        verifyReaderAliases();

        verifyReaderDefaultValue();

        verifyReaderDuplicates();

        verifyReaderMissingTags_DefaultMode();

        verifyReaderMissingTags_StrictMode();

        verifyReaderMissingTags_NonStrictMode();

        verifyThrowingExceptionWithWrongRootElement();
        verifyThrowingExceptionWithMissingRootElement();

        verifyThrowingExceptionWithWrongElement();
        verifyThrowingExceptionWithWrongElement2();
    }

    public void verifyEncodedRead()
        throws IOException, XmlPullParserException
    {
        File file = new File( "src/test/verifiers/xpp3/expected-encoding.xml" );

        Reader reader = ReaderFactory.newXmlReader( file );
        MavenXpp3Reader modelReader = new MavenXpp3Reader();

        Model model = modelReader.read( reader );

        Assert.assertEquals( "Maven\u00A9", model.getName() );
    }

    public void verifyThrowingExceptionWithWrongRootElement()
        throws Exception
    {
        File file = new File( "src/test/verifiers/xpp3/model-with-wrong-root-element.xml" );

        Reader reader = ReaderFactory.newXmlReader( file );

        MavenXpp3Reader modelReader = new MavenXpp3Reader();

        try
        {
            modelReader.read( reader );
            throw new VerifierException( "reading model-with-wrong-root-element.xml with strict=true should fail." );
        }
        catch( XmlPullParserException e )
        {
            Assert.assertTrue( e.getMessage().startsWith( "Expected root element 'mavenModel' but found" ) );
        }

        reader = ReaderFactory.newXmlReader( file );

        // no failure if it is wrong but strict is off
        modelReader.read( reader, false );
    }

    public void verifyThrowingExceptionWithMissingRootElement()
        throws Exception
    {
        File file = new File( "src/test/verifiers/xpp3/model-with-missing-root-element.xml" );

        Reader reader = ReaderFactory.newXmlReader( file );

        MavenXpp3Reader modelReader = new MavenXpp3Reader();

        try
        {
            modelReader.read( reader );
            throw new VerifierException( "reading model-with-missing-root-element.xml with strict=true should fail." );
        }
        catch( XmlPullParserException e )
        {
            Assert.assertTrue( e.getMessage().startsWith( "Expected root element 'mavenModel' but found" ) );
        }

        reader = ReaderFactory.newXmlReader( file );

        // no failure if it is wrong but strict is off
        modelReader.read( reader, false );
    }

    public void verifyThrowingExceptionWithWrongElement()
        throws Exception
    {
        File file = new File( "src/test/verifiers/xpp3/model-with-wrong-element.xml" );

        Reader reader = ReaderFactory.newXmlReader( file );

        MavenXpp3Reader modelReader = new MavenXpp3Reader();

        try
        {
            modelReader.read( reader );
            throw new VerifierException( "reading model-with-wrong-element.xml with strict=true should fail." );
        }
        catch( XmlPullParserException e )
        {
            Assert.assertTrue( e.getMessage().startsWith( "Unrecognised tag: 'bar'" ) );
        }

        reader = ReaderFactory.newXmlReader( file );

        // no failure if it is wrong but strict is off
        Model model = modelReader.read( reader, false );

        // check nothing important was missed
        Assert.assertEquals( "connection", model.getScm().getConnection() );
        Assert.assertEquals( "developerConnection", model.getScm().getDeveloperConnection() );
        Assert.assertEquals( "url", model.getScm().getUrl() );
    }

    public void verifyThrowingExceptionWithWrongElement2()
        throws Exception
    {
        File file = new File( "src/test/verifiers/xpp3/model-with-wrong-element2.xml" );

        Reader reader = ReaderFactory.newXmlReader( file );

        MavenXpp3Reader modelReader = new MavenXpp3Reader();

        try
        {
            modelReader.read( reader );
            throw new VerifierException( "reading model-with-wrong-element2.xml with strict=true should fail." );
        }
        catch( XmlPullParserException e )
        {
            Assert.assertTrue( e.getMessage().startsWith( "Unrecognised tag: 'bar'" ) );
        }

        reader = ReaderFactory.newXmlReader( file );

        // no failure if it is wrong but strict is off
        Model model = modelReader.read( reader, false );

        // check nothing important was missed
        Assert.assertEquals( "connection", model.getScm().getConnection() );
        Assert.assertEquals( "developerConnection", model.getScm().getDeveloperConnection() );
        Assert.assertEquals( "url", model.getScm().getUrl() );
    }

    public void verifyWriter()
        throws Exception
    {
        String expectedXml = FileUtils.fileRead( getTestFile( "src/test/verifiers/xpp3/expected.xml" ) );

        // ----------------------------------------------------------------------
        // Build the model thats going to be written.
        // ----------------------------------------------------------------------

        Model expected = new Model();

        expected.setExtend( "/foo/bar" );

        expected.setName( "Maven" );

        expected.setModelVersion( "4.0.0" );

        MailingList mailingList = new MailingList();

        mailingList.setName( "Mailing list" );

        mailingList.setSubscribe( "Super Subscribe" );

        mailingList.setUnsubscribe( "Duper Unsubscribe" );

        mailingList.setArchive( "?ber Archive" );

        expected.addMailingList( mailingList );

        Scm scm = new Scm();

        String connection = "connection";

        String developerConnection = "developerConnection";

        String url = "url";

        scm.setConnection( connection );

        scm.setDeveloperConnection( developerConnection );

        scm.setUrl( url );

        expected.setScm( scm );

        Build build = new Build();

        build.setSourceDirectory( "src/main/java" );

        build.setUnitTestSourceDirectory( "src/test/java" );

        SourceModification sourceModification = new SourceModification();

        sourceModification.setClassName( "excludeEclipsePlugin" );

        sourceModification.setDirectory( "foo" );

        sourceModification.addExclude( "de/abstrakt/tools/codegeneration/eclipse/*.java" );

        build.addSourceModification( sourceModification );

        expected.setBuild( build );

        Component component = new Component();

        component.setName( "component1" );

        expected.addComponent( component );

        component = new Component();

        component.setName( "component2" );

        component.setComment( "comment2" );

        expected.addComponent( component );

        Component c2 = new Component();

        c2.setName( "sub" );

        c2.setComment( "subcomment" );

        component.getComponents().add( c2 );

        component = new Component();

        component.setName( "component3" );

        Xpp3Dom xpp3Dom = new Xpp3Dom( "custom" );
        Xpp3Dom child = new Xpp3Dom( "foo" );
        child.setValue( "bar" );
        xpp3Dom.addChild( child );
        child = new Xpp3Dom( "bar" );
        child.setAttribute( "att1", "value" );
        child.setValue( "baz" );
        xpp3Dom.addChild( child );
        child = new Xpp3Dom( "el1" );
        xpp3Dom.addChild( child );
        Xpp3Dom el1 = child;
        child = new Xpp3Dom( "el2" );
        child.setValue( "te&xt" );
        el1.addChild( child );

        component.setCustom( xpp3Dom );

        expected.addComponent( component );

        component = new Component();
        component.setName( "component4" );
        expected.addComponent( component );

        Properties properties = new Properties();
        properties.setProperty( "name", "value" );
        component.setFlatProperties( properties );

        properties = new Properties();
        properties.setProperty( "key", "theValue" );
        component.setProperties( properties );

        Repository repository = new Repository();
        repository.setId( "foo" );
        expected.addRepository( repository );

        repository = new Repository();
        repository.setId( "bar" );
        expected.addRepository( repository );
        
        ContentTest content = new ContentTest();
        content.setContent( "content value" );
        content.setAttr( "attribute" );
        expected.setContent( content );

        // ----------------------------------------------------------------------
        // Write out the model
        // ----------------------------------------------------------------------

        MavenXpp3Writer writer = new MavenXpp3Writer();

        StringWriter buffer = new StringWriter();

        writer.write( buffer, expected );

        String actualXml = buffer.toString();

//        System.out.println( expectedXml );
//
//        System.err.println( actualXml );

        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreComments( true );
        Diff diff = XMLUnit.compareXML( expectedXml.trim(), actualXml.trim() );

        if ( !diff.identical() )
        {
            System.err.println( actualXml );
            throw new VerifierException( "writer result is not the same as original content: " + diff );
        }

        MavenXpp3Reader reader = new MavenXpp3Reader();

        Model actual = reader.read( new StringReader( actualXml ) );

        Assert.assertNotNull( "Actual", actual );

        assertModel( expected, actual );

        buffer = new StringWriter();

        writer.write( buffer, actual );

        diff = XMLUnit.compareXML( expectedXml.trim(), buffer.toString().trim() );

        if ( !diff.identical() )
        {
            System.err.println( actualXml );
            throw new VerifierException( "re-writer result is not the same as original content: " + diff );
        }
    }

    public void verifyReader()
        throws IOException, XmlPullParserException
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();

        // ----------------------------------------------------------------------
        // Test the "add default entities" flag
        // ----------------------------------------------------------------------

        Assert.assertTrue( reader.getAddDefaultEntities() );

        reader.setAddDefaultEntities( false );

        Assert.assertFalse( reader.getAddDefaultEntities() );

        reader.setAddDefaultEntities( true );

        Assert.assertTrue( reader.getAddDefaultEntities() );

        // ----------------------------------------------------------------------
        // Test that the entities is properly resolved
        // ----------------------------------------------------------------------

        String xml = "<mavenModel>\n" + "  <groupId>Laugst&oslash;l</groupId>\n" + "</mavenModel>";

        Model expected = new Model();

        String groupId = "Laugst\u00f8l";

        expected.setGroupId( groupId );

        Model actual = reader.read( new StringReader( xml ) );

        assertModel( expected, actual );
    }

    public void verifyReaderAliases()
        throws IOException, XmlPullParserException
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();

        String xml = "<mavenModel>\n" + "  <website>http://maven.apache.org/website</website>\n" +
            "  <organisation><name>my-org</name></organisation>\n" + "</mavenModel>";

        Model expected = new Model();

        expected.setUrl( "http://maven.apache.org/website" );

        Organization org = new Organization();

        org.setName( "my-org" );

        expected.setOrganization( org );

        Model actual = reader.read( new StringReader( xml ) );

        assertModel( expected, actual );
    }

    public void verifyReaderDefaultValue()
        throws IOException, XmlPullParserException
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();

        // The defaultValue for local/online is true
        String xml = "<mavenModel>\n" + "  <local><online></online></local>\n" + "</mavenModel>";

        Model expected = new Model();

        Local local = new Local();

        local.setOnline( true );

        expected.setLocal( local );

        Model actual = reader.read( new StringReader( xml ) );

        assertModel( expected, actual );
    }

    public void verifyReaderDuplicates()
        throws IOException, XmlPullParserException
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();

        String xml =
            "<mavenModel>\n" + "  <builder><sourceDirectory /><sourceDirectory /></builder>\n" + "</mavenModel>";

        try
        {
            reader.read( new StringReader( xml ) );
            Assert.fail( "Should have obtained a parse error for duplicate sourceDirectory" );
        }
        catch ( XmlPullParserException expected )
        {
            Assert.assertTrue( true );
        }

        xml = "<mavenModel>\n" + "  <builder><sourceDirectory /></builder>\n" +
            "  <builder><sourceDirectory /></builder>\n" + "</mavenModel>";

        try
        {
            reader.read( new StringReader( xml ) );
            Assert.fail( "Should have obtained a parse error for duplicate build" );
        }
        catch ( XmlPullParserException expected )
        {
            Assert.assertTrue( true );
        }
    }

    public void verifyReaderMissingTags_DefaultMode()
        throws IOException, XmlPullParserException
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();

        // The following is missing the <dependency> and </dependency> tags
        String xml =
            "<mavenModel>\n" + "  <dependencies><groupId>org.apache.cocoon</groupId><artifactId>cocoon-core</artifactId><version>2.2.0-SNAPSHOT</version></dependencies>\n" + "</mavenModel>";

        try
        {
            reader.read( new StringReader( xml ) );
            Assert.fail( "Should have obtained a parse error for missing dependency" );
        }
        catch ( XmlPullParserException expected )
        {
            Assert.assertTrue( true );
        }
    }

    public void verifyReaderMissingTags_StrictMode()
        throws IOException, XmlPullParserException
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();

        // The following is missing the <dependency> and </dependency> tags
        String xml =
            "<mavenModel>\n"
                + "  <dependencies><groupId>org.apache.cocoon</groupId><artifactId>cocoon-core</artifactId><version>2.2.0-SNAPSHOT</version></dependencies>\n"
                + "</mavenModel>";

        try
        {
            reader.read( new StringReader( xml ), true );
            Assert.fail( "Should have obtained a parse error for missing dependency" );
        }
        catch ( XmlPullParserException expected )
        {
            Assert.assertTrue( true );
        }
    }

    public void verifyReaderMissingTags_NonStrictMode()
        throws IOException, XmlPullParserException
    {
        MavenXpp3Reader reader = new MavenXpp3Reader();

        // The following is missing the <dependency> and </dependency> tags
        String xml =
            "<mavenModel>\n"
                + "  <dependencies><groupId>org.apache.cocoon</groupId><artifactId>cocoon-core</artifactId><version>2.2.0-SNAPSHOT</version></dependencies>\n"
                + "</mavenModel>";

        reader.read( new StringReader( xml ), false );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertModel( Model expected, Model actual )
    {
        Assert.assertNotNull( "Actual model", actual );

        Assert.assertEquals( "/model/extend", expected.getExtend(), actual.getExtend() );

//        assertParent( expected.getParent(), actual.getParent() );

        Assert.assertEquals( "/model/modelVersion", expected.getModelVersion(), actual.getModelVersion() );

        Assert.assertEquals( "/model/groupId", expected.getGroupId(), actual.getGroupId() );

        Assert.assertEquals( "/model/artifactId", expected.getArtifactId(), actual.getArtifactId() );

        Assert.assertEquals( "/model/type", expected.getType(), actual.getType() );

        Assert.assertEquals( "/model/name", expected.getName(), actual.getName() );

        Assert.assertEquals( "/model/version", expected.getVersion(), actual.getVersion() );

        Assert.assertEquals( "/model/shortDescription", expected.getShortDescription(), actual.getShortDescription() );

        Assert.assertEquals( "/model/description", expected.getDescription(), actual.getDescription() );

        Assert.assertEquals( "/model/url", expected.getUrl(), actual.getUrl() );

        Assert.assertEquals( "/model/logo", expected.getLogo(), actual.getLogo() );

//        assertIssueManagement();

//        assertCiManagement();

        Assert.assertEquals( "/model/inceptionYear", expected.getInceptionYear(), actual.getInceptionYear() );

//        assertEquals( "/model/siteAddress", expected.getSiteAddress(), actual.getSiteAddress() );

//        assertEquals( "/model/siteDirectory", expected.getSiteDirectory(), actual.getSiteDirectory() );

//        assertEquals( "/model/distributionSite", expected.getDistributionSite(), actual.getDistributionSite() );

//        assertEquals( "/model/distributionDirectory", expected.getDistributionDirectory(), actual.getDistributionDirectory() );

        assertMailingLists( expected.getMailingLists(), actual.getMailingLists() );
/*
        assertDevelopers( );

        assertContributors( );

        assertDependencies( );

        assertLicenses( );

        assertPackageGroups( );

        assertReports( );
*/
        assertScm( expected.getScm(), actual.getScm() );
/*
        assertBuild( );

        assertOrganization( expected.getOrganization(), actual.getOrganization() );
*/
        assertBuild( expected.getBuild(), actual.getBuild() );

        assertLocal( expected.getLocal(), actual.getLocal() );
    }

    public void assertMailingLists( List expected, List actual )
    {
        Assert.assertNotNull( "/model/mailingLists", actual );

        Assert.assertEquals( "/model/mailingLists.size", expected.size(), actual.size() );

        for ( int i = 0; i < expected.size(); i++ )
        {
            assertMailingList( i, (MailingList) expected.get( i ), actual.get( i ) );
        }
    }

    public void assertMailingList( int i, MailingList expected, Object actualObject )
    {
        Assert.assertNotNull( "/model/mailingLists[" + i + "]", actualObject );

        Assert.assertEquals( "/model/mailingLists", MailingList.class, actualObject.getClass() );

        MailingList actual = (MailingList) actualObject;

        Assert.assertEquals( "/model/mailingLists[" + i + "]/name", expected.getName(), actual.getName() );

        Assert.assertEquals( "/model/mailingLists[" + i + "]/subscribe", expected.getSubscribe(),
                             actual.getSubscribe() );

        Assert.assertEquals( "/model/mailingLists[" + i + "]/unsubscribe", expected.getUnsubscribe(),
                             actual.getUnsubscribe() );

        Assert.assertEquals( "/model/mailingLists[" + i + "]/archive", expected.getArchive(), actual.getArchive() );
    }

    public void assertScm( Scm expected, Object actualObject )
    {
        if ( expected == null )
        {
            Assert.assertNull( "/model/scm", actualObject );
        }
        else
        {
            Assert.assertNotNull( "/model/scm", actualObject );

            Assert.assertEquals( "/model/scm", Scm.class, actualObject.getClass() );

            Scm actual = (Scm) actualObject;

            Assert.assertEquals( "/model/scm/connection", expected.getConnection(), actual.getConnection() );

            Assert.assertEquals( "/model/scm/developerConnection", expected.getDeveloperConnection(),
                                 actual.getDeveloperConnection() );

            Assert.assertEquals( "/model/scm/url", expected.getUrl(), actual.getUrl() );
        }
    }

    public void assertBuild( Build expected, Object actualObject )
    {
        if ( expected == null )
        {
            Assert.assertNull( "/model/builder", actualObject );
        }
        else
        {
            Assert.assertNotNull( "/model/builder", actualObject );

            Assert.assertEquals( "/model/builder", Build.class, actualObject.getClass() );

            Build actual = (Build) actualObject;

            Assert.assertEquals( "/model/builder/sourceDirectory", expected.getSourceDirectory(),
                                 actual.getSourceDirectory() );

            Assert.assertEquals( "/model/builder/unitTestSourceDirectory", expected.getUnitTestSourceDirectory(),
                                 actual.getUnitTestSourceDirectory() );
        }
    }

    public void assertLocal( Local expected, Object actualObject )
    {
        if ( expected == null )
        {
            Assert.assertNull( "/model/local", actualObject );
        }
        else
        {
            Assert.assertNotNull( "/model/local", actualObject );

            Assert.assertEquals( "/model/local", Local.class, actualObject.getClass() );

            Local actual = (Local) actualObject;

            Assert.assertEquals( "/model/local/online", expected.isOnline(),
                                 actual.isOnline() );
        }
    }
}
