package org.codehaus.modello;

import java.io.File;

import org.codehaus.modello.generator.AbstractGenerator;
import org.codehaus.modello.generator.java.JavaGenerator;
import org.codehaus.modello.generator.xml.schema.XmlSchemaGenerator;
import org.codehaus.modello.generator.xml.xdoc.XdocGenerator;
import org.codehaus.modello.generator.xml.xpp3.Xpp3ReaderGenerator;
import org.codehaus.modello.generator.xml.xpp3.Xpp3WriterGenerator;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Modello
    extends AbstractLogEnabled
{
    private Logger logger;

    private PluginManager pluginManager;

    private ModelBuilder modelBuilder;

    public Modello()
    {
        logger = new ConsoleLogger();

        pluginManager = new PluginManager();

        modelBuilder = new ModelBuilder();

        setLogger( logger );

        pluginManager.setLogger( logger );

        modelBuilder.setLogger( logger );
    }

    public void initialize()
        throws Exception
    {
        pluginManager.initialize();

        modelBuilder.initialize( pluginManager );
    }

    public void work( String modelFile, String mode, String outputDirectory, 
                      String modelVersion, boolean packageWithVersion )
        throws ModelloException
    {
        try
        {
            work2( modelFile, mode, outputDirectory, modelVersion, packageWithVersion );
        }
        catch( ModelloRuntimeException ex )
        {
            throw new ModelloException( "Exception while generating.", ex );
        }
    }

    public void work2( String modelFile, String mode, String outputDirectory, 
                      String modelVersion, boolean packageWithVersion )
        throws ModelloException
    {
        if ( modelFile == null || modelFile.trim().length() == 0 )
        {
            throw new ModelloException( "Missing model." );
        }

        if ( mode == null || mode.trim().length() == 0 )
        {
            throw new ModelloException( "Missing mode." );
        }

        if ( !new File( modelFile ).isFile() )
        {
            throw new ModelloException( "The model must be a file." );
        }

        if ( outputDirectory == null || outputDirectory.trim().length() == 0 )
        {
            throw new ModelloException( "Missing output directory." );
        }

        if ( !new File( outputDirectory ).isDirectory() )
        {
            throw new ModelloException( "The output directory must be a directory." );
        }

        try
        {
            new AbstractGenerator.Version( modelVersion, "Model version parameter" );
        }
        catch( ModelloRuntimeException ex )
        {
            throw new ModelloException( "Error in the model version parameter.", ex );
        }

        if ( pluginManager.hasPlugin( mode ) )
        {
            ModelloPlugin plugin = pluginManager.getPlugin( mode );

            Model model = modelBuilder.getModel( modelFile );

            plugin.generate( model );
        }
        else if ( mode.equals( "java" ) )
        {
            JavaGenerator generator = new JavaGenerator( modelFile, new File( outputDirectory ).getPath(), modelVersion, packageWithVersion );

            System.out.println( "Generating java in " + outputDirectory + " from " + modelFile );

            generator.generate();
        }
        else if ( mode.equals( "xsd" ) )
        {
            XmlSchemaGenerator generator = new XmlSchemaGenerator( modelFile, new File( outputDirectory ).getPath(), modelVersion, false );

            System.out.println( "Generating xml schema in " + outputDirectory + " from " + modelFile );

            generator.generate();
        }
        else if ( mode.equals( "xdoc" ) )
        {
            XdocGenerator generator = new XdocGenerator( modelFile, new File( outputDirectory ).getPath(), modelVersion, false );

            System.out.println( "Generating xdoc in " + outputDirectory + " from " + modelFile );

            generator.generate();
        }
        else if ( mode.equals( "xpp3" ) )
        {
            JavaGenerator generator = new Xpp3ReaderGenerator( modelFile, new File( outputDirectory ).getPath(), modelVersion, packageWithVersion );

            System.out.println( "Generating xpp3 unmarshaller in " + outputDirectory + " from " + modelFile );

            generator.generate();

            generator = new Xpp3WriterGenerator( modelFile, new File( outputDirectory ).getPath(), modelVersion, packageWithVersion );

            System.out.println( "Generating xpp3 marshaller in " + outputDirectory + " from " + modelFile );

            generator.generate();
        }
        else
        {
            throw new ModelloRuntimeException( "Unknown mode: '" + mode + "'." );
        }
    }
}
