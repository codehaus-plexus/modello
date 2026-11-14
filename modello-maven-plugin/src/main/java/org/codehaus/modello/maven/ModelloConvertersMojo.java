package org.codehaus.modello.maven;

import javax.inject.Inject;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.modello.core.ModelloCore;

/**
 * Creates classes that can convert between different versions of the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Mojo(name = "converters", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ModelloConvertersMojo extends AbstractModelloSourceGeneratorMojo {

    @Inject
    public ModelloConvertersMojo(ModelloCore modelloCore) {
        super(modelloCore);
    }

    protected String getGeneratorType() {
        return "converters";
    }
}
