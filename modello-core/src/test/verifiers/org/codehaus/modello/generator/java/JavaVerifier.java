package org.codehaus.modello.generator.java;

import org.apache.maven.model.*;

import org.codehaus.modello.Verifier;

public class JavaVerifier
{
    private void assertEquals( Object expected, Object actual)
    {
        if ( expected.equals( actual ) )
        {
            return;
        }

        throw new RuntimeException( "Assertion error: expected '" + expected.toString() + "', actual: '" + actual + "'." );
    }

    public void verify()
    {
        Model model = new Model();

        Scm scm = new Scm();

        String connection = "connection";

        String developerConnection = "developerConnection";

        String url = "url";

        scm.setConnection( connection );

        scm.setDeveloperConnection( developerConnection );

        scm.setUrl( url );

        assertEquals( connection, scm.getConnection() );

        assertEquals( developerConnection, scm.getDeveloperConnection() );

        assertEquals( url, scm.getUrl() );
    }
}
