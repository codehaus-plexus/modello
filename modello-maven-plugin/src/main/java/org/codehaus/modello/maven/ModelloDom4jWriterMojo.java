package org.codehaus.modello.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Creates a DOM4J writer from the model.
 */
@Mojo( name = "dom4j-writer", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModelloDom4jWriterMojo
    extends AbstractModelloSourceGeneratorMojo
{
    protected String getGeneratorType()
    {
        return "dom4j-writer";
    }
}
