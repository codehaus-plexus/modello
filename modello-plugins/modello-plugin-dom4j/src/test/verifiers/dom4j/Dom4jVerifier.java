package org.codehaus.modello.generator.xml.dom4j;

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

import org.junit.jupiter.api.Assertions;
import org.codehaus.modello.test.model.Build;
import org.codehaus.modello.test.model.Component;
import org.codehaus.modello.test.model.MailingList;
import org.codehaus.modello.test.model.Model;
import org.codehaus.modello.test.model.Organization;
import org.codehaus.modello.test.model.Repository;
import org.codehaus.modello.test.model.Scm;
import org.codehaus.modello.test.model.SourceModification;
import org.codehaus.modello.test.model.io.dom4j.MavenDom4jReader;
import org.codehaus.modello.test.model.io.dom4j.MavenDom4jWriter;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class Dom4jVerifier
    extends Verifier
{
    /**
     * TODO: Add a association thats not under the root element
     */
    public void verify()
        throws IOException, DocumentException
    {
        verifyReader();

        verifyReaderAliases();

        verifyReaderDuplicates();

        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
        
        verifyWriter();
    }

    public void verifyEncodedRead()
        throws IOException, DocumentException
    {
        String path = "src/test/verifiers/dom4j/expected-encoding.xml";

        Reader reader = new XmlStreamReader( new File( path ) );
        MavenDom4jReader modelReader = new MavenDom4jReader();

        Model model = modelReader.read( reader );

        Assertions.assertEquals( "Maven\u00A9", model.getName() );
    }

    public void verifyWriter()
        throws IOException, DocumentException
    {
        String expectedXml = FileUtils.fileRead( getTestFile( "src/test/verifiers/dom4j/expected.xml" ) );
        expectedXml = expectedXml.replaceAll( "(\r\n)|(\r)", "\n" );

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
        child.setValue( "text" );
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

        // ----------------------------------------------------------------------
        // Write out the model
        // ----------------------------------------------------------------------

        MavenDom4jWriter writer = new MavenDom4jWriter();

        StringWriter buffer = new StringWriter();

        writer.write( buffer, expected );

        String actualXml = buffer.toString();
        actualXml = actualXml.replaceAll( "(\r\n)|(\r)", "\n" );

        Assertions.assertEquals( expectedXml.trim(), actualXml.trim() );

        MavenDom4jReader reader = new MavenDom4jReader();

        Model actual = reader.read( new StringReader( actualXml ) );

        Assertions.assertNotNull( actual , "Actual");

        assertModel( expected, actual );

        buffer = new StringWriter();

        writer.write( buffer, actual );

        Assertions.assertEquals( expectedXml.trim(), buffer.toString().trim().replaceAll( "(\r\n)|(\r)", "\n" ) );
    }

    public void verifyReader()
        throws IOException, DocumentException
    {
        MavenDom4jReader reader = new MavenDom4jReader();

        // ----------------------------------------------------------------------
        // Test that the entities is properly resolved
        // ----------------------------------------------------------------------

        String xml = "<!DOCTYPE mavenModel [\n" +
            "  <!ENTITY oslash \"&#248;\">\n" +
            "]>\n<mavenModel>\n" + "  <groupId>Laugst&oslash;l</groupId>\n" + "</mavenModel>";

        Model expected = new Model();

        String groupId = "Laugst\u00f8l";

        expected.setGroupId( groupId );

        Model actual = reader.read( new StringReader( xml ) );

        assertModel( expected, actual );
    }

    public void verifyReaderAliases()
        throws IOException, DocumentException
    {
        MavenDom4jReader reader = new MavenDom4jReader();

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

    public void verifyReaderDuplicates()
        throws IOException, DocumentException
    {
        MavenDom4jReader reader = new MavenDom4jReader();

        String xml =
            "<mavenModel>\n" + "  <builder><sourceDirectory /><sourceDirectory /></builder>\n" + "</mavenModel>";

/* TODO
        try
        {
            reader.read( new StringReader( xml ) );
            Assertions.fail( "Should have obtained a parse error for duplicate sourceDirectory" );
        }
        catch ( DocumentException expected )
        {
            Assertions.assertTrue( true );
        }

        xml = "<mavenModel>\n" + "  <builder><sourceDirectory /></builder>\n" +
            "  <builder><sourceDirectory /></builder>\n" + "</mavenModel>";

        try
        {
            reader.read( new StringReader( xml ) );
            Assertions.fail( "Should have obtained a parse error for duplicate build" );
        }
        catch ( DocumentException expected )
        {
            Assertions.assertTrue( true );
        }
        */
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertModel( Model expected, Model actual )
    {
        Assertions.assertNotNull( actual , "Actual model");

        Assertions.assertEquals( expected.getExtend(), actual.getExtend() , "/model/extend");

//        assertParent( expected.getParent(), actual.getParent() );

        Assertions.assertEquals( expected.getModelVersion(), actual.getModelVersion() , "/model/modelVersion");

        Assertions.assertEquals( expected.getGroupId(), actual.getGroupId() , "/model/groupId");

        Assertions.assertEquals( expected.getArtifactId(), actual.getArtifactId() , "/model/artifactId");

        Assertions.assertEquals( expected.getType(), actual.getType() , "/model/type");

        Assertions.assertEquals( expected.getName(), actual.getName() , "/model/name");

        Assertions.assertEquals( expected.getVersion(), actual.getVersion() , "/model/version");

        Assertions.assertEquals( expected.getShortDescription(), actual.getShortDescription() , "/model/shortDescription");

        Assertions.assertEquals( expected.getDescription(), actual.getDescription() , "/model/description");

        Assertions.assertEquals( expected.getUrl(), actual.getUrl() , "/model/url");

        Assertions.assertEquals( expected.getLogo(), actual.getLogo() , "/model/logo");

//        assertIssueManagement();

//        assertCiManagement();

        Assertions.assertEquals( expected.getInceptionYear(), actual.getInceptionYear() , "/model/inceptionYear");

//        assertEquals( expected.getSiteAddress(), actual.getSiteAddress() , "/model/siteAddress");

//        assertEquals( expected.getSiteDirectory(), actual.getSiteDirectory() , "/model/siteDirectory");

//        assertEquals( expected.getDistributionSite(), actual.getDistributionSite() , "/model/distributionSite");

//        assertEquals( expected.getDistributionDirectory(), actual.getDistributionDirectory() , "/model/distributionDirectory");

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
    }

    public void assertMailingLists( List expected, List actual )
    {
        Assertions.assertNotNull( actual , "/model/mailingLists");

        Assertions.assertEquals( expected.size(), actual.size() , "/model/mailingLists.size");

        for ( int i = 0; i < expected.size(); i++ )
        {
            assertMailingList( i, (MailingList) expected.get( i ), actual.get( i ) );
        }
    }

    public void assertMailingList( int i, MailingList expected, Object actualObject )
    {
        Assertions.assertNotNull( actualObject , "/model/mailingLists[" + i + "]");

        Assertions.assertEquals( MailingList.class, actualObject.getClass() , "/model/mailingLists");

        MailingList actual = (MailingList) actualObject;

        Assertions.assertEquals( expected.getName(), actual.getName() , "/model/mailingLists[" + i + "]/name");

        Assertions.assertEquals( expected.getSubscribe(),
                             actual.getSubscribe() , "/model/mailingLists[" + i + "]/subscribe");

        Assertions.assertEquals( expected.getUnsubscribe(),
                             actual.getUnsubscribe() , "/model/mailingLists[" + i + "]/unsubscribe");

        Assertions.assertEquals( expected.getArchive(), actual.getArchive() , "/model/mailingLists[" + i + "]/archive");
    }

    public void assertScm( Scm expected, Object actualObject )
    {
        if ( expected == null )
        {
            Assertions.assertNull( actualObject , "/model/scm");
        }
        else
        {
            Assertions.assertNotNull( actualObject , "/model/scm");

            Assertions.assertEquals( Scm.class, actualObject.getClass() , "/model/scm");

            Scm actual = (Scm) actualObject;

            Assertions.assertEquals( expected.getConnection(), actual.getConnection() , "/model/scm/connection");

            Assertions.assertEquals( expected.getDeveloperConnection(),
                                 actual.getDeveloperConnection() , "/model/scm/developerConnection");

            Assertions.assertEquals( expected.getUrl(), actual.getUrl() , "/model/scm/url");
        }
    }

    public void assertBuild( Build expected, Object actualObject )
    {
        if ( expected == null )
        {
            Assertions.assertNull( actualObject , "/model/builder");
        }
        else
        {
            Assertions.assertNotNull( actualObject , "/model/builder");

            Assertions.assertEquals( Build.class, actualObject.getClass() , "/model/builder");

            Build actual = (Build) actualObject;

            Assertions.assertEquals( expected.getSourceDirectory(),
                                 actual.getSourceDirectory() , "/model/builder/sourceDirectory");

            Assertions.assertEquals( expected.getUnitTestSourceDirectory(),
                                 actual.getUnitTestSourceDirectory() , "/model/builder/unitTestSourceDirectory");
        }
    }
}
