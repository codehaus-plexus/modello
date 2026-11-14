package org.codehaus.modello.maven;

import javax.inject.Inject;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.modello.core.ModelloCore;

/**
 * Creates a DOM4J writer from the model.
 */
@Mojo(name = "dom4j-writer", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ModelloDom4jWriterMojo extends AbstractModelloSourceGeneratorMojo {

    @Inject
    public ModelloDom4jWriterMojo(ModelloCore modelloCore) {
        super(modelloCore);
    }

    protected String getGeneratorType() {
        return "dom4j-writer";
    }
}
