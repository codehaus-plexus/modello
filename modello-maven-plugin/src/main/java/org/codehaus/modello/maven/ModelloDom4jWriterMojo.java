package org.codehaus.modello.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Creates a DOM4J writer from the model.
 */
@Mojo( name = "dom4j-writer", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModelloDom4jWriterMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated DOM4J reader.
     */
    @Parameter( defaultValue = "${basedir}/target/generated-sources/modello", required = true )
    private File outputDirectory;

    protected String getGeneratorType()
    {
        return "dom4j-writer";
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
