package org.codehaus.modello;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ModelloRuntimeException
    extends RuntimeException
{
    public ModelloRuntimeException( String msg )
    {
        super( msg );
    }

    public ModelloRuntimeException( String msg, Throwable cause )
    {
        super( msg, cause );
    }
}
