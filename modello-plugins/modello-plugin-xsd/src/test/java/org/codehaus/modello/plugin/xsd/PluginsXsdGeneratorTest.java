package org.codehaus.modello.plugin.xsd;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import java.io.IOException;
import java.util.Properties;

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelValidationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** 
 * Test that the <a href="https://github.com/apache/maven/blob/master/maven-plugin-api/src/main/mdo/plugin.mdo">Maven plugin descriptor</a>
 * generates an XSD which can validate plugin descriptors (with arbitrary element names below {@code <configuration>}).
 * @see <a href="https://github.com/codehaus-plexus/modello/issues/264">Issue 264</a>
 *
 */
public class PluginsXsdGeneratorTest
    extends AbstractModelloGeneratorTest
{

    public PluginsXsdGeneratorTest()
    {
        super( "plugins" );
    }

    @Test
    public void testWithNameWildcard()
        throws ModelloException, ModelValidationException, IOException, ComponentLookupException,
        ParserConfigurationException, SAXException
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/plugin.mdo" ) );

        // generate XSD file
        Properties parameters = getModelloParameters( "1.0.0" );

        modello.generate( model, "xsd", parameters );

        SAXParser parser = createSaxParserWithSchema( "plugin-1.0.0.xsd" );
        parser.parse( getClass().getResourceAsStream( "/plugin.xml" ), new Handler() );
    }

    private static class Handler
        extends DefaultHandler
    {
        public void warning( SAXParseException e )
            throws SAXException
        {
            throw e;
        }

        public void error( SAXParseException e )
            throws SAXException
        {
            throw e;
        }
    }
}
