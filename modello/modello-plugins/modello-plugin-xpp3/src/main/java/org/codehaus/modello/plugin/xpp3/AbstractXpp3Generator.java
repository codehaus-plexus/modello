package org.codehaus.modello.plugin.xpp3;

/*
 * LICENSE
 */

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.plugin.AbstractModelloGenerator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractXpp3Generator
    extends AbstractModelloGenerator
{
    protected String getFileName( String suffix )
        throws ModelloException
    {
        String name = getModel().getName();

        if ( isPackageWithVersion() )
        {
            return name + getGeneratedVersion().toString( "V" ) + suffix;
        }
        else
        {
            return name + suffix;
        }
    }
}
