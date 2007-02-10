/**
 * 
 */
package org.codehaus.modello.plugin.metadata.jpa.processors;

import java.util.List;

import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.plugin.metadata.processor.ClassMetadataProcessorMetadata;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessor;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorContext;
import org.codehaus.modello.plugin.metadata.processor.MetadataProcessorException;
import org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since
 * @version $Id$
 * @plexus.component role="org.codehaus.modello.plugin.metadata.processor.MetadataProcessor"
 *                   role-hint="table"
 */
public class TableMetadataProcessor
    implements MetadataProcessor
{

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.MetadataProcessor#process(MetadataProcessorContext,
     *      org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata)
     */
    public void process( MetadataProcessorContext context, ProcessorMetadata metadata )
        throws MetadataProcessorException
    {
        System.out.println( "Processing metadata : " + metadata.getKey() );
        ModelClass modelClass = ( (ClassMetadataProcessorMetadata) metadata ).getModelClass();
        Document doc = context.getDocument();        
        XPath expression = DocumentHelper
            .createXPath( "/entity-mappings/entity[@class='" + modelClass.getName() + "']" );
        Node node = expression.selectSingleNode( doc );
        if ( node.getNodeType() != Node.ELEMENT_NODE )
        {
            throw new MetadataProcessorException( "No matching element could be located for name: "
                + modelClass.getName() );
        }

        Element tableElt = ( (Element) node ).addElement( "table" );
        // FIXME:
        tableElt.addAttribute( "name", "test" );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.modello.plugin.metadata.processor.MetadataProcessor#validate(MetadataProcessorContext,
     *      org.codehaus.modello.plugin.metadata.processor.ProcessorMetadata)
     */
    public boolean validate( MetadataProcessorContext context, ProcessorMetadata metadata )
    {
        ModelClass modelClass = ( (ClassMetadataProcessorMetadata) metadata ).getModelClass();
        Document doc = context.getDocument();
        // TODO Contribute to the Document here.
        Element rootElement = doc.getRootElement();
        XPath expression = DocumentHelper
            .createXPath( "/entity-mappings/entity[@class='" + modelClass.getName() + "']" );
        Node node = expression.selectSingleNode( doc );
        return ( null != node && ( node.getNodeType() != Node.ELEMENT_NODE ) );
    }
}
