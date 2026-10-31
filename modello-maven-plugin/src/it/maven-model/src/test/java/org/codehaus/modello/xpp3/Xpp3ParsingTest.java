package org.codehaus.modello.xpp3;

import java.io.File;

import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** @author Jason van Zyl */
public class Xpp3ParsingTest
{
    private static String readFailure( String modelFile )
        throws Exception
    {
        File model = new File( System.getProperty( "basedir" ), "src/test/models/" + modelFile );

        MavenXpp3Reader reader = new MavenXpp3Reader();

        XmlPullParserException e =
            assertThrows( XmlPullParserException.class, () -> reader.read( new XmlStreamReader( model ), true ) );
        return e.getMessage();
    }

    @Test
    public void testXpp3ParsingWithModelWithWrongRootTag()
        throws Exception
    {
        String message = readFailure( "model-with-wrong-root-tag.xml" );
        assertTrue( message.startsWith(
            "Expected root element 'project' but found 'model' (position: START_TAG seen <model>... @1:8" ), message );
    }

    @Test
    public void testXpp3ParsingWithModelWithMissingElements()
        throws Exception
    {
        String message = readFailure( "model-with-missing-elements.xml" );
        assertTrue( message.startsWith( "Unrecognised tag: 'groupId' (position: START_TAG seen ...<dependencies>" ),
                    message );
    }

    @Test
    public void testXpp3ParsingWithModelWithPostTags()
        throws Exception
    {
        // internal message from MXParser
        String message = readFailure( "model-with-post-tags.xml" );
        assertTrue( message.startsWith( "start tag not allowed in epilog" ), message );
    }

}
