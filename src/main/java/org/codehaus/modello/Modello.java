package org.codehaus.modello;

import java.io.File;
import java.util.Properties;

import org.codehaus.modello.generator.AbstractGeneratorPlugin;
import org.codehaus.modello.generator.GeneratorPlugin;
import org.codehaus.modello.generator.GeneratorPluginManager;
import org.codehaus.modello.metadata.MetaDataPluginManager;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Modello
    extends AbstractLogEnabled
{
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
}
