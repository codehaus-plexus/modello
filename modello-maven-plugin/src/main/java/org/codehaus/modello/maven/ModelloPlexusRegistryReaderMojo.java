package org.codehaus.modello.maven;

/*
 * Copyright (c) 2007, Codehaus.org
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

import java.io.File;

/**
 * Creates a Plexus Registry reader from the Modello model.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: ModelloJPoxStoreMojo.java 712 2006-11-25 02:16:58Z brett $
 * @goal registry-reader
 * @phase generate-sources
 */
public class ModelloPlexusRegistryReaderMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated reader.
     *
     * @parameter expression="${basedir}/target/generated-sources/modello"
     * @required
     */
    private File outputDirectory;

    protected String getGeneratorType()
    {
        return "registry-reader";
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
