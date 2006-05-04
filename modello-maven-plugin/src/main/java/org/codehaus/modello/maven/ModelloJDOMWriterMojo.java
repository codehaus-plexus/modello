package org.codehaus.modello.maven;

import org.codehaus.modello.maven.AbstractModelloGeneratorMojo;

import java.io.File;


/**
 * @goal jdom-writer
 *
 * @phase generate-sources
 *
 * @description Creates jdom writer from the model that is capable of preserving element ordering
 * and comments. In future it should also preserve whitespace.
 *
 */
public class ModelloJDOMWriterMojo
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
        return "jdom-writer";
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
