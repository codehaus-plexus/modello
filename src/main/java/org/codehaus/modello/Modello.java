package org.codehaus.modello;

import org.codehaus.modello.generator.java.JavaGenerator;
import org.codehaus.modello.generator.xml.schema.XmlSchemaGenerator;
import org.codehaus.modello.generator.xml.xdoc.XdocGenerator;

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

        String mode = args[1];

        String outputDirectory = args[2];

        if ( mode.equals( "java" ) )
        {
        javaGenerator( model, outputDirectory );
        }
        else if ( mode.equals( "xsd" ) )
        {
            xmlSchemaGenerator( model, outputDirectory );
        }
        else if( mode.equals( "xdoc" ) )
        {
            xdocGenerator( model, outputDirectory );
        }
    }

    public static void main( String[] args )
        throws Exception
    {
        Modello modello = new Modello( args );
    }

    public void javaGenerator( String model, String outputDirectory )
        throws Exception
    {
        JavaGenerator generator = new JavaGenerator( model, new File( outputDirectory ).getPath() );

        generator.generate();
    }

    public void xmlSchemaGenerator( String model, String outputDirectory )
        throws Exception
    {
        XmlSchemaGenerator generator = new XmlSchemaGenerator( model, new File( outputDirectory ).getPath() );

        generator.generate();
    }

    public void xdocGenerator( String model, String outputDirectory )
        throws Exception
    {
        XdocGenerator generator = new XdocGenerator( model, new File( outputDirectory ).getPath() );

        generator.generate();
    }
}
