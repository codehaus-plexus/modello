package org.codehaus.modello.maven;

import java.io.File;

/**
 * Creates an StAX writer from the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @goal stax-writer
 * @phase generate-sources
 */
public class ModelloStaxWriterMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated StAX writer.
     *
     * @parameter expression="${basedir}/target/generated-sources/modello"
     * @required
     */
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
