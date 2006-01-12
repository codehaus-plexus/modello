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

import org.apache.maven.model.v4_0_0.MailingList;
import org.apache.maven.model.v4_0_0.Model;
import org.apache.maven.model.v4_0_0.Scm;
import org.codehaus.modello.verifier.Verifier;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

public class PackageVersionVerifier
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

        try
        {
            scm.setConnection( connection );

            scm.setDeveloperConnection( developerConnection );

            scm.setUrl( url );
        }
        catch( Exception e)
        {
            e.printStackTrace();
            Assert.fail( e.getMessage() );
        }

        Assert.assertEquals( "Scm.connection", connection, scm.getConnection() );

        Assert.assertEquals( "Scm.developerConnection", developerConnection, scm.getDeveloperConnection() );

        Assert.assertEquals( "Scm.url", url, scm.getUrl() );

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

        Assert.assertNotNull( lists );

        Assert.assertTrue( lists instanceof ArrayList );

        try
        {
            model.setMailingLists( expected );
        }
        catch( Exception e)
        {
            e.printStackTrace();
            Assert.fail( e.getMessage() );
        }

        List actual = model.getMailingLists();

        Assert.assertEquals( "/model/mailinglists.size", expected.size(), actual.size() );

        for( int i = 0; i < expected.size(); i++ )
        {
            assertMailingList( (MailingList) expected.get( i ), (MailingList) actual.get( i ) );
        }
    }

    public void testModelAddMailingList()
    {
        Model model = new Model();

        model.addMailingList( createMailingList( 0 ) );

        model.addMailingList( createMailingList( 1 ) );

        model.addMailingList( createMailingList( 2 ) );

        List actual = model.getMailingLists();

        Assert.assertEquals( "/model/mailinglists.size", 3, actual.size() );

        for( int i = 0; i < 3; i++ )
        {
            assertMailingList( createMailingList( i ), (MailingList) actual.get( i ) );
        }
    }

    private MailingList createMailingList( int i )
    {
        MailingList mailingList = new MailingList();

        try
        {
            mailingList.setName( "Mailing list #" + i );

            mailingList.setSubscribe( "Subscribe #" + i );

            mailingList.setUnsubscribe( "Unsubscribe #" + i );

            mailingList.setArchive( "Archive #" + i );
        }
        catch( Exception e)
        {
            e.printStackTrace();
            Assert.fail( e.getMessage() );
        }

        return mailingList;
    }

    private void assertMailingList( MailingList expected, MailingList actual )
    {
        Assert.assertEquals( "Mailing list", expected.getName(), actual.getName() );

        Assert.assertEquals( "Subscribe", expected.getSubscribe(), actual.getSubscribe() );

        Assert.assertEquals( "Unsubscribe", expected.getUnsubscribe(), actual.getUnsubscribe() );

        Assert.assertEquals( "Archive", expected.getArchive(), actual.getArchive() );
    }
}
