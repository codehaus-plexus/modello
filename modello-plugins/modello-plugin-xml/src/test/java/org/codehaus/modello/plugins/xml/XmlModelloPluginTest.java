package org.codehaus.modello.plugins.xml;

/*
 * LICENSE
 */

import java.io.File;

import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloTestCase;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class XmlModelloPluginTest
    extends ModelloTestCase
{
    public void testXmlPlugin()
        throws Exception
    {
        Modello modello = new Modello();

        modello.initialize();

        String output = getTestFile( "target/output" );

        new File( output ).mkdirs();

        modello.work( getTestFile( "src/test/resources/model.mdo" ), "xml", output, "1.0", true );
    }
}
