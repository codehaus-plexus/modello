package org.codehaus.modello.maven;

import javax.inject.Inject;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.plexus.build.BuildContext;

/**
 * Creates classes that can convert between different versions of the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Mojo(name = "converters", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ModelloConvertersMojo extends AbstractModelloSourceGeneratorMojo {

    @Inject
    public ModelloConvertersMojo(BuildContext buildContext, ModelloCore modelloCore) {
        super(buildContext, modelloCore);
    }

    protected String getGeneratorType() {
        return "converters";
    }
}
