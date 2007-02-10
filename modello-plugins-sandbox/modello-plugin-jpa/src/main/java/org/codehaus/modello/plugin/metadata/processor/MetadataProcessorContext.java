/**
 * 
 */
package org.codehaus.modello.plugin.metadata.processor;

import java.io.Writer;
import java.util.HashMap;

import org.codehaus.modello.model.Model;
import org.dom4j.Document;

/**
 * Encapsulates oft-used objects expected by {@link MetadataProcessor}
 * implementations.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id$
 */
public class MetadataProcessorContext
{

    /**
     * Key under which the XML DOM is registered.
     */
    public static final String KEY_DOM_DOCUMENT = "key.document";

    /**
     * Key under which the XML Writer is registered.
     */
    public static final String KEY_WRITER = "key.writer";

    /**
     * Key under which the loaded Modello Model is registered.
     */
    public static final String KEY_MODELLO_MODEL = "key.modello.model";

    /**
     * Holds key-value pairs.
     */
    private HashMap map = new HashMap();

    public void setDocument( Document document )
    {
        map.put( KEY_DOM_DOCUMENT, document );
    }

    public void setWriter( Writer writer )
    {
        map.put( KEY_WRITER, writer );
    }

    public Writer getWriter()
    {
        return (Writer) map.get( KEY_WRITER );
    }

    public Document getDocument()
    {
        return (Document) map.get( KEY_DOM_DOCUMENT );
    }

    public void setModel( Model model )
    {
        map.put( KEY_MODELLO_MODEL, model );
    }

    public Model getModel()
    {
        return (Model) map.get( KEY_MODELLO_MODEL );
    }

}
