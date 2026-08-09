package org.codehaus.modello.generator.jackson;

/*
 * Copyright (c) 2013, Codehaus.org
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
import org.codehaus.modello.test.model.io.jackson.MavenJacksonReader;
import org.codehaus.modello.test.model.io.jackson.MavenJacksonWriter;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.modello.verifier.VerifierException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 */
public class JacksonVerifier
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
    }

    public void verifyWriter()
        throws Exception
    {
        String expectedJson = FileUtils.fileRead( getTestFile( "src/test/verifiers/jackson/expected.json" ) );

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

        // DOM

        ObjectNode custom = JsonNodeFactory.instance.objectNode();
        custom.put( "foo", "bar" );

        ObjectNode child = JsonNodeFactory.instance.objectNode();
        child.put( "att1", "value" );
        child.put( "content", "baz" );
        custom.put( "bar", child );

        ObjectNode el1 = JsonNodeFactory.instance.objectNode();
        el1.put( "el2", "te&xt" );

        custom.put( "el1", el1 );

        custom.putArray( "excludes" ).add( "*.vlt" ).add( "*.xml" );

        component.setCustom( custom );

        expected.addComponent( component );

        // end DOM

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

        MavenJacksonWriter writer = new MavenJacksonWriter();

        StringWriter buffer = new StringWriter();

        writer.write( buffer, expected );

        String actualJson = buffer.toString();

        compareJsonText( expectedJson, actualJson );

        // Test the reader

        MavenJacksonReader reader = new MavenJacksonReader();

        Model actual = reader.read( new StringReader( expectedJson ) );

        assertNotNull( actual , "Actual");

        assertModel( expected, actual );

        buffer = new StringWriter();

        writer.write( buffer, actual );

        // test the re-writer result

        compareJsonText( expectedJson, buffer.toString() );
    }

    private void compareJsonText( String expectedJson, String actualJson )
        throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode expected = mapper.readTree( expectedJson.trim() );
        JsonNode actual = mapper.readTree( actualJson.trim() );

        assertEquals( expected, actual );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    public void assertModel( Model expected, Model actual )
    {
        assertNotNull( actual , "Actual model");

        assertEquals( expected.getExtend(), actual.getExtend() , "/model/extend");

//        assertParent( expected.getParent(), actual.getParent() );

        assertEquals( expected.getModelVersion(), actual.getModelVersion() , "/model/modelVersion");

        assertEquals( expected.getGroupId(), actual.getGroupId() , "/model/groupId");

        assertEquals( expected.getArtifactId(), actual.getArtifactId() , "/model/artifactId");

        assertEquals( expected.getType(), actual.getType() , "/model/type");

        assertEquals( expected.getName(), actual.getName() , "/model/name");

        assertEquals( expected.getVersion(), actual.getVersion() , "/model/version");

        assertEquals( expected.getShortDescription(), actual.getShortDescription() , "/model/shortDescription");

        assertEquals( expected.getDescription(), actual.getDescription() , "/model/description");

        assertEquals( expected.getUrl(), actual.getUrl() , "/model/url");

        assertEquals( expected.getLogo(), actual.getLogo() , "/model/logo");

//        assertIssueManagement();

//        assertCiManagement();

        assertEquals( expected.getInceptionYear(), actual.getInceptionYear() , "/model/inceptionYear");

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

        assertLocal( expected.getLocal(), actual.getLocal() );
    }

    public void assertMailingLists( List expected, List actual )
    {
        assertNotNull( actual , "/model/mailingLists");

        assertEquals( expected.size(), actual.size() , "/model/mailingLists.size");

        for ( int i = 0; i < expected.size(); i++ )
        {
            assertMailingList( i, (MailingList) expected.get( i ), actual.get( i ) );
        }
    }

    public void assertMailingList( int i, MailingList expected, Object actualObject )
    {
        assertNotNull( actualObject , "/model/mailingLists[" + i + "]");

        assertEquals( MailingList.class, actualObject.getClass() , "/model/mailingLists");

        MailingList actual = (MailingList) actualObject;

        assertEquals( expected.getName(), actual.getName() , "/model/mailingLists[" + i + "]/name");

        assertEquals( expected.getSubscribe(),
                             actual.getSubscribe() , "/model/mailingLists[" + i + "]/subscribe");

        assertEquals( expected.getUnsubscribe(),
                             actual.getUnsubscribe() , "/model/mailingLists[" + i + "]/unsubscribe");

        assertEquals( expected.getArchive(), actual.getArchive() , "/model/mailingLists[" + i + "]/archive");
    }

    public void assertScm( Scm expected, Object actualObject )
    {
        if ( expected == null )
        {
            assertNull( actualObject , "/model/scm");
        }
        else
        {
            assertNotNull( actualObject , "/model/scm");

            assertEquals( Scm.class, actualObject.getClass() , "/model/scm");

            Scm actual = (Scm) actualObject;

            assertEquals( expected.getConnection(), actual.getConnection() , "/model/scm/connection");

            assertEquals( expected.getDeveloperConnection(),
                                 actual.getDeveloperConnection() , "/model/scm/developerConnection");

            assertEquals( expected.getUrl(), actual.getUrl() , "/model/scm/url");
        }
    }

    public void assertBuild( Build expected, Object actualObject )
    {
        if ( expected == null )
        {
            assertNull( actualObject , "/model/builder");
        }
        else
        {
            assertNotNull( actualObject , "/model/builder");

            assertEquals( Build.class, actualObject.getClass() , "/model/builder");

            Build actual = (Build) actualObject;

            assertEquals( expected.getSourceDirectory(),
                                 actual.getSourceDirectory() , "/model/builder/sourceDirectory");

            assertEquals( expected.getUnitTestSourceDirectory(),
                                 actual.getUnitTestSourceDirectory() , "/model/builder/unitTestSourceDirectory");
        }
    }

    public void assertLocal( Local expected, Object actualObject )
    {
        if ( expected == null )
        {
            assertNull( actualObject , "/model/local");
        }
        else
        {
            assertNotNull( actualObject , "/model/local");

            assertEquals( Local.class, actualObject.getClass() , "/model/local");

            Local actual = (Local) actualObject;

            assertEquals( expected.isOnline(),
                                 actual.isOnline() , "/model/local/online");
        }
    }
}
