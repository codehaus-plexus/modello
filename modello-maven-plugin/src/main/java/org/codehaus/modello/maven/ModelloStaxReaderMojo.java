package org.codehaus.modello.maven;

import javax.inject.Inject;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.plexus.build.BuildContext;

/**
 * Creates an StAX reader from the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Mojo(name = "stax-reader", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ModelloStaxReaderMojo extends AbstractModelloSourceGeneratorMojo {

    @Inject
    public ModelloStaxReaderMojo(BuildContext buildContext, ModelloCore modelloCore) {
        super(buildContext, modelloCore);
    }

    protected String getGeneratorType() {
        return "stax-reader";
    }
}
