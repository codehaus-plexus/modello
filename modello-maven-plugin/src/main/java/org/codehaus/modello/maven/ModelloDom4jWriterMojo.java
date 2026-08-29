package org.codehaus.modello.maven;

import javax.inject.Inject;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.plexus.build.BuildContext;

/**
 * Creates a DOM4J writer from the model.
 */
@Mojo(name = "dom4j-writer", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ModelloDom4jWriterMojo extends AbstractModelloSourceGeneratorMojo {

    @Inject
    public ModelloDom4jWriterMojo(BuildContext buildContext, ModelloCore modelloCore) {
        super(buildContext, modelloCore);
    }

    protected String getGeneratorType() {
        return "dom4j-writer";
    }
}
