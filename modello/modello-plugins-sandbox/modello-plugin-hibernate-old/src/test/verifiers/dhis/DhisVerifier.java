package dhis;

/*
 * LICENSE
 */

import java.io.File;
import java.util.Date;

import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.tool.hbm2ddl.SchemaExport;
import no.uio.dhis.UserInfoRole;
import no.uio.dhis.UserProfile;

import org.codehaus.modello.verifier.Verifier;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DhisVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Configuration configuration = new Configuration();

        configuration.setProperty( "hibernate.dialect", "net.sf.hibernate.dialect.HSQLDialect" );
        configuration.setProperty( "hibernate.connection.driver_class", "org.hsqldb.jdbcDriver" );
        configuration.setProperty( "hibernate.connection.url", "jdbc:hsqldb:." );
        configuration.setProperty( "hibernate.connection.username", "sa" );
        configuration.setProperty( "hibernate.connection.password", "" );
        configuration.setProperty( "hibernate.connection.pool_size", "10" );
        configuration.setProperty( "hibernate.connection.show_sql", "true" );

        File file = getTestFile( "target/dhis/classes/dhis.hbm.xml" );

        assertTrue( file.exists() );

        configuration.addFile( file );

        SchemaExport exporter = new SchemaExport( configuration );

        exporter.create( false, true );

        SessionFactory sessionFactory;

        sessionFactory = configuration.buildSessionFactory();

        // This extra sessions is kept open because HSQL will terminate when the 
        // last connection is closed
        Session masterSession = sessionFactory.openSession();

        // ----------------------------------------------------------------------
        // Persist some objects
        // ----------------------------------------------------------------------

        Session session = sessionFactory.openSession();

        Transaction tx = session.beginTransaction();

        UserInfoRole userInfoRole = new UserInfoRole();

        String name = "foo";

        String lastUpdated = new Date().toString();

        userInfoRole.setName( name );

        userInfoRole.setLastUpdated( lastUpdated );

        UserProfile userProfile = new UserProfile();

        String username = "hubba";

        String firstname = "bubba";

        String surname = "topper";

        userProfile.setUserName( username );

        userProfile.setSurname( surname );

        userProfile.setFirstName( firstname );

        userProfile.addUserInfoRole( userInfoRole );

        int userInfoRoleId = ((Integer) session.save( userInfoRole )).intValue();

        int userProfileId = ((Integer) session.save( userProfile )).intValue();

        session.flush();

        tx.commit();

        session.close();

        // ----------------------------------------------------------------------
        // Load the objects again
        // ----------------------------------------------------------------------

        session = sessionFactory.openSession();

        tx = session.beginTransaction();

        userProfile = (UserProfile) session.load( UserProfile.class, new Integer( userProfileId ) );

        assertEquals( userProfileId, userProfile.getId() );

        assertEquals( username, userProfile.getUserName() );

        assertEquals( surname, userProfile.getSurname() );

        assertEquals( firstname, userProfile.getFirstName() );

        assertEquals( 1, userProfile.getUserInfoRoles().size() );

        userInfoRole = (UserInfoRole) userProfile.getUserInfoRoles().get( 0 );

        assertEquals( userInfoRoleId, userInfoRole.getId() );

        assertEquals( name, userInfoRole.getName() );

        assertEquals( lastUpdated, userInfoRole.getLastUpdated() );

        tx.commit();

        masterSession.close();
    }
}
