package org.codehaus.modello;

import org.codehaus.modello.generator.java.JavaGenerator;

import java.io.File;

/**
 *
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class Modello
{
    public Modello( String[] args )
        throws Exception
    {
        String model = args[0];

        String outputDirectory = args[1];

        JavaGenerator generator = new JavaGenerator( model, new File( outputDirectory, "java" ).getPath() );

        generator.generate();
    }

    public static void main( String[] args )
        throws Exception
    {
        Modello modello = new Modello( args );
    }
}
