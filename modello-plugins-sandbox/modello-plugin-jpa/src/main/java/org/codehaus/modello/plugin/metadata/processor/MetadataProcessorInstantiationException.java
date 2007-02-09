/**
 * 
 */
package org.codehaus.modello.plugin.metadata.processor;

/**
 * Wraps up cases where a {@link MetadataProcessor} could not be instantiated.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @since 1.0.0
 * @version $Id: MetadataProcessorInstantiationException.java 794 2007-02-03
 *          22:03:26Z rahul $
 */
public class MetadataProcessorInstantiationException
    extends Throwable
{

    /**
     * Default.
     */
    public MetadataProcessorInstantiationException()
    {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public MetadataProcessorInstantiationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * @param message
     */
    public MetadataProcessorInstantiationException( String message )
    {
        super( message );
    }

    /**
     * @param cause
     */
    public MetadataProcessorInstantiationException( Throwable cause )
    {
        super( cause );
    }

}
