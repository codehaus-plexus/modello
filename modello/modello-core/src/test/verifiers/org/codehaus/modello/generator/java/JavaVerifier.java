package org.codehaus.modello.generator.java;

import org.apache.maven.model.*;
import org.codehaus.modello.generator.*;

public class JavaVerifier
    extends AbstractVerifier
{
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

        assertEquals( "Connection", connection, scm.getConnection() );

        assertEquals( "DeveloperConnection", developerConnection, scm.getDeveloperConnection() );

        assertEquals( "Url", url, scm.getUrl() );
    }
}
