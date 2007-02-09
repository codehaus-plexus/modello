/**
 * 
 */
package org.codehaus.modello.plugin.metadata.jpa.processors;

import org.codehaus.modello.plugin.metadata.jpa.EntityMetadata;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessor;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorContext;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id$
 */
public class EntityMetadataProcessorTest
    extends PlexusTestCase
{

    public void testLookup()
        throws Exception
    {
        EntityMetadataProcessor processor = (EntityMetadataProcessor) lookup( MetadataProcessor.ROLE, "entity" );
        assertNotNull( processor );
    }

    public void testValidateMetadata()
        throws Exception
    {
        EntityMetadataProcessor processor = (EntityMetadataProcessor) lookup( MetadataProcessor.ROLE, "entity" );
        assertNotNull( processor );

        // TODO: Implement validate()
        boolean valid = processor.validate( new MetadataProcessorContext(), new EntityMetadata() );

        assertTrue( "Invalid metadata", valid );
    }

}
