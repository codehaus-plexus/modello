package org.codehaus.modello.xpp3;

import junit.framework.TestCase;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileReader;

/** @author Jason van Zyl */
public class Xpp3ParsingTest
    extends TestCase
{
    public void testXpp3ParsingWithModelWithWrongRootTag()
        throws Exception
    {
        File model = new File( System.getProperty( "basedir" ), "src/test/models/model-with-wrong-root-tag.xml" );

        MavenXpp3Reader reader = new MavenXpp3Reader();
                
        reader.read( new FileReader( model ), true );
    }

    public void testXpp3ParsingWithModelWithMissingElements()
        throws Exception
    {
        File model = new File( System.getProperty( "basedir" ), "src/test/models/model-with-missing-elements.xml" );

        MavenXpp3Reader reader = new MavenXpp3Reader();

        reader.read( new FileReader( model ), true );
    }

}
