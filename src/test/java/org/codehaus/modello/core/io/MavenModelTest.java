package org.codehaus.modello.core.io;

/*
 * LICENSE
 */

import java.io.FileReader;

import org.codehaus.modello.ModelloTest;

/**
 * This test uses the maven model as this is the most complete and by far the
 * biggest model using modello.
 * 
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenModelTest
    extends ModelloTest
{
    public void testMavenModel()
        throws Exception
    {
        ModelReader reader = new ModelReader();

        reader.loadModel( new FileReader( getTestPath( "src/test/resources/models/maven.mdo" ) ) );
    }
}
