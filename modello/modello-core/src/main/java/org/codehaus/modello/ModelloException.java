package org.codehaus.modello;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloException
    extends Exception
{
    public ModelloException( String msg )
    {
        super( msg );
    }

    public ModelloException( String msg, Throwable cause )
    {
        super( msg, cause );
    }
}
