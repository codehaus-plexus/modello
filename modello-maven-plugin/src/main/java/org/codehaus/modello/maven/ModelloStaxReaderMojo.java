package org.codehaus.modello.maven;

import java.io.File;

/**
 * Creates an StAX reader from the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @goal stax-reader
 * @phase generate-sources
 */
public class ModelloStaxReaderMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated StAX reader.
     *
     * @parameter expression="${basedir}/target/generated-sources/modello"
     * @required
     */
    private File outputDirectory;

    protected String getGeneratorType()
    {
        return "stax-reader";
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
