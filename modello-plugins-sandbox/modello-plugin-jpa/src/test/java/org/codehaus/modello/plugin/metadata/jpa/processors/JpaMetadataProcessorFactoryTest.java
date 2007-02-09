/**
 * 
 */
package org.codehaus.modello.plugin.metadata.jpa.processors;

import org.codehaus.modello.plugin.metadata.jpa.EntityMetadata;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessor;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorFactory;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorInstantiationException;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id$
 */
public class JpaMetadataProcessorFactoryTest
    extends PlexusTestCase
{

    public void testMetadataProcessorFactoryLookup()
        throws Exception
    {
        JpaMetadataProcessorFactory factory = (JpaMetadataProcessorFactory) lookup( MetadataProcessorFactory.ROLE,
                                                                                    "jpa" );
        assertNotNull( factory );
    }

    public void testCreateMetadataProcessor()
        throws Exception, MetadataProcessorInstantiationException
    {
        JpaMetadataProcessorFactory factory = (JpaMetadataProcessorFactory) lookup( MetadataProcessorFactory.ROLE,
                                                                                    "jpa" );
        assertNotNull( factory );

        MetadataProcessor processor = factory.createMetadataProcessor( new EntityMetadata() );

        assertNotNull( processor );
        assertTrue( processor instanceof EntityMetadataProcessor );
    }

}
