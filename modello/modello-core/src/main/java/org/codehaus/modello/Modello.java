package org.codehaus.modello;

import org.codehaus.modello.generator.AbstractGenerator;
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

    private boolean packageWithVersion;

    public Modello()
    {
    }

    public void parseArgumentsFromCommandLine( String[] args )
        throws Exception
    {
        if ( args.length != 5 )
        {
            System.err.println( "Usage: modello <model> <mode> <output directory> " );

            System.exit( 1 );
        }

        model = args[0];

        mode = args[1];

        outputDirectory = args[2];

        modelVersion = args[3];

        if ( args[4].equals( "true" ) )
        {
            packageWithVersion = true;
        }
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
            JavaGenerator g = new JavaGenerator( model, new File( outputDirectory ).getPath(), modelVersion, packageWithVersion );

            System.out.println( "Generating java in " + outputDirectory + " from " + model );

            g.generate();
        }
        else if ( mode.equals( "xsd" ) )
        {
            XmlSchemaGenerator generator = new XmlSchemaGenerator( model, new File( outputDirectory ).getPath(), modelVersion );

            System.out.println( "Generating xml schema in " + outputDirectory + " from " + model );

            generator.generate();
        }
        else if ( mode.equals( "xdoc" ) )
        {
            XdocGenerator generator = new XdocGenerator( model, new File( outputDirectory ).getPath(), modelVersion );

            System.out.println( "Generating xdoc in " + outputDirectory + " from " + model );

            generator.generate();
        }
        else if ( mode.equals( "xpp3" ) )
        {
            JavaGenerator generator = new Xpp3ReaderGenerator( model, new File( outputDirectory ).getPath(), modelVersion, packageWithVersion );

            System.out.println( "Generating xpp3 unmarshaller in " + outputDirectory + " from " + model );

            generator.generate();

            generator = new Xpp3WriterGenerator( model, new File( outputDirectory ).getPath(), modelVersion, packageWithVersion );

            System.out.println( "Generating xpp3 marshaller in " + outputDirectory + " from " + model );

            generator.generate();
        }
    }

    public static void main( String[] args )
        throws Exception
    {
        Modello modello = new Modello();

        modello.parseArgumentsFromCommandLine( args );

        modello.work();
    }
}
