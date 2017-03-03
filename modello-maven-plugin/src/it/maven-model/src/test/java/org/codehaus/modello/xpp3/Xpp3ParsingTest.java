package org.codehaus.modello.xpp3;

import static org.hamcrest.CoreMatchers.startsWith;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.hamcrest.core.StringStartsWith;

import java.io.File;

/** @author Jason van Zyl */
public class Xpp3ParsingTest
{
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void testXpp3ParsingWithModelWithWrongRootTag()
        throws Exception
    {
        thrown.expectMessage( startsWith( "Expected root element 'project' but found 'model' (position: START_TAG seen <model>... @1:7)" ) );
        
        File model = new File( System.getProperty( "basedir" ), "src/test/models/model-with-wrong-root-tag.xml" );

        MavenXpp3Reader reader = new MavenXpp3Reader();

        reader.read( ReaderFactory.newXmlReader( model ), true );
    }

    @Test
    public void testXpp3ParsingWithModelWithMissingElements()
        throws Exception
    {
        thrown.expectMessage( startsWith( "Unrecognised tag: 'groupId' (position: START_TAG seen ...<dependencies>" ) );
        
        File model = new File( System.getProperty( "basedir" ), "src/test/models/model-with-missing-elements.xml" );

        MavenXpp3Reader reader = new MavenXpp3Reader();

        reader.read( ReaderFactory.newXmlReader( model ), true );
    }
    
    @Test
    public void testXpp3ParsingWithModelWithPostTags()
        throws Exception
    {
        // internal message from MXParser
        thrown.expectMessage( startsWith( "start tag not allowed in epilog" ) );
        
        File model = new File( System.getProperty( "basedir" ), "src/test/models/model-with-post-tags.xml" );

        MavenXpp3Reader reader = new MavenXpp3Reader();

        reader.read( ReaderFactory.newXmlReader( model ), true );
    }

}
