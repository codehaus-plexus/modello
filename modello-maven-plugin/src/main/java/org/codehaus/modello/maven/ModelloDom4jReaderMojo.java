package org.codehaus.modello.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Creates a DOM4J reader from the model.
 *
 * @version $Id$
 */
@Mojo( name = "dom4j-reader", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModelloDom4jReaderMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated DOM4J reader.
     */
    @Parameter( defaultValue = "${basedir}/target/generated-sources/modello", required = true )
    private File outputDirectory;

    protected String getGeneratorType()
    {
        return "dom4j-reader";
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
