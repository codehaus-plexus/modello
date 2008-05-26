package org.codehaus.modello.maven;

import java.io.File;

/**
 * Creates a DOM4J writer from the model.
 *
 * @version $Id:$
 * @goal dom4j-writer
 * @phase generate-sources
 */
public class ModelloDom4jWriterMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated DOM4J reader.
     *
     * @parameter expression="${basedir}/target/generated-sources/modello"
     * @required
     */
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
