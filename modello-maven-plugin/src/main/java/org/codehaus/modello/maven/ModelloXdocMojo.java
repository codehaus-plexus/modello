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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.modello.ModelloParameterConstants;

import java.io.File;
import java.util.Properties;

/**
 * Creates documentation for the model in xdoc format.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
@Mojo( name = "xdoc", threadSafe = true )
public class ModelloXdocMojo
    extends AbstractModelloGeneratorMojo
{
    /**
     * The output directory of the generated documentation.
     */
    @Parameter( defaultValue = "${project.build.directory}/generated-site/xdoc", required = true )
    private File outputDirectory;
    
    /**
     * 
     * @since 1.0-alpha-21
     */
    @Parameter
    private String xdocFileName;

    /**
     * The first version of the model. This is used to decide whether or not
     * to show the since column. If this is not specified, it defaults to the
     * version of the model, which in turn means that the since column will not
     * be shown.
     *
     * @since 1.0-alpha-14
     */
    @Parameter
    private String firstVersion;

    protected String getGeneratorType()
    {
        return "xdoc";
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

        // Use version if firstVersion was not specified
        if ( firstVersion == null )
        {
            firstVersion = getVersion();
        }

        parameters.put( ModelloParameterConstants.FIRST_VERSION, firstVersion );
       
        if ( xdocFileName != null )
        {
            parameters.put( ModelloParameterConstants.OUTPUT_XDOC_FILE_NAME, xdocFileName );
        }
    }

    public String getFirstVersion()
    {
        return firstVersion;
    }

    public void setFirstVersion( String firstVersion )
    {
        this.firstVersion = firstVersion;
    }
}
