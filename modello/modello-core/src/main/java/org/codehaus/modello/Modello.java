package org.codehaus.modello;

import org.codehaus.modello.generator.java.JavaGenerator;
import org.codehaus.modello.generator.xml.schema.XmlSchemaGenerator;
import org.codehaus.modello.generator.xml.xdoc.XdocGenerator;
import org.codehaus.modello.generator.xml.xpp3.Xpp3ReaderGenerator;
import org.codehaus.modello.generator.xml.xpp3.Xpp3WriterGenerator;

import java.io.File;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Modello
{
    private String model;

    private String mode;

    private String outputDirectory;

    private String modelVersion;

    public Modello()
    {
    }

    public void parseArgumentsFromCommandLine( String[] args )
        throws Exception
    {
        if ( args.length != 3 )
        {
            System.err.println( "Usage: modello <model> <mode> <output directory> " );
            System.exit( 1 );
        }

        setModel( args[0] );

        setMode( args[1] );

        setOutputDirectory( args[2] );

        setModelVersion( args[3] );
    }

    public void work()
        throws Exception
    {
        if ( model == null || model.trim().length() == 0 )
        {
            throw new Exception( "Missing model." );
        }

        if ( mode == null || mode.trim().length() == 0 )
        {
            throw new Exception( "Missing mode." );
        }

        if ( outputDirectory == null || outputDirectory.trim().length() == 0 )
        {
            throw new Exception( "Missing output directory." );
        }

        if ( !new File( model ).isFile() )
        {
            throw new Exception( "The model must be a file." );
        }

        if ( !new File( outputDirectory ).isDirectory() )
        {
            throw new Exception( "The output directory must be a directory." );
        }

        if ( mode.equals( "java" ) )
        {
            javaGenerator( model, outputDirectory, modelVersion );
        }
        else if ( mode.equals( "xsd" ) )
        {
            xmlSchemaGenerator( model, outputDirectory, modelVersion );
        }
        else if ( mode.equals( "xdoc" ) )
        {
            xdocGenerator( model, outputDirectory, modelVersion );
        }
        else if ( mode.equals( "xpp3" ) )
        {
            xpp3UnmarshallerGenerator( model, outputDirectory, modelVersion );

            xpp3MarshallerGenerator( model, outputDirectory, modelVersion );
        }
    }

    public static void main( String[] args )
        throws Exception
    {
        Modello modello = new Modello();

        modello.parseArgumentsFromCommandLine( args );

        modello.work();
    }

    public void javaGenerator( String model, String outputDirectory, String modelVersion )
        throws Exception
    {
        JavaGenerator generator = new JavaGenerator( model, new File( outputDirectory ).getPath(), modelVersion );

        System.out.println( "Generating java in " + outputDirectory + " from " + model );

        generator.generate();
    }

    public void xmlSchemaGenerator( String model, String outputDirectory, String modelVersion )
        throws Exception
    {
        XmlSchemaGenerator generator = new XmlSchemaGenerator( model, new File( outputDirectory ).getPath(), modelVersion );

        System.out.println( "Generating xml schema in " + outputDirectory + " from " + model );

        generator.generate();
    }

    public void xdocGenerator( String model, String outputDirectory, String modelVersion )
        throws Exception
    {
        XdocGenerator generator = new XdocGenerator( model, new File( outputDirectory ).getPath(), modelVersion );

        System.out.println( "Generating xdoc in " + outputDirectory + " from " + model );

        generator.generate();
    }

    public void xpp3UnmarshallerGenerator( String model, String outputDirectory, String modelVersion )
        throws Exception
    {
        Xpp3ReaderGenerator generator = new Xpp3ReaderGenerator( model, new File( outputDirectory ).getPath(), modelVersion );

        System.out.println( "Generating xpp3 unmarshaller in " + outputDirectory + " from " + model );

        generator.generate();
    }

    public void xpp3MarshallerGenerator( String model, String outputDirectory, String modelVersion )
        throws Exception
    {
        Xpp3WriterGenerator generator = new Xpp3WriterGenerator( model, new File( outputDirectory ).getPath(), modelVersion );

        System.out.println( "Generating xpp3 marshaller in " + outputDirectory + " from " + model );

        generator.generate();
    }

    public void setMode( String mode )
    {
        this.mode = mode;
    }

    public void setModel( String model )
    {
        this.model = model;
    }

    public void setOutputDirectory( String outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    public void setModelVersion( String modelVersion )
    {
        this.modelVersion = modelVersion;
    }

}
