package org.codehaus.modello.generator.java;

import java.util.*;
import org.apache.maven.model.*;
import org.codehaus.modello.generator.*;
import org.codehaus.modello.verifier.*;

public class JavaVerifier
    extends Verifier
{
    public void verify()
    {
        Model model = new Model();

        // The SCM tests one class that has a reference to another class.
        Scm scm = new Scm();

        String connection = "connection";

        String developerConnection = "developerConnection";

        String url = "url";

        scm.setConnection( connection );

        scm.setDeveloperConnection( developerConnection );

        scm.setUrl( url );

        assertEquals( "Scm.connection", connection, scm.getConnection() );

        assertEquals( "Scm.developerConnection", developerConnection, scm.getDeveloperConnection() );

        assertEquals( "Scm.url", url, scm.getUrl() );

        testMailingLists();
    }

    private void testMailingLists()
    {
        List expected = new ArrayList();

        expected.add( createMailingList( 0 ) );

        expected.add( createMailingList( 1 ) );

        expected.add( createMailingList( 2 ) );

        Model model = new Model();

        List lists = model.getMailingLists();

        assertNotNull( lists );

        assertTrue( lists instanceof ArrayList );

        model.setMailingLists( expected );

        List actual = model.getMailingLists();

        assertEquals( "/model/mailinglists.size", expected.size(), actual.size() );

        for( int i = 0; i < expected.size(); i++ )
        {
            assertMailingList( (MailingList) expected.get( i ), (MailingList) actual.get( i ) );
        }
    }

    public void testModelAddMailingList()
        throws Exception
    {
        Model model = new Model();

        model.addMailingList( createMailingList( 0 ) );

        model.addMailingList( createMailingList( 1 ) );

        model.addMailingList( createMailingList( 2 ) );

        List actual = model.getMailingLists();

        assertEquals( "/model/mailinglists.size", 3, actual.size() );

        for( int i = 0; i < 3; i++ )
        {
            assertMailingList( createMailingList( i ), (MailingList) actual.get( i ) );
        }
    }

    private MailingList createMailingList( int i )
    {
        MailingList mailingList = new MailingList();

        mailingList.setName( "Mailing list #" + i );

        mailingList.setSubscribe( "Subscribe #" + i );

        mailingList.setUnsubscribe( "Unsubscribe #" + i );

        mailingList.setArchive( "Archive #" + i );

        return mailingList;
    }

    private void assertMailingList( MailingList expected, MailingList actual )
    {
        assertEquals( "Mailing list", expected.getName(), actual.getName() );

        assertEquals( "Subscribe", expected.getSubscribe(), actual.getSubscribe() );

        assertEquals( "Unsubscribe", expected.getUnsubscribe(), actual.getUnsubscribe() );

        assertEquals( "Archive", expected.getArchive(), actual.getArchive() );
    }
}
