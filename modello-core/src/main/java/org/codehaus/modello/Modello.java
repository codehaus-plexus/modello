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

import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelValidationException;
import org.codehaus.plexus.embed.Embedder;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.DefaultPlexusContainer;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Modello
//    extends AbstractLogEnabled
{
    PlexusContainer container;

    ModelloCore core;

    public Modello()
        throws ModelloException
    {
        try
        {
            container = new DefaultPlexusContainer();

            core = (ModelloCore) container.lookup( ModelloCore.ROLE );
        }
        catch( Exception ex )
        {
            throw new ModelloException( "Error while starting plexus.", ex );
        }
    }

    public void generate( Reader modelReader, String outputType, Properties parameters )
        throws ModelloException, ModelValidationException
    {
        Model model = core.loadModel( modelReader );

        core.generate( model, outputType, parameters );
    }

    public void translate( Reader reader, Writer writer, String outputType, Properties parameters )
        throws ModelloException, ModelValidationException
    {
        Model model = core.translate( reader, outputType, parameters );

        core.saveModel( model, writer );
    }
/*
    private Logger logger;

    private GeneratorPluginManager generatorPluginManager;

    private MetaDataPluginManager metaDataPluginManager;

    private ModelBuilder modelBuilder;

    public Modello()
    {
        logger = new ConsoleLogger();

        generatorPluginManager = new GeneratorPluginManager();

        metaDataPluginManager = new MetaDataPluginManager();

        modelBuilder = new ModelBuilder();

        Logger logger = new ConsoleLogger();

        initializeLogger( logger, this );

        initializeLogger( logger, generatorPluginManager );

        initializeLogger( logger, metaDataPluginManager );

        initializeLogger( logger, modelBuilder );
    }

    public void initialize()
        throws ModelloException
    {
        generatorPluginManager.initialize();

        metaDataPluginManager.initialize();

        modelBuilder.setGeneratorPluginManager( generatorPluginManager );

        modelBuilder.setMetaDataPluginManager( metaDataPluginManager );

        modelBuilder.initialize();
    }

    public ModelBuilder getModelBuilder()
    {
        return modelBuilder;
    }

    public Model work( File modelFile, String mode, File outputDirectory, 
                       String modelVersion, boolean packageWithVersion )
        throws ModelloException, ModelValidationException
    {
        try
        {
            Model model = getModel( modelFile );

            return work2( model, mode, outputDirectory, modelVersion, packageWithVersion );
        }
        catch( ModelloRuntimeException ex )
        {
            throw new ModelloException( "Exception while generating.", ex );
        }
    }

    public Model getModel( File modelFile )
        throws ModelloException, ModelValidationException
    {
        if ( modelFile == null )
        {
            throw new ModelloException( "Missing model file." );
        }

        if ( !modelFile.isFile() )
        {
            throw new ModelloException( "The model must be a file." );
        }

        return modelBuilder.getModel( modelFile );
    }

    public Model work2( Model model, String mode, File outputDirectory, 
                      String modelVersion, boolean packageWithVersion )
        throws ModelloException
    {
        if ( mode == null || mode.trim().length() == 0 )
        {
            throw new ModelloException( "Missing mode." );
        }

        if ( outputDirectory == null )
        {
            throw new ModelloException( "Missing output directory." );
        }

        if ( !outputDirectory.isDirectory() )
        {
            throw new ModelloException( "The output directory must be a directory." );
        }

        try
        {
            new AbstractGeneratorPlugin.Version( modelVersion, "Model version parameter" );
        }
        catch( ModelloRuntimeException ex )
        {
            throw new ModelloException( "Error in the model version parameter.", ex );
        }

        Properties parameters = new Properties();

        parameters.put( ModelloParameterConstants.OUTPUT_DIRECTORY, outputDirectory.getAbsolutePath() );

        parameters.put( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( packageWithVersion ) );

        parameters.put( ModelloParameterConstants.VERSION, modelVersion );

        if ( generatorPluginManager.hasPlugin( mode ) )
        {
            GeneratorPlugin generator = generatorPluginManager.getGeneratorPlugin( mode );

            generator.generate( model, parameters );
        }
*/
/*
        else if ( mode.equals( "java" ) )
        {
            JavaGenerator generator = new JavaGenerator( model, outputDirectory, modelVersion, packageWithVersion );

            System.out.println( "Generating java in " + outputDirectory + " from " + model );

            generator.generate();
        }
        else if ( mode.equals( "xsd" ) )
        {
            XmlSchemaGenerator generator = new XmlSchemaGenerator( model, outputDirectory, modelVersion, false );

            System.out.println( "Generating xml schema in " + outputDirectory + " from " + model );

            generator.generate();
        }
        else if ( mode.equals( "xdoc" ) )
        {
            XdocGenerator generator = new XdocGenerator( model, outputDirectory, modelVersion, false );

            System.out.println( "Generating xdoc in " + outputDirectory + " from " + model );

            generator.generate();
        }
/*
        else if ( mode.equals( "xpp3" ) )
        {
            JavaGenerator generator = new Xpp3ReaderGenerator( model, outputDirectory, modelVersion, packageWithVersion );

            System.out.println( "Generating xpp3 unmarshaller in " + outputDirectory + " from " + model);

            generator.generate();

            generator = new Xpp3WriterGenerator( model, outputDirectory, modelVersion, packageWithVersion );

            System.out.println( "Generating xpp3 marshaller in " + outputDirectory + " from " + model );

            generator.generate();
        }
*/
/*
        else
        {
            throw new ModelloRuntimeException( "Unknown mode: '" + mode + "'." );
        }

        return model;
    }

    private void initializeLogger( Logger logger, Object object )
    {
        if ( object instanceof LogEnabled )
        {
            ((LogEnabled) object).setLogger( logger );
        }
    }
*/
}
