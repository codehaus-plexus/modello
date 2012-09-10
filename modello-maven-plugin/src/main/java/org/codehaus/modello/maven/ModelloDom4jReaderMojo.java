package org.codehaus.modello.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Creates a DOM4J reader from the model.
 */
@Mojo( name = "dom4j-reader", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModelloDom4jReaderMojo
    extends AbstractModelloSourceGeneratorMojo
{
    protected String getGeneratorType()
    {
        return "dom4j-reader";
    }
}
