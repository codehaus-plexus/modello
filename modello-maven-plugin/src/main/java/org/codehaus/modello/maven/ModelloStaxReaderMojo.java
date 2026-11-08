package org.codehaus.modello.maven;

import javax.inject.Inject;

import java.util.Map;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.plexus.build.BuildContext;

/**
 * Creates an StAX reader from the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Mojo(name = "stax-reader", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ModelloStaxReaderMojo extends AbstractModelloSourceGeneratorMojo {

    /**
     * Whether the generated reader checks that the model version of the document matches the version of the reader.
     * Only applies to models with a {@code versionDefinition}.
     *
     * @since 2.9.0
     */
    @Parameter(property = "modello.stax.versionCheck", defaultValue = "true")
    private boolean versionCheck = true;

    @Inject
    public ModelloStaxReaderMojo(BuildContext buildContext, ModelloCore modelloCore) {
        super(buildContext, modelloCore);
    }

    protected String getGeneratorType() {
        return "stax-reader";
    }

    @Override
    protected void customizeParameters(Map<String, Object> parameters) {
        super.customizeParameters(parameters);

        parameters.put(ModelloParameterConstants.STAX_VERSION_CHECK, Boolean.toString(versionCheck));
    }
}
