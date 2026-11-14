package org.codehaus.modello.maven;

import javax.inject.Inject;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.modello.core.ModelloCore;

/**
 * Creates a DOM4J reader from the model.
 */
@Mojo(name = "dom4j-reader", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ModelloDom4jReaderMojo extends AbstractModelloSourceGeneratorMojo {

    @Inject
    public ModelloDom4jReaderMojo(ModelloCore modelloCore) {
        super(modelloCore);
    }

    protected String getGeneratorType() {
        return "dom4j-reader";
    }
}
