package org.codehaus.modello.maven;

import org.codehaus.modello.maven.AbstractModelloGeneratorMojo;

import java.io.File;


/**
 * @goal mkleint-writer
 *
 * @phase generate-sources
 *
 * @description Creates mkleint writer from the model.
 *
 */
public class ModelloMkleintWriterMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * @parameter expression="${basedir}/target/generated-sources/modello"
     *
     * @required
     */
    private File outputDirectory;

    protected String getGeneratorType()
    {
        return "mkleint-writer";
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
