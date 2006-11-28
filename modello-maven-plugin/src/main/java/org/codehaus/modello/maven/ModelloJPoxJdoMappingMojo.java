package org.codehaus.modello.maven;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.modello.ModelloParameterConstants;

import java.io.File;
import java.util.Properties;

/**
 * Creates a JDO mapping from the Modello model.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @goal jpox-jdo-mapping
 * @phase generate-resources
 * @description Creates a JDO mapping from the Modello model.
 */
public class ModelloJPoxJdoMappingMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated classes of the JDO mapping.
     *
     * @parameter expression="${basedir}/target/classes/META-INF"
     * @required
     */
    private File outputDirectory;

    /**
     * Produce a mapping file suitable for replication. It will have an alternate extension of '.jdorepl' so it is not
     * picked up by default, and all value-strategy and objectid-class entries are removed from the mapping so that
     * the original identities can be used.
     *
     * @parameter default-value="false"
     */
    private boolean replicationParameters;

    protected String getGeneratorType()
    {
        return "jpox-jdo-mapping";
    }

    protected boolean producesCompilableResult()
    {
        return false;
    }

    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }


    protected void customizeParameters( Properties parameters )
    {
        super.customizeParameters( parameters );

        if ( replicationParameters )
        {
            parameters.setProperty( ModelloParameterConstants.FILENAME, "package.jdorepl" );

            parameters.setProperty( "JPOX.override.value-strategy", "off" );
            parameters.setProperty( "JPOX.override.objectid-class", "" );
        }
    }
}
