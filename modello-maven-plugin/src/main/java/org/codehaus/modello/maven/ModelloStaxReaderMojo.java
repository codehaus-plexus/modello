package org.codehaus.modello.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Creates an StAX reader from the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Mojo( name = "stax-reader", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModelloStaxReaderMojo
    extends AbstractModelloSourceGeneratorMojo
{
    protected String getGeneratorType()
    {
        return "stax-reader";
    }
}
