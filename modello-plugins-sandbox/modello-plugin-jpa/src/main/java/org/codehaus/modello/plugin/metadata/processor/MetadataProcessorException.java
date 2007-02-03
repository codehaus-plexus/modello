/**
 * 
 */
package org.codehaus.modello.plugin.metadata.processor;

/**
 * Wraps up exception cases that occur when a {@link MetadataProcessor} is
 * processing metadata.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 */
public class MetadataProcessorException
    extends Throwable
{

    /**
     * Default.
     */
    public MetadataProcessorException()
    {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public MetadataProcessorException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * @param message
     */
    public MetadataProcessorException( String message )
    {
        super( message );
    }

    /**
     * @param cause
     */
    public MetadataProcessorException( Throwable cause )
    {
        super( cause );
    }

}
