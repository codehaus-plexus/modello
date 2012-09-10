package org.codehaus.modello.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Creates an StAX writer from the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Mojo( name = "stax-writer", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModelloStaxWriterMojo
    extends AbstractModelloSourceGeneratorMojo
{
    protected String getGeneratorType()
    {
        return "stax-writer";
    }
}
