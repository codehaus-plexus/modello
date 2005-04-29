package org.codehaus.modello;

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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.ModelValidationException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractModelloGeneratorMojo
    extends AbstractMojo
{
    private String basedir;

    private String outputDirectory;

    private String model;

    private String version;

    /** 
     *  True if the generated package names should include the version.
     *  @parameter expression="${packageWithVersion}" 
     *  @required
     *  @todo make this a Boolean
     */
    private Boolean packageWithVersion = Boolean.FALSE;

    private ModelloCore modelloCore;

    private MavenProject project;
    
    protected abstract String getGeneratorType();
    
    protected boolean producesCompilableResult()
    {
        return true;
    }

    public void execute() throws MojoExecutionException
    {
        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        
        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        // Just pass a Map in here, no need to make properties up again.

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, outputDirectory );

        parameters.setProperty( ModelloParameterConstants.VERSION, version );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, packageWithVersion.toString() );

        try
        {
            modelloCore.generate( modelloCore.loadModel( new FileReader( new File( basedir, model ) ) ), getGeneratorType(), parameters );
            
            if(producesCompilableResult() && project != null )
            {
                project.addCompileSourceRoot( outputDirectory );
            }
        }
        catch (FileNotFoundException e)
        {
            throw new MojoExecutionException("Couldn't find file.", e);
        }
        catch (ModelloException e)
        {
            throw new MojoExecutionException("Error generating.", e);
        }
        catch (ModelValidationException e)
        {
            throw new MojoExecutionException("Error generating.", e);
        }
    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public ModelloCore getModelloCore()
    {
        return modelloCore;
    }

    public void setModelloCore(ModelloCore modelloCore)
    {
        this.modelloCore = modelloCore;
    }

    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }

    public Boolean getPackageWithVersion()
    {
        return packageWithVersion;
    }

    public void setPackageWithVersion(Boolean packageWithVersion)
    {
        this.packageWithVersion = packageWithVersion;
    }

    public MavenProject getProject()
    {
        return project;
    }

    public void setProject(MavenProject project)
    {
        this.project = project;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
