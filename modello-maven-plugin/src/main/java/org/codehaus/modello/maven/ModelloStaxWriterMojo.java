package org.codehaus.modello.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Creates an StAX writer from the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Mojo( name = "stax-writer", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModelloStaxWriterMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated StAX writer.
     */
    @Parameter( defaultValue = "${basedir}/target/generated-sources/modello", required = true )
    private File outputDirectory;

    protected String getGeneratorType()
    {
        return "stax-writer";
    }

    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }
}
