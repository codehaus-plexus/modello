package org.codehaus.modello;

import java.io.File;

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

    private static String mode;

    private static String outputDirectory;

    private static String modelVersion;

    private static boolean packageWithVersion;

    public static void main( String[] args )
        throws Exception
    {
        Modello modello = new Modello();

        parseArgumentsFromCommandLine( args );

        modello.initialize();

        modello.work( new File( modelFile ), mode, new File( outputDirectory ), modelVersion, packageWithVersion );
    }

    public static void parseArgumentsFromCommandLine( String[] args )
        throws Exception
    {
        if ( args.length != 5 )
        {
            System.err.println( "Usage: modello <model> <mode> <output directory> <modelVersion> <packageWithVersion>" );

            System.exit( 1 );
        }

        modelFile = args[0];

        mode = args[1];

        outputDirectory = args[2];

        modelVersion = args[3];

        packageWithVersion = Boolean.valueOf( args[4] ).booleanValue();
    }
}
