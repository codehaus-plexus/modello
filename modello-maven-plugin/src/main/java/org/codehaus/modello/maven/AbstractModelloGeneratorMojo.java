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

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelValidationException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractModelloGeneratorMojo
    extends AbstractMojo
{
    // ----------------------------------------------------------------------
    // Parameters
    // ----------------------------------------------------------------------

    /**
     * Base directory of the project.
     *
     * @parameter expression="${basedir}"
     * @required
     */
    private String basedir;

    /**
     * Relative path to the mdo file for the current model.
     *
     * @parameter expression="${model}"
     * @deprecated Use models instead
     */
    private String model;

    /**
     * List of relative paths to mdo files containing the models.
     *
     * @parameter
     * @todo Make this @required when model is no longer supported
     */
    private String[] models;

    /**
     * The version of the model we will be working on.
     *
     * @parameter expression="${version}"
     * @required
     */
    private String version;

    /**
     * The encoding to use when generating Java source files.
     * 
     * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
     * @since 1.0-alpha-19
     */
    private String encoding;

    /**
     * True if the generated package names should include the version.
     *
     * @parameter expression="${packageWithVersion}" default-value="false"
     * @required
     */
    private boolean packageWithVersion;

    /**
     * <p>Note: This is passed by Maven and must not be configured by the user.</p>
     *
     * @component
     */
    private ModelloCore modelloCore;

    /**
     * The Maven project instance for the executing project.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Additional historical versions to generate, each being packaged with the version regardless of the
     * <code>packageWithVersion</code> setting.
     *
     * @parameter
     */
    private List/*<String>*/ packagedVersions = Collections.EMPTY_LIST;

    // ----------------------------------------------------------------------
    // Overridables
    // ----------------------------------------------------------------------

    protected abstract String getGeneratorType();

    public abstract File getOutputDirectory();

    protected boolean producesCompilableResult()
    {
        return true;
    }
    
    protected boolean producesResources()
    {
        return false;
    }

    /**
     * Creates a Properties objects.
     * <p/>
     * The abstract mojo will override the output directory, the version and the
     * package with version flag.
     *
     * @return the parameters
     */
    protected Properties createParameters()
    {
        return new Properties();
    }

    /**
     * Override this method to customize the values in the properties set.
     * <p/>
     * This method will be called after the parameters have been populated with the
     * parameters in the abstract mojo.
     *
     * @param parameters the parameters to customize
     */
    protected void customizeParameters( Properties parameters )
    {
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void execute()
        throws MojoExecutionException
    {
        String outputDirectory = getOutputDirectory().getAbsolutePath();

        getLog().info( "outputDirectory: " + outputDirectory );

        // ----------------------------------------------------------------------
        // Initialize the parameters
        // ----------------------------------------------------------------------

        Properties parameters = createParameters();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, outputDirectory );

        parameters.setProperty( ModelloParameterConstants.VERSION, version );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION,
                                Boolean.toString( packageWithVersion ) );

        parameters.setProperty( ModelloParameterConstants.ALL_VERSIONS,
                                StringUtils.join( packagedVersions.iterator(), "," ) );

        if ( encoding != null )
        {
            parameters.setProperty( ModelloParameterConstants.ENCODING, encoding );
        }

        customizeParameters( parameters );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        try
        {
            if ( models != null )
            {
                for ( int i = 0; i < models.length; i++ )
                {
                    doExecute( models[i], outputDirectory, parameters );
                }
            }

            // Keeping this for backwards compatibility
            if ( model != null )
            {
                doExecute( model, outputDirectory, parameters );
            }
        }
        catch ( FileNotFoundException e )
        {
            throw new MojoExecutionException( "Couldn't find file.", e );
        }
        catch ( ModelloException e )
        {
            throw new MojoExecutionException( "Error generating.", e );
        }
        catch ( ModelValidationException e )
        {
            throw new MojoExecutionException( "Error generating.", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Couldn't read file.", e );
        }
    }

    /**
     * Performs execute on a single specified model.
     *
     * @param modelStr
     * @param parameters
     * @param outputDirectory
     * @throws IOException
     * @throws ModelloException
     * @throws ModelValidationException
     */
    private void doExecute( String modelStr, String outputDirectory, Properties parameters )
        throws IOException, ModelloException, ModelValidationException
    {
        getLog().info( "Working on model: " + modelStr );

        Model model = modelloCore.loadModel( new File( basedir, modelStr ) );

        // TODO: dynamically resolve/load the generator type
        getLog().info( "Generating current version: " + version );
        modelloCore.generate( model, getGeneratorType(), parameters );

        for ( Iterator i = packagedVersions.iterator(); i.hasNext(); )
        {
            String version = (String) i.next();

            parameters.setProperty( ModelloParameterConstants.VERSION, version );

            parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( true ) );

            getLog().info( "Generating packaged version: " + version );
            modelloCore.generate( model, getGeneratorType(), parameters );
        }

        if ( producesCompilableResult() && project != null )
        {
            project.addCompileSourceRoot( outputDirectory );
        }

        if ( producesResources() && project != null )
        {
            Resource resource = new Resource();
            resource.setDirectory( outputDirectory );
            project.addResource( resource );
        }
    }

    // ----------------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------------

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir( String basedir )
    {
        this.basedir = basedir;
    }

    /**
     * @deprecated Use getModels() instead
     */
    public String getModel()
    {
        return model;
    }

    /**
     * @deprecated Use setModels(String[]) instead
     */
    public void setModel( String model )
    {
        this.model = model;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public boolean getPackageWithVersion()
    {
        return packageWithVersion;
    }

    public void setPackageWithVersion( boolean packageWithVersion )
    {
        this.packageWithVersion = packageWithVersion;
    }

    public ModelloCore getModelloCore()
    {
        return modelloCore;
    }

    public void setModelloCore( ModelloCore modelloCore )
    {
        this.modelloCore = modelloCore;
    }

    public MavenProject getProject()
    {
        return project;
    }

    public void setProject( MavenProject project )
    {
        this.project = project;
    }

    public void setPackagedVersions( List/*<String>*/ packagedVersions )
    {
        this.packagedVersions = Collections.unmodifiableList( packagedVersions );
    }

    /**
     * @return Returns the paths to the models.
     */
    public String[] getModels()
    {
        return models;
    }

    /**
     * @param models Sets the paths to the models.
     */
    public void setModels( String[] models )
    {
        this.models = models;
    }
}
