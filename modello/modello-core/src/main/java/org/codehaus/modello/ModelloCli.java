package org.codehaus.modello;

import java.io.FileReader;
import java.util.Properties;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloCli
{
    private static String modelFile;

    private static String outputType;

    private static Properties parameters;

//    private static String outputDirectory;

//    private static String modelVersion;

//    private static boolean packageWithVersion;

    public static void main( String[] args )
        throws Exception
    {
        Modello modello = new Modello();

        parseArgumentsFromCommandLine( args );

//        modello.initialize();

        modello.generate( new FileReader( modelFile ), outputType, parameters );
//        modello.work( new File( modelFile ), mode, new File( outputDirectory ), modelVersion, packageWithVersion );
    }

    public static void parseArgumentsFromCommandLine( String[] args )
        throws Exception
    {
        if ( args.length != 5 )
        {
            System.err.println( "Usage: modello <model> <outputType> <output directory> <modelVersion> <packageWithVersion>" );

            System.exit( 1 );
        }

        modelFile = args[0];

        outputType = args[1];

        parameters = new Properties();

        String outputDirectory = args[2];

        String modelVersion = args[3];

        String packageWithVersion = args[4];

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, outputDirectory );

        parameters.setProperty( ModelloParameterConstants.VERSION, modelVersion );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, packageWithVersion );
    }
}
