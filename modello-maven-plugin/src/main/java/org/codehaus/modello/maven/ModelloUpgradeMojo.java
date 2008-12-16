package org.codehaus.modello.maven;

import java.io.File;

/**
 * Creates classes that can upgrade to model version X from X-1.
 *
 * @goal upgrade
 * @phase generate-sources
 */
public class ModelloUpgradeMojo
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
        return "upgrade";
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
