package org.codehaus.modello.maven;

import java.io.File;

/**
 * Creates classes that can convert between different versions of the model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @goal converters
 * @phase generate-sources
 */
public class ModelloConvertersMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated Java beans.
     *
     * @parameter expression="${basedir}/target/generated-sources/modello"
     * @required
     */
    private File outputDirectory;

    protected String getGeneratorType()
    {
        return "converters";
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
