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

import java.io.StringReader;
import java.util.Properties;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

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

        String fileString = FileUtils.fileRead(modelFile);

        fileString = StringUtils.replace( fileString, "<description>", "<description>" );

        fileString = StringUtils.replace( fileString, "</description>", "</description>" );

        modello.generate( new StringReader( fileString ), outputType, parameters );
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
