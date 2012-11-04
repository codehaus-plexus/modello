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

import java.io.File;
import java.util.Properties;

import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.modello.ModelloParameterConstants;

/**
 * @author Hervé Boutemy
 */
public abstract class AbstractModelloSourceGeneratorMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated Java beans.
     */
    @Parameter( defaultValue = "${project.build.directory}/generated-sources/modello", required = true )
    private File outputDirectory;

    /**
     * Generate DOM content as plexus-utils <code>Xpp3Dom</code> objects instead of <code>org.w3c.dom.Element</code>.
     */
    @Parameter( property = "domAsXpp3", defaultValue = "true" )
    private boolean domAsXpp3;

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

        parameters.setProperty( ModelloParameterConstants.DOM_AS_XPP3, Boolean.toString( domAsXpp3 ) );
    }
}
