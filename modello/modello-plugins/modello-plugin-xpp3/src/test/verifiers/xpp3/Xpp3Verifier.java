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

import org.apache.maven.model.Build;
import org.apache.maven.model.Component;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Scm;
import org.apache.maven.model.SourceModification;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.util.FileUtils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
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
        verifyWriter();

        verifyReader();
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

        expected.addComponent( component );

        Repository repository = new Repository();
        repository.setId( "foo" );
        expected.addRepository( repository );

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

        Assert.assertEquals( expectedXml, actualXml );

        MavenXpp3Reader reader = new MavenXpp3Reader();

        Model actual = reader.read( new StringReader( actualXml ) );

        Assert.assertNotNull( "Actual", actual );

        assertModel( expected, actual );
    }

    public void verifyReader()
        throws Exception
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

        String xml = "<project>\n" + "  <groupId>Laugst&oslash;l</groupId>\n" + "</project>";

        Model expected = new Model();

        String groupId = "Laugst\u00f8l";

        expected.setGroupId( groupId );

        Model actual = reader.read( new StringReader( xml ) );

        assertModel( expected, actual );
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

        Assert.assertEquals( "/model/mailingLists[" + i + "]/subscribe", expected.getSubscribe(), actual.getSubscribe() );

        Assert.assertEquals( "/model/mailingLists[" + i + "]/unsubscribe", expected.getUnsubscribe(), actual.getUnsubscribe() );

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

            Assert.assertEquals( "/model/builder/sourceDirectory", expected.getSourceDirectory(), actual.getSourceDirectory() );

            Assert.assertEquals( "/model/builder/unitTestSourceDirectory", expected.getUnitTestSourceDirectory(),
                          actual.getUnitTestSourceDirectory() );
        }
    }
}
