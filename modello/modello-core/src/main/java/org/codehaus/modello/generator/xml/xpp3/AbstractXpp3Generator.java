package org.codehaus.modello.generator.xml.xpp3;

/*
 * LICENSE
 */

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.generator.java.JavaGenerator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractXpp3Generator
    extends JavaGenerator
{
    public AbstractXpp3Generator( String model, String outputDirectory, String modelVersion, boolean version )
    {
        super( model, outputDirectory, modelVersion, version );
    }

    protected String getFileName( String suffix )
        throws ModelloException
    {
        String name = getModel().getName();

        if ( isPackageWithVersion() )
        {
            Version version = getModelVersion();

            return name + version.toString( "V" ) + suffix;
        }
        else
        {
            return name + suffix;
        }
    }
}
